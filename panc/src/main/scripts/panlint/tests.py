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

import glob
import unittest
from sys import argv
from os.path import basename, dirname, join
from io import StringIO
import mock
import six

import panlint


class TestPanlint(unittest.TestCase):
    def setUp(self):
        self.maxDiff = None
        self.longMessage = True

    def _assert_lint_line(self, input_line, input_diagnoses, input_messages, input_problems, input_first_line=False):
        """For a given line of code, assert that the full lint output matches expectations

        Parameters:
            input_line (panlint.Line): Line of source code to be linted
            input_diagnoses (list of str): Expected lines of diagnosis markers
            input_messages (list of Message): Expected problem descriptions
            input_problems (int): Expected number of problems
            input_first_line (bool): Whether this line should be considered the first line of a file (defaults to False)
        """
        input_diagnoses.sort()

        result_line, result_first_line = panlint.lint_line(input_line, [], input_first_line)
        self.assertEqual(len(result_line.problems), input_problems)

        result_diagnoses = [p.diagnose() for p in result_line.problems]
        result_diagnoses.sort()

        for d1, d2 in zip(input_diagnoses, result_diagnoses):
            self.assertEqual(d1, d2)

        # If messages is set to None, ignore the contents and just check that is not an empty set
        if input_messages is None:
            for p in result_line.problems:
                self.assertNotEqual(p.message.text, '')
        else:
            input_messages.sort()
            result_messages = [p.message.text for p in result_line.problems]
            result_messages.sort()
            for m1, m2 in zip(input_messages, result_messages):
                self.assertEqual(m1, m2)

        # first_line must ALWAYS be False when returned
        self.assertEqual(result_first_line, False)

    def test_message_ids(self):
        line_pattern_ids = [m.id for m in panlint.LINE_PATTERNS.keys()]
        line_pattern_ids.sort()
        for p in line_pattern_ids:
            self.assertRegex(p, r'LP\d{3}', f'Format of line pattern message ID {p}')

        path_pattern_ids = [m.id for m in panlint.PATH_PATTERNS.keys()]
        path_pattern_ids.sort()
        for p in path_pattern_ids:
            self.assertRegex(p, r'PP\d{3}', f'Format of path pattern message ID {p}')

        all_pattern_ids = line_pattern_ids + path_pattern_ids
        all_pattern_ids.sort()
        unique_pattern_ids = list(set(all_pattern_ids))
        unique_pattern_ids.sort()
        self.assertEqual(unique_pattern_ids, all_pattern_ids)

    def test_diagnose(self):
        dummy_message = panlint.Message('', 0, '')
        self.assertEqual(panlint.Problem(0, 0, dummy_message).diagnose(), '')
        self.assertEqual(panlint.Problem(0, 4, dummy_message).diagnose(), '^^^^')
        self.assertEqual(panlint.Problem(2, 8, dummy_message).diagnose(), '  ^^^^^^')
        self.assertEqual(panlint.Problem(7, 7, dummy_message).diagnose(), '       ')
        self.assertEqual(panlint.Problem(3, -2, dummy_message).diagnose(), '   ')

    def test_print_diagnosis(self):
        FORMAT = '\x1b[34m%s\x1b[39m'
        self.assertEqual(panlint.print_diagnosis(''), FORMAT % '')
        self.assertEqual(panlint.print_diagnosis('so many words'), FORMAT % 'so many words')

    def test_get_string_ranges(self):
        self.assertEqual(
            panlint.get_string_ranges(panlint.Line('', 1, '''there is a "string" in here''')),
            [(11, 19)],
        )
        self.assertEqual(
            panlint.get_string_ranges(panlint.Line('', 1, '''"string" + 'string' + something''')),
            [(0, 8), (11, 19)],
        )

    def test_merge_diagnoses(self):
        diag1 = ' ^'
        diag2 = '       ^^^'
        merged = ' ^     ^^^'
        self.assertEqual(panlint.merge_diagnoses([]), '')
        self.assertEqual(panlint.merge_diagnoses([diag1]), diag1)
        self.assertEqual(panlint.merge_diagnoses([diag2]), diag2)
        self.assertEqual(panlint.merge_diagnoses([diag1, diag2]), merged)

    def test_files(self):
        """
        Test all files in test_files that start with test_*.pan using lint_file
        """
        no_errors = ([], 0)
        dir_base = join(dirname(argv[0]), 'test_files')
        for afn in glob.glob(join(dir_base, 'test_*.pan')):
            fn = basename(afn)
            if fn.startswith('test_good'):
                self.assertEqual(panlint.lint_file(afn), no_errors)
            else:
                self.assertTrue(False, 'test_files: unknown testfile ' + afn)

    def test_mvn_templates(self):
        dir_base = join(dirname(argv[0]), 'test_files')
        self.assertEqual(
            panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), True),
            ([], 0),
        )
        self.assertEqual(
            panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), False)[1],
            1,
        )

    def test_strip_trailing_comments(self):
        comment_plain = panlint.Line('', 1, '''Words; # This is a trailing comment''')
        comment_in_string = panlint.Line('', 2, '''words = '# Not a trailing comment' + pictures;''')
        comment_mixed = panlint.Line('', 3, '''words = '# Not a trailing comment';#But this is''')

        annotation_plain = panlint.Line('', 4, '''Words; @{This is a trailing annotation}''')
        annotation_in_string = panlint.Line('', 5, '''words = '@{Not a trailing annotation}' + pictures;''')
        annotation_mixed = panlint.Line('', 6, '''words = '@{Not a trailing annotation}';@{But this is}''')

        self.assertEqual(
            panlint.strip_trailing_comments(comment_plain, []).text,
            'Words;'
        )
        self.assertEqual(
            panlint.strip_trailing_comments(comment_in_string, panlint.get_string_ranges(comment_in_string)).text,
            comment_in_string.text
        )
        self.assertEqual(
            panlint.strip_trailing_comments(comment_mixed, panlint.get_string_ranges(comment_mixed)).text,
            '''words = '# Not a trailing comment';'''
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_plain, []).text,
            'Words;'
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_in_string, panlint.get_string_ranges(annotation_in_string)).text,
            annotation_in_string.text
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_mixed, panlint.get_string_ranges(annotation_mixed)).text,
            '''words = '@{Not a trailing annotation}';'''
        )

    def test_whitespace_around_operators(self):
        good = {
            'simple': 'variable ALPHA = 5 + 3;',
            'fn': 'variable ALPHA = afunction() + 3;',
            'fn2': 'variable ALPHA = afunction() + 31;',
            'for': 'for (idx = 31; idx >= 0; idx = idx - 1) {',
            'square_brackets': 'variable EXAMPLE = b[c-1];',
            'negative':  'variable EXAMPLE = -1;',
            # lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
            'line_cont': '+ 42;',
            'line_to_be_cont': 'variable EXAMPLE = 42 +',
            'conditional_negative': 'variable A ?= -4;',
            'list_negatives': 'variable LARRY = list(-6, 5, -12);',
            'negative_first': 'variable NEVER = -2 * 3;',
            'negative_after': 'variable NAMED = 2 * -3;',
        }

        bad = {
            'before': (
                panlint.Line('', 2048, 'variable BOBBY = 8* 1;'),
                'Missing space before operator',
                '                 ^^',
            ),
            'after': (
                panlint.Line('', 3072, 'variable BRILLIANT = 16 /2;'),
                'Missing space after operator',
                '                        ^^',
            ),
            'both': (
                panlint.Line('', 4096, 'variable DAVID = 10-2;'),
                'Missing space before and after operator',
                '                  ^^^',
            ),
            'square_brackets': (
                panlint.Line('', 6144, 'variable XAVIER = b[c + 1];'),
                'Unwanted space in simple expression in square brackets',
                '                     ^',
            ),
            'negative': (
                panlint.Line('', 8192, 'variable XANDER = - 1;'),
                'Unwanted space after minus sign (not operator)',
                '                  ^^^',
            ),
            'negative_before': (
                panlint.Line('', 16001, 'variable BARRY = -8* -1;'),
                'Missing space before operator',
                '                  ^^',
            ),
            'negative_after': (
                panlint.Line('', 16002, 'variable BEHOVES = -16 /-2;'),
                'Missing space after operator',
                '                       ^^',
            ),
            'negative_both': (
                panlint.Line('', 16003, 'variable DARIUS = -10--2;'),
                'Missing space before and after operator',
                '                    ^^^',
            ),
        }

        lc = panlint.LineChecks()

        for i, (name, line) in enumerate(good.items()):
            result = lc.whitespace_around_operators(panlint.Line(f'{name}.pan', i, line), [])
            self.assertEqual(result.problems, [], name)

        for name, (bad_line, bad_message, bad_diag) in bad.items():
            result = lc.whitespace_around_operators(bad_line, [])
            self.assertEqual(len(result.problems), 1)
            self.assertEqual(result.problems[0].message.text, bad_message)
            self.assertEqual(result.problems[0].diagnose(), bad_diag)

        # Handling lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
        self.assertEqual(lc.whitespace_around_operators(panlint.Line('', 9216, '+ 42;'), []).problems, [])
        self.assertEqual(lc.whitespace_around_operators(panlint.Line('', 10240, 'variable x = 42 +'), []).problems, [])

        for s in ['+', '-', '*', '/', '>', '<', '>=', '<=']:
            t = f'variable g = 3 {s} 4'
            line = panlint.Line('tests.pan', 42, t)
            result = lc.whitespace_around_operators(line, [])
            self.assertEqual(line, result)

    def test_whitespace_after_semicolons(self):
        self._assert_lint_line(
            panlint.Line('', 1, 'foreach(k; v;  things) {'),
            ['             ^^'],
            ['Semicolons should be followed exactly one space or end-of-line'],
            1,
        )
        self._assert_lint_line(
            panlint.Line('', 2, 'foreach(k;    v;  things) {'),
            ['          ^^^^', '                ^^'],
            ['Semicolons should be followed exactly one space or end-of-line'],
            2,
        )

    def test_profilepath_trailing_slash(self):
        # Trailing slashes in profile paths
        self._assert_lint_line(
            panlint.Line('', 148, '"/system/hostname" = "foo.example.org";'),
            [],
            [],
            0,
        )
        self._assert_lint_line(
            panlint.Line('', 151, "prefix '/system/network/interfaces/eth0';"),
            [],
            [],
            0,
        )

        self._assert_lint_line(
            panlint.Line('', 795, "'/' = dict();"),
            [],
            [],
            0,
        )

        self._assert_lint_line(
            panlint.Line('', 22, '"/system/hostname/" = "bar.example.org";'),
            ['                 ^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

        self._assert_lint_line(
            panlint.Line('', 77, '"/system/hostname////////" = "bob.example.org";'),
            ['                 ^^^^^^^^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

        self._assert_lint_line(
            panlint.Line('', 182, "prefix '/system/aii/osinstall/ks/';"),
            ['                                ^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

    def test_lint_line(self):
        good_first = panlint.Line('', 120, 'structure template foo.bar;')
        bad_first = panlint.Line('', 122, 'variable foo = "bar";')

        # Test first line checking
        self._assert_lint_line(good_first, [], [], 0, True)

        self._assert_lint_line(bad_first, ['^'*len(bad_first.text)], None, 1, True)

        # Test component inclusion check
        self._assert_lint_line(
            panlint.Line('', 123, '"/software/components/foo/bar" = 42;'),
            ['                      ^^^'],
            None,
            1,
        )

        # Test pattern based checking
        self._assert_lint_line(
            panlint.Line('', 124, '   x = x + 1; # Bad Indentation'),
            ['^^^'],
            None,
            1,
        )

        # Test method based checking
        self._assert_lint_line(
            panlint.Line('', 125, 'x = x+1; # Missing space'),
            ['    ^^^'],
            None,
            1,
        )

        # Test that all three check types co-exist
        self._assert_lint_line(
            panlint.Line('', 126, '  "/software/components/foo/bar" = 42+7;'),
            ['^^', '                        ^^^', '                                    ^^^'],
            None,
            3,
        )

    def test_find_annotation_blocks(self):
        test_text = '''structure template awesome;
        @{ desc = what is the point of this template? }

        'foo' : string
        'bar' ? long

        @{ This stuff on line seven is not code, things like x=x+1 should be ignored here... }
        'simon' : string = 'says';
        '''

        six.assertCountEqual(self, panlint.find_annotation_blocks(test_text), [2, 7])
        self.assertEqual(panlint.find_annotation_blocks('template garbage;\n\n# Nothing to see here.\n\n'), [])

    def test_find_heredoc_blocks(self):
        test_text = '''unique template awesome;
        "/something" = 1;
        "/a/b/c" = <<EOFF;
        "/a/" = 1+1;
        EOFF
        "/very" = 1;
        "/more" = <<EOFF;
        hello
        EOFF
        '''

        six.assertCountEqual(self, panlint.find_heredoc_blocks(test_text), [4, 5, 8, 9])
        self.assertEqual(panlint.find_heredoc_blocks('template garbage;\n\n# Nothing to see here.\n\n'), [])

    def test_component_use(self):
        # Test a line containing a standard path assignment
        line_standard = panlint.Line('', 100, "'/software/components/chkconfig/service/rdma' = dict(")
        diag_standard = "                      ^^^^^^^^^"
        line_standard_commented = panlint.Line('', 101, '# ' + line_standard.text)

        # Test a line setting a path prefix
        line_prefix = panlint.Line(
            '',
            200,
            "prefix '/software/components/metaconfig/services/{/etc/sysconfig/fetch-crl}';",
        )
        diag_prefix = "                             ^^^^^^^^^^"
        line_prefix_commented = panlint.Line(
            '',
            201,
            '# ' + line_prefix.text,
        )

        # Test both lines with components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, ['chkconfig'], False),
            (line_standard, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, ['metaconfig'], False),
            (line_prefix, False)
        )

        # Test both lines without components listed as included
        self._assert_lint_line(
            line_standard,
            [diag_standard],
            ['Component chkconfig in use, but component config has not been included'],
            1,
        )
        self._assert_lint_line(
            line_prefix,
            [diag_prefix],
            ['Component metaconfig in use, but component config has not been included'],
            1,
        )

        # Test both lines without components listed as included but commented out
        self._assert_lint_line(
            line_standard_commented,
            [],
            [],
            0,
        )
        self._assert_lint_line(
            line_prefix_commented,
            [],
            [],
            0,
        )

    def test_component_source_file(self):
        """ Test the regex designed to detect whether the template being linted is part of a component's source code """
        rfa = panlint.RE_COMPONENT_SOURCE_FILE.findall

        # These tests should all find component names
        self.assertEqual(
            rfa('/var/quattor/cfg/plenary/template-library/18.6.0/core/components/shorewall/sysconfig.pan'),
            ['shorewall'],
        )
        self.assertEqual(
            rfa('./ncm-metaconfig/src/main/metaconfig/nginx/tests/profiles/config.pan'),
            ['metaconfig'],
        )
        self.assertEqual(
            rfa('./configuration-modules-core/ncm-opennebula/src/main/resources/tests/profiles/remoteconf_ceph.pan'),
            ['opennebula'],
        )
        self.assertEqual(
            rfa('ncm-network/src/test/resources/actions.pan'),
            ['network'],
        )

        # These tests should NOT find component names
        self.assertEqual(
            rfa('features/monitoring/grafana/config.pan'),
            [],
        )
        self.assertEqual(
            rfa('./service/profileserver/client/config.pan'),
            [],
        )

    def test_check_line_component_use(self):
        problems = panlint.check_line_component_use(
            panlint.Line('rdma.pan', 12, "'/software/components/chkconfig/service/rdma' = dict("),
            [],
        )

        self.assertIsInstance(problems, list)
        self.assertEqual(len(problems), 1)
        for p in problems:
            self.assertIsInstance(p, panlint.Problem)

    def test_check_line_patterns(self):
        lines = [
            ('variable UNIVERSAL_TRUTH = 42;', []),
            ('variable BAD = -1;', ['Global variables should be five or more characters']),
            ('variable bad_long = ":-(";', ['Global variables should be uppercase']),
            ('variable bad = "all lower";', ['Global variables should be uppercase',
                                             'Global variables should be five or more characters']),
            ('variable tricky_onE = "Uhoh";', ['Global variables should be uppercase']),
            ('variable camelCase = "camels!";', ['Global variables should be uppercase']),
            ('variable TitleCase ?= -3;', ['Global variables should be uppercase']),
            ('variable NoSpacesHere?=True;', ['Global variables should be uppercase']),
            ('error(format("Duplicate %s in foo", mp));', ['Redundant use of format within error or debug call']),
            ('error("is_asndate: invalid format for time");', []),
            ('debug(format("%s: bar: %s", OBJECT, ARGV[0]));', ['Redundant use of format within error or debug call']),
            ('debug("Foo" + bar + " has an unexpected format (should be a dict)");', []),
            ('"/software/components/metaconfig/services/{/etc/example_service_with_long_name/conf.d/dropin_config.cfg}/contents/foo" = "bar";', ['Line is longer than 120 characters']),
            ('bind "/software/components/metaconfig/services/{/etc/example_service_with_long_name/conf.d/dropin_config.cfg}/contents" = type_example_service;', []),
        ]

        for text, messages in lines:
            line = panlint.Line('patterns.pan', 0, text)
            messages = set(messages)
            problems = panlint.check_line_patterns(line, [])

            self.assertIsInstance(problems, list)
            for p in problems:
                self.assertIsInstance(p, panlint.Problem)
            self.assertEqual(set([p.message.text for p in problems]), messages)

    def test_check_line_paths(self):
        problems = panlint.check_line_paths(
            panlint.Line('slash.pan', 24, "'/software/components/fake/' = list("),
        )

        self.assertIsInstance(problems, list)
        self.assertEqual(len(problems), 1)
        for p in problems:
            self.assertIsInstance(p, panlint.Problem)

    def test_check_line_methods(self):
        result = panlint.check_line_methods(
            panlint.Line('foo.pan', 251, "'/software/components/bar' = 5+5;"),
            [(0, 25)],
        )

        self.assertIsInstance(result, panlint.Line)
        self.assertEqual(len(result.problems), 1)
        for p in result.problems:
            self.assertIsInstance(p, panlint.Problem)

    def test_print_report(self):
        test_line = panlint.Line('fake.pan', 7, "This is a FAKE line")
        test_line.problems = [panlint.Problem(10, 14, panlint.Message('FM000', 1, u'Everything is Fine'))]

        expected_output = '\n'.join([
            '',
            'fake.pan:7: Everything is Fine',
            '\x1b[32mThis is a FAKE line\x1b[39m',
            '\x1b[34m          ^^^^\x1b[39m',
            '',
        ])

        with mock.patch('sys.stdout', new=StringIO()) as mock_stdout:
            panlint.print_report(test_line)
            self.assertEqual(mock_stdout.getvalue(), expected_output)

    def test_features_standalone(self):
        lc = panlint.LineChecks()

        self.assertEqual(
            lc.features_standalone(
                panlint.Line('./linux/features/test/foo/config.pan', 3, "include 'features/test/foo/service/users';"),
                [],
            ).problems,
            [],
        )

        problems = lc.features_standalone(
            panlint.Line('./linux/features/test/foo/config.pan', 3, "include 'features/test/bar/webserver';"),
            [],
        ).problems
        self.assertEqual(len(problems), 1)
        for p in problems:
            self.assertIsInstance(p, panlint.Problem)
            self.assertEqual(p.message, 'Feature template includes a template which is not a child of the feature')
            self.assertEqual(p.start, 9)
            self.assertEqual(p.end, 36)


if __name__ == '__main__':
    unittest.main()
