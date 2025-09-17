#!/usr/bin/env python3
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
from os import sep as os_sep
from os.path import dirname
import six
from colorama import Fore, Style, init as colorama_init
from prettytable import PrettyTable


class Line(object):
    def __init__(self, filename, number, text):
        self.filename = str(filename)
        self.number = int(number)
        self.text = str(text)
        self.problems = []

    def __repr__(self):
        return f"Line('{self.filename}', {self.number}, '{self.text}')"


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
            message (Message): A Message object describing the problem
        """
        self.start = int(start)
        self.end = int(end)
        self.message = message

    def diagnose(self):
        """Format a line of diagnosis markers from a range of character positions
        Returns:
            str: A line of marker characers aligned with the position of the problem
        """
        return (' ' * self.start) + ('^' * (self.end - self.start))


class Message:
    """A class representing a message describing one issue with a line of code."""
    def __init__(self, message_id, severity, text):
        """
        Parameters:
            id (str): A unique five character identifier for the specific issue
            severity (int): How serious the issue is from 1-3 (least serious - most serious)
            text (str): A description of what the issue is
        """
        self.id = str(message_id)
        self.severity = int(severity)
        self.text = str(text)

    def __str__(self):
        return '%s: %s' % (SEVERITY_TEXT[self.severity], self.text)


RS_COMMENT = r'(?:#|@{.*?})'

RE_STRING = re.compile(r'''('.*?'|".*?"(?<!\\"))''')
RE_PATH = re.compile(r'''^\s*(?:prefix)?\s*(?P<path>'\S+'|"\S+")''')

RE_FIRST_LINE = re.compile(r'^\s*(?:(?:declaration|unique|structure|object)\s+)?template (?:(?:[\S-]+/)+)?[\S-]+;$')
RE_MVN_TEMPLATE = re.compile(r'\$\{\S+\}')
RE_COMMENT = re.compile(RS_COMMENT)
RE_COMMENT_LINE = re.compile(r'^\s*' + RS_COMMENT + '.*$')
RE_ANNOTATION = re.compile(r'@\w*\s*{.*?}', re.S)
# Deal with heredoc as fake operator incl tag (so no bitshift). Will ignore it in the code.
#   The order here is important: the < at the end must remain at the end or linting heredocs will fail.
RE_OPERATOR = re.compile(r'([>=<!?]=|[>+*=/-]|<<\w+(?!;)|<)')
RE_HEREDOC = re.compile(r'<<(\w+);\s*$.*?\1$', re.S | re.M)

# Find usage and inclusion of components
RE_COMPONENT_INCLUDE = re.compile(r'^\s*[^#]?\s*include.*components/(?P<name>\w+)/config', re.M)
RE_COMPONENT_USE = re.compile(r'/software/components/(?P<name>\w+)/')

# Detect whether a file is part of the source tree of a component
RE_COMPONENT_SOURCE_FILE = re.compile(r'^/?(?:\S+/)?(?:core/components/|ncm-)(?P<name>\w+)/\S+$')

# Find where templates belonging to features have been included
RE_FEATURE_INCLUDE = re.compile(r'^\s*[^#]?\s*include.*(?P<name>features/\S+\w)', re.M)

LINE_LENGTH_LIMIT = 120

SEV_ADVICE = 1
SEV_WARNING = 2
SEV_ERROR = 3

SEVERITY_TEXT = {
    SEV_ADVICE: 'Advice',
    SEV_WARNING: 'Warning',
    SEV_ERROR: 'Error',
}

SEVERITY_VALUE_MAP = dict(zip(SEVERITY_TEXT.values(), SEVERITY_TEXT.keys()))

# Simple regular-expression based checks that will be performed against all non-ignored lines
# Every pattern must provide a single capturing group named "error"
LINE_PATTERNS = {
    Message("LP001", SEV_ADVICE, "Indentation should be a multiple of four spaces"):
        re.compile(r'^(?!( {4})*(\S|$))(?P<error>\s*)'),

    Message("LP002", SEV_ADVICE, "Spaces should be used instead of tabs"):
        re.compile(r'(?P<error>\t+)'),

    Message("LP003", SEV_ADVICE, "Trailing whitespace"):
        re.compile(r'(?P<error>\s+$)'),

    Message("LP004", SEV_WARNING, "Use dicts instead of nlists"):
        re.compile(r'\b(?P<error>(?:is_)?nlist)\s*\('),

    Message("LP005", SEV_ADVICE, "Include statements no longer need curly braces"):
        re.compile(r'''include\s+(?P<error>{[^;]+})'''),

    Message("LP006", SEV_ADVICE, f"Line is longer than {LINE_LENGTH_LIMIT} characters"):
        re.compile(r'''^(?!bind ).{0,%s}(?P<error>.*?)$''' % LINE_LENGTH_LIMIT),

    Message("LP007", SEV_ADVICE, "Commas should be followed by exactly one space"):
        re.compile(r'(?P<error>,(?:\S|\s{2,}))'),

    Message("LP008", SEV_ADVICE, "Whitespace before semicolon"):
        re.compile(r'(?P<error>\s+;)'),

    Message("LP009", SEV_ADVICE, "Semicolons should be followed exactly one space or end-of-line"):
        re.compile(r';(?P<error>(?:\S|\s{2,}))'),

    Message("LP010", SEV_ADVICE, "Global variables should be uppercase"):
        re.compile(r'variable\s+(?P<error>[\w]+)(?<=\S[a-z]\S)'),

    Message("LP011", SEV_ADVICE, "Global variables should be five or more characters"):
        re.compile(r'variable\s+(?P<error>\w{1,4})\b'),

    Message("LP012", SEV_WARNING, "Redundant use of format within error or debug call"):
        re.compile(r'(?:error|debug)\s*\(\s*(?P<error>format)\s*\('),
}

# Simple regular-expression based checks that will be performed against
# all strings on non-ignored lines that appear to be profile paths
# Every pattern must provide a single capturing group named "error"
PATH_PATTERNS = {
    Message("PP001", SEV_ADVICE, "Unnecessary trailing slash at end of profile path"):
        re.compile(r'''.(?P<error>/+)$''')
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
            elif op.startswith('<<') and len(op) > 2:
                # ignore fake heredoc operator
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
                message = Message('LC001', SEV_ADVICE, message_text)
                line.problems.append(Problem(start, end, message))

        return line

    @staticmethod
    def features_standalone(line, _):
        """If the current file looks like a feature, then check a line for includes of feature templates
        to ensure that they are children of the current feature"""
        message_text = 'Feature template includes a template which is not a child of the feature'

        file_path = line.filename.split(os_sep)
        try:
            file_feature = file_path.index('features')
        except ValueError:
            # Return immediately if this file isn't part of a feature
            return line

        if file_feature >= 0:
            file_path = file_path[file_feature:]
            includes = RE_FEATURE_INCLUDE.finditer(line.text)
            for include in includes:
                this_file = os_sep.join(file_path)
                this_dir = dirname(this_file)

                incl_file = include.group('name')
                incl_dir = dirname(incl_file)

                if not incl_dir.startswith(this_dir):
                    start, end = include.span('name')
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
        return u'%s +%d #%s' % (filename, line_number, message)
    return u'%s:%d: %s' % (filename, line_number, message)


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

    return u''.join([Fore.GREEN, text.rstrip('\n'), Fore.RESET])


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
    return u''.join(result).rstrip()


def print_diagnosis(diagnosis):
    """Format a line of diagnosis produced by diagnose() and/or merge_diagnoses()"""
    return u''.join([Fore.BLUE, diagnosis, Fore.RESET])


def debug_line(line):
    """Print debug information for a processed line of an input file"""
    if DEBUG:
        label = 'DEBUG: %04d %-12s |' % (line.number, '...')
        print(u''.join([Style.DIM, Fore.CYAN, label, Style.RESET_ALL, line.text.replace('\t', TAB_ARROW)]))


def debug_ignored_line(line):
    """Print debug information for an ignored line of an input file"""
    if DEBUG:
        label = 'DEBUG: %04d %-12s |' % (line.number, 'Ignored')
        print(u''.join([Fore.CYAN, Style.DIM, label, Fore.RESET, line.text.replace('\t', TAB_ARROW), Style.RESET_ALL]))


def debug_range(start, end, label, problem=False):
    """Print debug information referring to a range of characters in a single line"""
    if DEBUG:
        label = ('DEBUG: ^^^^ %-12s |' % label)
        diagnosis = diagnose(start, end)
        color = Fore.CYAN
        if problem:
            color = Fore.RED
        print(u''.join([Style.DIM, Fore.CYAN, label, Style.BRIGHT, color, diagnosis, Style.RESET_ALL]))


def diagnose(start, end):
    """Format a line of diagnosis markers from a range of character positions"""
    return (u' ' * start) + (u'^' * (end - start))


def print_report(line, vi=False):
    """Print a full report of all problems found with a single line of a processed file"""
    print(u'')
    messages = ', '.join(set([p.message.text for p in line.problems]))
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
    t = PrettyTable(['Filename'] + list(SEVERITY_VALUE_MAP) + ['Total'])
    t.align['Filename'] = 'l'
    t.align['Total'] = 'r'
    for severity in SEVERITY_VALUE_MAP:
        t.align[severity] = 'r'

    for filename, stats in problem_stats.items():
        if stats:
            t.add_row([filename] + stats + [sum(stats)])
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
            message_text = 'Component %s in use, but component config has not been included' % match.group('name')
            message = Message('CU001', SEV_WARNING, message_text)
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


def lint_line(line, components_included, first_line=False, allow_mvn_templates=False, suppress=SEV_ADVICE):
    """Run all lint checks against line and return any problems found."""
    debug_line(line)

    if first_line:
        first_line = False
        if not RE_FIRST_LINE.match(line.text):
            if not (RE_MVN_TEMPLATE.match(line.text) and allow_mvn_templates):
                line.problems.append(
                    Problem(0, len(line.text), Message(
                        'FL001',
                        SEV_ERROR,
                        'First non-comment line must be the template type and name',
                    ))
                )
    else:
        string_ranges = get_string_ranges(line)
        line = strip_trailing_comments(line, string_ranges)

        line.problems += check_line_component_use(line, components_included)
        line.problems += check_line_patterns(line, string_ranges)
        line.problems += check_line_paths(line)

        line = check_line_methods(line, string_ranges)

        # Filter out problems with a lower than specified severity
        line.problems = [p for p in line.problems if p.message.severity >= suppress]

    return (line, first_line)


def lint_file(filename, allow_mvn_templates=False, ignore_components=None, suppress=SEV_ADVICE):
    """Run lint checks against all lines of a file."""
    if ignore_components is None:
        ignore_components = []

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
    # add ignored components
    components_included.extend(ignore_components)

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
                suppress,
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
    parser.add_argument('--summary', action='store_true', help='Display a summary of problems')
    parser.add_argument('--allow_mvn_templates', action='store_true', help='Allow use of maven templates')
    parser.add_argument('--always_exit_success', action='store_true',
                        help='Always exit cleanly even if problems are found')
    parser.add_argument('--ignore-components', type=str,
                        help='List of component to ignore when checking included components')
    parser.add_argument('--features_standalone', action='store_true',
                        help='Check that features don\'t include other features')
    parser.add_argument(
        '--suppress',
        type=str,
        choices=SEVERITY_TEXT.values(),
        default='Advice',
        help='Only report problems of the provided level or above',
    )
    parser.add_argument(
        '--threshold',
        type=str,
        choices=SEVERITY_TEXT.values(),
        default='Advice',
        help='Only fail if problems of a certain level or above are found',
    )
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
    problem_max_severity = 0

    if not args.paths:
        print('No files were provided, not doing anything')
        return 0

    ignore_components = None
    if args.ignore_components:
        ignore_components = args.ignore_components.split(',')

    # This check is based on site-specific policy, so it is optional
    if not args.features_standalone:
        del LineChecks.features_standalone

    for path in args.paths:
        for filename in glob(path):
            file_problem_lines, file_problem_count = lint_file(filename, args.allow_mvn_templates, ignore_components, SEVERITY_VALUE_MAP[args.suppress])
            problem_lines += file_problem_lines
            problem_count += file_problem_count
            problem_stats[filename] = file_problem_count

            file_severities = [0]
            for line in file_problem_lines:
                for problem in line.problems:
                    file_severities.append(problem.message.severity)

            problem_stats[filename] = []
            for value in SEVERITY_TEXT:
                problem_stats[filename].append(file_severities.count(value))

            problem_max_severity = max(problem_max_severity, max(file_severities))

    for line in problem_lines:
        print_report(line, vi=args.vi)

    if args.table:
        print('\nProblem count per file:')
        print(filestats_table(problem_stats))

    if args.summary:
        print()
        print(f"\n{problem_count} problems found in {len(problem_lines)} lines")
        print('Highest severity problem found was %s' % (SEVERITY_TEXT[problem_max_severity]))

    if args.always_exit_success:
        return 0

    if problem_count and problem_max_severity >= SEVERITY_VALUE_MAP[args.threshold]:
        return 1

    return 0


if __name__ == '__main__':
    sys_exit(main())
