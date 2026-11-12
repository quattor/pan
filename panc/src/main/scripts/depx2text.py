#!/usr/bin/env python3
"""Render a DEPX JSON include trace as a readable text trace."""

from __future__ import annotations

import argparse
import gzip
import json
import re
import sys
import zlib
from pathlib import Path
from urllib.parse import quote, urlsplit


def _object(value: object, label: str) -> dict:
    if not isinstance(value, dict):
        raise ValueError(f"{label} must be an object")
    return value


def _string(value: object, label: str, *, empty: bool = False) -> str:
    if not isinstance(value, str) or (not empty and not value):
        raise ValueError(f"{label} must be a string")
    return value


def _index(value: object, label: str) -> int:
    if type(value) is not int or not 0 <= value <= 9007199254740991:
        raise ValueError(f"{label} must be a non-negative integer")
    return value


def _array(value: object, label: str) -> list:
    if not isinstance(value, list):
        raise ValueError(f"{label} must be an array")
    return value


def _quoted(value: str | None) -> str:
    if value is None:
        return "null"
    escapes = {"\\": "\\\\", '"': '\\"', "\n": "\\n", "\r": "\\r", "\t": "\\t"}
    parts = ['"']
    for character in value:
        if character in escapes:
            parts.append(escapes[character])
        elif ord(character) < 0x20 or 0x7f <= ord(character) <= 0x9f:
            parts.append(f"\\u{ord(character):04x}")
        else:
            parts.append(character)
    parts.append('"')
    return "".join(parts)


def _source(value: object, roots: list) -> str | None:
    if value is None:
        return None
    source = _object(value, "source")
    name = _string(source.get("name"), "source name")
    kind = _string(source.get("type"), "source type")
    if "location" not in source:
        raise ValueError("missing source location")
    location = source["location"]
    if location is None:
        return f"{name} ({kind})"
    location = _object(location, "source location")
    if set(location) == {"root", "path"}:
        root = _index(location["root"], "source root")
        if root >= len(roots):
            raise ValueError("source refers to missing root")
        relative = _string(location["path"], "source path", empty=True)
        if "\\" in relative or relative.startswith("/") or any(part in (".", "..") for part in relative.split("/")):
            raise ValueError("source path must be relative without traversal")
        suffix = quote(relative, safe="/:@&=+$,;!~*'()-._")
        path = f"//{root}//{suffix}" if len(roots) != 1 else f"//{suffix}"
    elif set(location) == {"uri"}:
        path = _string(location["uri"], "source URI")
        try:
            uri = urlsplit(path)
        except ValueError as error:
            raise ValueError("invalid file URI") from error
        if uri.scheme.lower() != "file" or not re.match(r"(?i)^file:/", path) or not uri.path.startswith("/") or re.search(r"%(?![0-9a-fA-F]{2})", path):
            raise ValueError("source URI must be an absolute file URI")
    else:
        raise ValueError("source location must contain root and path or URI")
    return f"{name} {kind} {path}"


def _location(value: object) -> str:
    if value is None:
        return "null"
    location = _object(value, "site location")
    numbers = [_index(location.get(key), key) for key in ("begin_line", "begin_column", "end_line", "end_column")]
    begin_line, begin_column, end_line, end_column = numbers
    if begin_line < 1 or end_line < 1 or (end_line, end_column) < (begin_line, begin_column):
        raise ValueError("invalid site location range")
    return f"{begin_line}:{begin_column}-{end_line}:{end_column}"


