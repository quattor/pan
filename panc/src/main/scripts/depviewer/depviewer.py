"""Open a dependency trace in the adjacent viewer over protected loopback HTTP."""

import argparse
import io
import json
import logging
import os
import secrets
import subprocess
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import BinaryIO, cast

LOGGER = logging.getLogger("depviewer")
CHUNK_SIZE = 64 * 1024
TRACE_SUFFIXES = (".depx", ".depx.gz")


def build_inventory(inputs: list[Path]) -> tuple[list[Path], list[dict[str, str]]]:
    """Snapshot readable trace paths without following discovered directory links."""
    paths: set[Path] = set()
    LOGGER.info("Trace discovery started with %s inputs", len(inputs))

    def add_trace(path: Path, explicit: bool) -> None:
        """Validate readability without reading or parsing trace contents."""
        try:
            if not path.name.endswith(TRACE_SUFFIXES):
                raise ValueError("trace must end in .depx or .depx.gz")
            resolved = path.resolve(strict=True)
            if not resolved.is_file():
                raise ValueError("trace is not a regular file")
            with resolved.open("rb"):
                pass
            paths.add(resolved)
        except (OSError, RuntimeError, ValueError) as error:
            if explicit:
                raise ValueError(
                    "explicit input trace is invalid or not readable; expected .depx or .depx.gz"
                ) from error
            LOGGER.warning("Discovered trace omitted (%s)", type(error).__name__)

    def walk_error(error: OSError) -> None:
        """Report inaccessible discovered directories without leaking path names."""
        LOGGER.warning("Discovered directory omitted (%s)", type(error).__name__)

    for path in inputs:
        try:
            if path.is_dir():
                if path.is_symlink():
                    raise ValueError(
                        "explicit input directory symlinks are not followed"
                    )
                with os.scandir(path):
                    pass
                for root, directories, filenames in os.walk(
                    path, onerror=walk_error, followlinks=False
                ):
                    directories[:] = sorted(
                        name
                        for name in directories
                        if not (Path(root) / name).is_symlink()
                    )
                    for name in sorted(filenames):
                        if name.endswith(TRACE_SUFFIXES):
                            add_trace(Path(root) / name, explicit=False)
            else:
                add_trace(path, explicit=True)
        except OSError as error:
            raise ValueError("explicit input directory is not readable") from error
    traces = sorted(paths)
    if not traces:
        raise ValueError("no readable dependency traces found in inputs")
    base = Path(os.path.commonpath([str(path.parent) for path in traces]))
    catalog = [
        {"id": str(index), "name": path.relative_to(base).as_posix()}
        for index, path in enumerate(traces)
    ]
    LOGGER.info("Trace discovery completed: %s unique traces", len(traces))
    return traces, catalog


def make_handler(
    page: Path, traces: list[Path], catalog: list[dict[str, str]], token: str
) -> type[BaseHTTPRequestHandler]:
    """Build a handler exposing only inventory IDs under a secret route prefix."""
    trace_routes = {
        f"/{token}/trace/{index}": path for index, path in enumerate(traces)
    }
    catalog_bytes = json.dumps({"traces": catalog}).encode("utf-8")

    class ViewerHandler(BaseHTTPRequestHandler):
        """Serve allowlisted assets while enforcing the loopback origin boundary."""

        def log_message(self, format: str, *args: object) -> None:
            """Suppress HTTP logs, which otherwise expose the secret route token."""

        def do_GET(self) -> None:
            """Validate request metadata and stream the selected asset unchanged."""
            host = f"127.0.0.1:{cast(ThreadingHTTPServer, self.server).server_port}"
            if (
                self.headers.get_all("Host") != [host]
                or self.headers.get_all("Origin", [f"http://{host}"])
                != [f"http://{host}"]
                or self.headers.get_all("Sec-Fetch-Site", ["same-origin"])
                not in (["same-origin"], ["none"])
            ):
                self.send_error(403)
                return
            if self.path == f"/{token}/depviewer.html":
                asset, content_type = page, "text/html; charset=utf-8"
            elif self.path == f"/{token}/catalog":
                try:
                    self.send_response(200)
                    self.asset_headers(
                        "application/json; charset=utf-8", len(catalog_bytes)
                    )
                    self.stream(io.BytesIO(catalog_bytes))
                except (BrokenPipeError, ConnectionResetError):
                    LOGGER.debug("Viewer client disconnected during catalog transfer")
                return
            elif self.path in trace_routes:
                asset, content_type = (
                    trace_routes[self.path],
                    "application/octet-stream",
                )
            else:
                self.send_error(404)
                return
            try:
                with asset.open("rb") as source:
                    self.send_response(200)
                    self.asset_headers(content_type, os.fstat(source.fileno()).st_size)
                    self.stream(source)
            except (BrokenPipeError, ConnectionResetError):
                LOGGER.debug("Viewer client disconnected during asset transfer")
            except OSError as error:
                LOGGER.error("Viewer asset transfer failed (%s)", type(error).__name__)
                self.close_connection = True

        def asset_headers(self, content_type: str, size: int) -> None:
            """Apply shared no-store and browser safety headers."""
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(size))
            self.send_header("Cache-Control", "no-store")
            self.send_header("Referrer-Policy", "no-referrer")
            self.send_header("X-Content-Type-Options", "nosniff")
            self.end_headers()

        def stream(self, source: BinaryIO) -> None:
            """Copy an asset in bounded chunks without decoding gzip input."""
            while chunk := source.read(CHUNK_SIZE):
                self.wfile.write(chunk)

        def do_POST(self) -> None:
            """Reject unsupported methods without reading request bodies."""
            self.send_error(405)
            self.close_connection = True

        do_HEAD = do_POST
        do_PUT = do_POST
        do_DELETE = do_POST
        do_OPTIONS = do_POST
        do_PATCH = do_POST

    return ViewerHandler


def main() -> int:
    """Validate the trace, launch the browser, and serve until interrupted."""
    logging.basicConfig(level=logging.INFO, format="depviewer: %(message)s")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "paths",
        type=Path,
        nargs="+",
        help="trace.depx, trace.depx.gz, or recursive directory",
    )
    args = parser.parse_args()
    try:
        traces, catalog = build_inventory(args.paths)
    except ValueError as error:
        parser.error(str(error))
    page = Path(__file__).resolve().with_name("depviewer.html")
    for asset, label in ((page, "viewer page"),):
        try:
            if not asset.is_file():
                parser.error(f"{label} is missing or is not a regular file")
            with asset.open("rb"):
                pass
        except OSError:
            parser.error(f"{label} is not readable")
    token = secrets.token_urlsafe(32)
    try:
        with ThreadingHTTPServer(
            ("127.0.0.1", 0), make_handler(page, traces, catalog, token)
        ) as server:
            LOGGER.info("Viewer server started on loopback port %s", server.server_port)
            url = f"http://127.0.0.1:{server.server_port}/{token}/depviewer.html"
            try:
                subprocess.run(
                    ["xdg-open", url],
                    check=True,
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
            except (OSError, subprocess.CalledProcessError) as error:
                LOGGER.error(
                    "Browser launch failed (%s); check xdg-open and your desktop browser",
                    type(error).__name__,
                )
                return 1
            LOGGER.info("Browser launched; press Ctrl+C to stop the viewer")
            server.serve_forever()
    except KeyboardInterrupt:
        return 0
    except OSError as error:
        LOGGER.error("Viewer server failed (%s)", type(error).__name__)
        return 1
    finally:
        LOGGER.info("Viewer server shutdown")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
