#!/usr/bin/env python
# coding=utf8
#
# Copyright 2016 Science & Technology Facilities Council
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Linter for the Pan language"""

from __future__ import print_function
import argparse
import re
from glob import glob
from sys import stdout, exit as sys_exit
from inspect import getmembers, isfunction
import six
from colorama import Fore, Style, init as colorama_init
from prettytable import PrettyTable


class Line(object):
    def __init__(self, filename, number, text):
        self.filename = str(filename)
        self.number = int(number)
        self.text = str(text)
        self.problems = []


class Problem(object):
    """A class representing the abstract concept of a problem with a line of code.

    Problems have the ability to self-diagnose.
    Problems are a property of Lines, rather than the other way around.
    """
    def __init__(self, start, end, message):
        """
        Parameters:
            start (int): Position of the first character of the problem
            end (int): Position of the last character of the problem
            message (str): A description of the problem
        """
        self.start = int(start)
        self.end = int(end)
        self.message = str(message)

    def diagnose(self):
        """Format a line of diagnosis markers from a range of character positions
        Returns:
            str: A line of marker characers aligned with the position of the problem
        """
        return (' ' * self.start) + ('^' * (self.end - self.start))


RS_COMMENT = r'(?:#|@{.*?})'

RE_STRING = re.compile(r'''('.*?'|".*?"(?<!\\"))''')
RE_PATH = re.compile(r'''^\s*(?:prefix)?\s*(?P<path>'\S+'|"\S+")''')

RE_FIRST_LINE = re.compile(r'^\s*(?:(?:declaration|unique|structure|object)\s+)?template (?:(?:[\S-]+/)+)?[\S-]+;$')
RE_MVN_TEMPLATE = re.compile(r'\$\{\S+\}')
RE_COMMENT = re.compile(RS_COMMENT)
RE_COMMENT_LINE = re.compile(r'^\s*' + RS_COMMENT + '.*$')
RE_ANNOTATION = re.compile(r'@\w*{.*?}', re.S)
RE_OPERATOR = re.compile(r'([>=<!?]=|[<>+*=/-])')
RE_HEREDOC = re.compile(r'<<(\w+);\s*$.*?\1$', re.S | re.M)

# Find usage and inclusion of components
RE_COMPONENT_INCLUDE = re.compile(r'^\s*[^#]?\s*include.*components/(?P<name>\w+)/config', re.M)
RE_COMPONENT_USE = re.compile(r'/software/components/(?P<name>\w+)/')

# Detect whether a file is part of the source tree of a component
RE_COMPONENT_SOURCE_FILE = re.compile(r'^/?(?:\S+/)?(?:core/components/|ncm-)(?P<name>\w+)/\S+$')

LINE_LENGTH_LIMIT = 120

# Simple regular-expression based checks that will be performed against all non-ignored lines
# Every pattern must provide a single capturing group named "error"
LINE_PATTERNS = {
    "Indentation should be a multiple of four spaces": re.compile(r'^(?!( {4})*(\S|$))(?P<error>\s*)'),
    "Spaces should be used instead of tabs": re.compile(r'(?P<error>\t+)'),
    "Trailing whitespace": re.compile(r'(?P<error>\s+$)'),
    "Use dicts instead of nlists": re.compile(r'\b(?P<error>(?:is_)?nlist)\s*\('),
    "Include statements no longer need curly braces": re.compile(r'''include\s+(?P<error>{[^;]+})'''),
    "Line is longer than %s characters" % LINE_LENGTH_LIMIT:
    re.compile(r'''^.{0,%s}(?P<error>.*?)$''' % LINE_LENGTH_LIMIT),
    "Commas should be followed by exactly one space": re.compile(r'(?P<error>,(?:\S|\s{2,}))'),
    "Whitespace before semicolon": re.compile(r'(?P<error>\s+;)'),
    "Semicolons should be followed exactly one space or end-of-line": re.compile(r';(?P<error>(?:\S|\s{2,}))'),
    "Global variables should be uppercase": re.compile(r'variable\s+(?P<error>[\w]+)(?<=\S[a-z]\S)'),
    "Global variables should be five or more characters": re.compile(r'variable\s+(?P<error>\w{1,4})\b'),
    "Redundant use of format within error or debug call": re.compile(r'(?:error|debug)\s*\(\s*(?P<error>format)\s*\('),
}

# Simple regular-expression based checks that will be performed against
# all strings on non-ignored lines that appear to be profile paths
# Every pattern must provide a single capturing group named "error"
PATH_PATTERNS = {
    "Unnecessary trailing slash at end of profile path": re.compile(r'''.(?P<error>/+)$'''),
}

TAB_ARROW = u'\u2192'

DEBUG = False


class LineChecks:
    """More complex single line checks that require some logic to implement their checks"""

    @staticmethod
    def whitespace_around_operators(line, string_ranges):
        """Check a line of text to ensure that there is whitespace before and after all operators"""
        operators = RE_OPERATOR.finditer(line.text)

        for operator in operators:
            op = operator.group(1)
            # end: not in match
            start, end = operator.span(1)

            chars_before = line.text[:start]
            chars_after = line.text[end:]

            # simple statement text in square brackets; if any
            # the "simple" pattern: letters, digtis, +/- operator, minus sign
            #    also whitespace, those are invalid
            sqb_simple = r'\s\w\d+-'

            sqb_before = re.search(r'[\[]([' + sqb_simple + ']*)$', chars_before)
            sqb_after = re.search(r'^([' + sqb_simple + ']*)[]]', chars_after)

            valid = True
            if inside_string(start, end, string_ranges):
                continue
            elif op == '-' and (
                    (
                        re.search(r'[^\w\s)=]\s*$', chars_before) and re.search(r'^\s*\d+\s*[^\w\s]', chars_after)
                    ) or (
                        re.search(r'=\s*$', chars_before) and re.search(r'^\s*\d+', chars_after)
                    )
            ):
                # -\d not preceded or followed by eg variable name
                # first or:
                #   use [^\w\s] instead of \W to avoid backtracking winning from greedy *
                #   search(r'\W\s*$', "x ") gives a match
                #     chars_before: allow ')': end of cuntion call
                #                   = handled in second or
                # second or:
                #   no space directly after assignment
                if re.search(r'^\s', chars_after):
                    valid = False
                    message_text = 'Unwanted space after minus sign (not operator)'
                    end += 2
            elif op in ('-', '+', ) and sqb_before and sqb_after:
                # something simple in square brackets
                in_brackets = sqb_before.group(1) + op + sqb_after.group(1)
                reg = re.search(r'\s', in_brackets)
                if reg:
                    valid = False
                    message_text = 'Unwanted space in simple expression in square brackets'
                    start = sqb_before.start(1) + reg.start(0)
                    end = start + 1

            else:
                reason = 'Missing'
                messages = set()
                if chars_before and chars_before[-1] not in (' ', '\t'):
                    valid = False
                    messages.add('before')
                    start -= 1
                if chars_after and chars_after[0] not in (' ', '\t'):
                    valid = False
                    messages.add('after')
                    end += 1
                messages = sorted(list(messages), key=None, reverse=True)
                message_text = '%s space %s operator' % (reason, ' and '.join(messages))

            if not valid:
                debug_range(start, end, 'WS Operator', True)
                line.problems.append(Problem(start, end, message_text))

        return line


def inside_string(i, j, string_ranges):
    """Returns true if the range described by i and j is contained within a range in the list string_ranges"""
    for s, e in string_ranges:
        if i >= s and j <= e:
            return True
    return False


def print_fileinfo(filename, line_number, message, vi=False):
    """Return a formatted string with filename, line_number and message.

    Keyword arguments:
    vi -- output line numbers in a format suitable for passing to editors such as vi (default False)
    """
    if vi:
        return '%s +%d #%s' % (filename, line_number, message)
    return '%s:%d: %s' % (filename, line_number, message)


def print_line(text):
    """Return a formatted line of text, replacing tabs with a visible character

    If stdout is a tty and claims to support UTF-8 encoding, a unicode rightwards arrow (u2192) will be used for tabs,
    otherwise a space character ( ) will be used.
    This keeps the character counts in line with fixed-width columns while still making tabs distinguishable in output.
    """
    if stdout.isatty() and stdout.encoding == 'UTF-8':
        text = text.replace('\t', TAB_ARROW)
    else:
        text = text.replace('\t', ' ')

    return ''.join([Fore.GREEN, text.rstrip('\n'), Fore.RESET])


def merge_diagnoses(args):
    """Merge lines of diagnosis produced by diagnose()"""
    if not args:
        return ''

    args = [a.rstrip() for a in args]
    result = [' '] * max([len(a) for a in args])

    for text in args:
        for i, c in enumerate(text):
            if c != ' ':
                result[i] = c
    return ''.join(result).rstrip()


def print_diagnosis(diagnosis):
    """Format a line of diagnosis produced by diagnose() and/or merge_diagnoses()"""
    return ''.join([Fore.BLUE, diagnosis, Fore.RESET])


def debug_line(line):
    """Print debug information for a processed line of an input file"""
    if DEBUG:
        label = 'DEBUG: %04d %-12s |' % (line.number, '...')
        print(''.join([Style.DIM, Fore.CYAN, label, Style.RESET_ALL, line.text.replace('\t', TAB_ARROW)]))


def debug_ignored_line(line):
    """Print debug information for an ignored line of an input file"""
    if DEBUG:
        label = 'DEBUG: %04d %-12s |' % (line.number, 'Ignored')
        print(''.join([Fore.CYAN, Style.DIM, label, Fore.RESET, line.text.replace('\t', TAB_ARROW), Style.RESET_ALL]))


def debug_range(start, end, label, problem=False):
    """Print debug information referring to a range of characters in a single line"""
    if DEBUG:
        label = ('DEBUG: ^^^^ %-12s |' % label)
        diagnosis = diagnose(start, end)
        color = Fore.CYAN
        if problem:
            color = Fore.RED
        print(''.join([Style.DIM, Fore.CYAN, label, Style.BRIGHT, color, diagnosis, Style.RESET_ALL]))


def diagnose(start, end):
    """Format a line of diagnosis markers from a range of character positions"""
    return (' ' * start) + ('^' * (end - start))


def print_report(line, vi=False):
    """Print a full report of all problems found with a single line of a processed file"""
    print('')
    messages = ', '.join(set([p.message for p in line.problems]))
    print(print_fileinfo(line.filename, line.number, messages, vi=vi))
    print(print_line(line.text))
    print(print_diagnosis(merge_diagnoses([p.diagnose() for p in line.problems])))


def get_string_ranges(line):
    """Find all ranges of strings within a single line of text"""
    string_ranges = []
    strings = RE_STRING.finditer(line.text)

    if strings:
        for string in strings:
            s, e = string.span()
            debug_range(s, e, 'String Range')
            string_ranges.append((s, e))

    return string_ranges


def filestats_table(problem_stats):
    """Return a formatted table of problem counts per file"""
    t = PrettyTable(['Filename', 'Problems'])
    t.align['Filename'] = 'l'
    t.align['Problems'] = 'r'
    for filename, problem_count in problem_stats.iteritems():
        if problem_count > 0:
            t.add_row([filename, problem_count])
    t.sortby = 'Filename'
    return t


def find_annotation_blocks(text):
    """Find multi-line annotation blocks in a block of text"""
    result = []
    annotations = RE_ANNOTATION.finditer(text)
    for annotation in annotations:
        start_char, end_char = annotation.span()
        start_line = text[:start_char].count('\n') + 1
        end_line = start_line + text[start_char:end_char].count('\n')
        result += range(start_line, end_line + 1)
    return result


def find_heredoc_blocks(text):
    """Find multi-line heredoc blocks in a block of text"""
    result = []
    heredocs = RE_HEREDOC.finditer(text)
    for heredoc in heredocs:
        start_char, end_char = heredoc.span()
        start_line = text[:start_char].count('\n') + 1
        end_line = start_line + text[start_char:end_char].count('\n')
        # start_line incl the line with the <<
        # but we want to validate that line (eg for assignment), so another +1
        # the +1 for end_line is the usual range-does-not-incl-end
        result += range(start_line + 1, end_line + 1)
    return result


def strip_trailing_comments(line, string_ranges):
    """Remove comments from the end of a line, ignoring anything within specified string ranges"""
    for comment in RE_COMMENT.finditer(line.text):
        # Does the candidate comment start inside a string?
        # If so, it's not really a comment.
        if not inside_string(comment.start(), comment.start() + 1, string_ranges):
            debug_range(comment.start(), comment.end(), 'Comment', False)
            line.text = line.text[:comment.start()].rstrip()
    return line


def check_line_component_use(line, components_included):
    """
    Check a line for usage of a component, flag a problem if any component
    is not in the list of included components.
    """
    problems = []

    for match in RE_COMPONENT_USE.finditer(line.text):
        if match.group('name') not in components_included:
            start, end = match.span('name')
            debug_range(start, end, 'ComponentUse', True)
            message = 'Component %s in use, but component config has not been included' % match.group('name')
            problems.append(Problem(start, end, message))
    return problems


def check_line_patterns(line, string_ranges):
    """Check line against regular expressions in LINE_PATTERNS, ignoring code within specified string ranges."""
    problems = []

    for message, pattern in six.iteritems(LINE_PATTERNS):
        matches = pattern.finditer(line.text)
        for match in matches:
            if match and match.group('error'):
                start, end = match.span('error')
                debug_range(start, end, 'LineRE Match', True)
                if not inside_string(start, end, string_ranges):
                    problems.append(Problem(start, end, message))
    return problems


def check_line_paths(line):
    """
    If a line contains a profile path on the left hand side,
    check code within it against regular expressions in PATH_PATTERNS.
    """
    problems = []

    path_match = RE_PATH.match(line.text)
    if path_match:
        path_start, path_end = path_match.span('path')
        path_string = line.text[path_start + 1:path_end - 1]
        debug_range(path_start, path_end, 'Path String', False)
        for message, pattern in six.iteritems(PATH_PATTERNS):
            matches = pattern.finditer(path_string)
            for match in matches:
                if match and match.group('error'):
                    start, end = match.span('error')
                    start += path_start + 1
                    end += path_start + 1
                    debug_range(start, end, 'PathRE Match', True)
                    problems.append(Problem(start, end, message))

    return problems


def check_line_methods(line, string_ranges):
    """Run checks defined as methods of LineChecks against line, ignoring code within specified string ranges."""
    for _, check_method in getmembers(LineChecks(), predicate=isfunction):
        line = check_method(line, string_ranges)

    return line


def lint_line(line, components_included, first_line=False, allow_mvn_templates=False):
    """Run all lint checks against line and return any problems found."""
    debug_line(line)

    if first_line:
        first_line = False
        if not RE_FIRST_LINE.match(line.text):
            if not (RE_MVN_TEMPLATE.match(line.text) and allow_mvn_templates):
                line.problems.append(
                    Problem(0, len(line.text), 'First non-comment line must be the template type and name')
                )
    else:
        string_ranges = get_string_ranges(line)
        line = strip_trailing_comments(line, string_ranges)

        line.problems += check_line_component_use(line, components_included)
        line.problems += check_line_patterns(line, string_ranges)
        line.problems += check_line_paths(line)

        line = check_line_methods(line, string_ranges)

    return (line, first_line)


def lint_file(filename, allow_mvn_templates=False):
    """Run lint checks against all lines of a file."""
    problem_lines = []
    file_problem_count = 0

    with open(filename) as f:
        raw_text = f.read()

    first_line = True
    ignore_lines = []

    # Identify annotation blocks and exclude them from linting
    # We will need special linting rules for these
    ignore_lines += find_annotation_blocks(raw_text)

    # Identify heredoc blocks and exclude them from linting
    ignore_lines += find_heredoc_blocks(raw_text)

    # Get list of all component configs included in template
    components_included = RE_COMPONENT_INCLUDE.findall(raw_text)

    # Is the current file part of the source tree of a component?
    # If so, regard the component config as being included
    components_included += RE_COMPONENT_SOURCE_FILE.findall(filename)

    for line_number, line_text in enumerate(raw_text.splitlines(), start=1):
        line = Line(filename, line_number, line_text.rstrip('\n'))

        if line.text and line.number not in ignore_lines and not RE_COMMENT_LINE.match(line.text):
            line, first_line = lint_line(
                line,
                components_included,
                first_line,
                allow_mvn_templates,
            )

            if line.problems:
                problem_lines.append(line)
                file_problem_count += len(line.problems)
        else:
            debug_ignored_line(line)

    return (problem_lines, file_problem_count)


def main():
    """Main function"""
    parser = argparse.ArgumentParser(description='Linter for the pan language')
    parser.add_argument('paths', metavar='PATH', type=str, nargs='*', help='Paths of files to check')
    parser.add_argument('--vi', action='store_true', help='Output line numbers in a vi option style')
    parser.add_argument('--table', action='store_true', help='Display a table of per-file problem stats')
    parser.add_argument('--allow_mvn_templates', action='store_true', help='Allow use of maven templates')
    parser.add_argument('--always_exit_success', action='store_true',
                        help='Always exit cleanly even if problems are found')
    group_output = parser.add_mutually_exclusive_group()
    group_output.add_argument('--debug', action='store_true', help='Enable debug output')
    group_output.add_argument('--ide', action='store_true', help='Output machine-readable results for use by IDEs')
    args = parser.parse_args()

    # Only output colors sequences if the output is a terminal
    colorama_init(strip=(not stdout.isatty()) or args.ide)
    global DEBUG
    DEBUG = args.debug

    problem_count = 0
    problem_lines = []
    problem_stats = {}

    if not args.paths:
        print('No files were provided, not doing anything')
        return 0

    for path in args.paths:
        for filename in glob(path):
            file_problem_lines, file_problem_count = lint_file(filename, args.allow_mvn_templates)
            problem_lines += file_problem_lines
            problem_count += file_problem_count
            problem_stats[filename] = file_problem_count

    for line in problem_lines:
        print_report(line, vi=args.vi)

    if args.table:
        print('\nProblem count per file:')
        print(filestats_table(problem_stats))

    print('\n%d problems found in %d lines' % (problem_count, len(problem_lines)))

    if args.always_exit_success:
        return 0

    if problem_count:
        return 1

    return 0


if __name__ == '__main__':
    sys_exit(main())