def render_depx(document: object) -> str:
    """Validate schema 1 and return the corresponding version 2 text trace."""
    document = _object(document, "document")
    if document.get("format") != "depx" or type(document.get("version")) is not int or document["version"] != 1:
        raise ValueError("expected depx schema version 1")
    roots = _array(document.get("roots"), "roots")
    for root in roots:
        _string(root, "root")
        if not re.match(r"^(?:/|[A-Za-z]:[\\/])", root):
            raise ValueError("root must be an absolute path")
    profiles = _array(document.get("profiles"), "profiles")
    dependencies = _array(document.get("dependencies"), "dependencies")
    if not profiles:
        raise ValueError("document contains no profiles")
    lines = ["depx 2"] + [f"root {i} {_quoted(root)}" for i, root in enumerate(roots)]
    names = set()
    for profile in profiles:
        profile = _object(profile, "profile")
        name = _string(profile.get("name"), "profile name")
        if name in names:
            raise ValueError("duplicate profile name")
        names.add(name)
        lines.append(f"profile {_quoted(name)}")
        sites = []
        for index, site in enumerate(_array(profile.get("sites"), "sites")):
            site = _object(site, "site")
            fields = []
            for key in ("caller", "target"):
                if key not in site:
                    raise ValueError(f"missing site {key}")
                template = site[key]
                if template is None:
                    fields.append((None, None))
                else:
                    template = _object(template, key)
                    template_name = _string(template.get("name"), f"{key} name")
                    if "source" not in template:
                        raise ValueError(f"missing {key} source")
                    fields.append((template_name, _source(template["source"], roots)))
            if "location" not in site:
                raise ValueError("missing site location")
            location = _location(site["location"])
            (caller, caller_path), (target, target_path) = fields
            site_names = f" caller={_quoted(caller)} target={_quoted(target)}"
            sites.append((site_names, location))
            lines.append(f"  site {index}{site_names} caller-path={_quoted(caller_path)} target-path={_quoted(target_path)} location={location}")
        lines.append("  occurrences")
        depths = []
        statuses = []
        active = []
        positions = {}
        for index, occurrence in enumerate(_array(profile.get("occurrences"), "occurrences")):
            occurrence = _object(occurrence, "occurrence")
            site = _index(occurrence.get("site"), "occurrence site")
            if site >= len(sites):
                raise ValueError("occurrence refers to unknown site")
            if "parent" not in occurrence:
                raise ValueError("missing occurrence parent")
            parent = occurrence["parent"]
            if parent is None:
                active.clear()
                positions.clear()
                depth = 0
            else:
                parent = _index(parent, "occurrence parent")
                if parent >= index or parent not in positions:
                    raise ValueError("occurrence parent must be an earlier active ancestor")
                if statuses[parent] in ("skipped_unique", "skipped_declaration"):
                    raise ValueError("skipped occurrence cannot have children")
                while len(active) > positions[parent] + 1:
                    del positions[active.pop()]
                depth = depths[parent] + 1
            status = occurrence.get("status")
            if status not in ("executed", "skipped_unique", "skipped_declaration", "create"):
                raise ValueError("unknown occurrence status")
            depths.append(depth)
            statuses.append(status)
            positions[index] = len(active)
            active.append(index)
            site_names, location = sites[site]
            kind = "create" if status == "create" else "include"
            lines.append(f"{'  ' * (depth + 2)}{kind} {index} site={site}{site_names} location={location} status={status.replace('_', '-')}")
    lines.append("dependencies")
    for dependency in dependencies:
        source = _source(dependency, roots)
        if source is None:
            raise ValueError("dependency must be a source")
        lines.append(f"  file {_quoted(source)}")
    return "\n".join(lines) + "\n"


def main() -> int:
    """Read, validate and render before opening an output file."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", nargs="?", default="-", help="input DEPX file, or - for stdin")
    parser.add_argument("-o", "--output", default="-", help="output file, or - for stdout")
    args = parser.parse_args()
    try:
        if args.input == "-":
            data = sys.stdin.buffer.read()
        else:
            data = Path(args.input).read_bytes()
            if args.input.endswith(".gz"):
                data = gzip.decompress(data)
        document = json.loads(data.decode("utf-8-sig"), parse_constant=lambda value: (_ for _ in ()).throw(ValueError("invalid JSON constant")))
        text = render_depx(document)
        output = text.encode("utf-8")
        if args.output == "-":
            sys.stdout.buffer.write(output)
        elif args.output.endswith(".gz"):
            Path(args.output).write_bytes(gzip.compress(output))
        else:
            Path(args.output).write_bytes(output)
        return 0
    except (ValueError, OSError, EOFError, UnicodeError, RecursionError, zlib.error) as error:
        print(f"depx2text: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
