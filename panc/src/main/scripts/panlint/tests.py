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
import six

import panlint


class TestPanlint(unittest.TestCase):
    def setUp(self):
        self.maxDiff = None
        self.longMessage = True

    def test_diagnose(self):
        self.assertEqual(panlint.diagnose(0, 0), '')
        self.assertEqual(panlint.diagnose(0, 4), '^^^^')
        self.assertEqual(panlint.diagnose(2, 8), '  ^^^^^^')
        self.assertEqual(panlint.diagnose(7, 7), '       ')
        self.assertEqual(panlint.diagnose(3, -2), '   ')

    def test_print_diagnosis(self):
        FORMAT = '\x1b[34m%s\x1b[39m'
        self.assertEqual(panlint.print_diagnosis(''), FORMAT % '')
        self.assertEqual(panlint.print_diagnosis('so many words'), FORMAT % 'so many words')

    def test_get_string_ranges(self):
        self.assertEqual(panlint.get_string_ranges(panlint.Line('', 1, '''there is a "string" in here''')), [(11, 19)])
        self.assertEqual(panlint.get_string_ranges(panlint.Line('', 1, '''"string" + 'string' + something''')),
                         [(0, 8), (11, 19)])

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
        self.assertEqual(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), True)[1], 0)
        self.assertEqual(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), False)[1], 1)

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
            'simple': 'variable a = 5 + 3;',
            'fn': 'variable a = afunction() + 3;',
            'fn2': 'variable a = afunction() + 31;',
            'for': 'for (idx = 31; idx >= 0; idx = idx - 1) {',
            'square_brackets': 'variable x = b[c-1];',
            'negative': 'variable x = -1;',
            # lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
            'line_cont': '+ 42;',
            'line_to_be_cont': 'variable x = 42 +',
        }

        bad_before = panlint.Line('', 2048, 'variable b = 8* 1;')
        dgn_before = '             ^^'

        bad_after = panlint.Line('', 3072, 'variable b = 16 /2;')
        dgn_after = '                ^^'

        bad_both = panlint.Line('', 4096, 'variable d = 10-2;')
        dgn_both = '              ^^^'

        bad_square_brackets = panlint.Line('', 6144, 'variable x = b[c + 1];')
        dgn_square_brackets = '                ^'

        bad_negative = panlint.Line('', 8192, 'variable x = - 1;')
        dgn_negative = '             ^^^'

        bad_tests = [
            (bad_before, 'Missing space before operator', dgn_before),
            (bad_after, 'Missing space after operator', dgn_after),
            (bad_both, 'Missing space before and after operator', dgn_both),
            (bad_square_brackets, 'Unwanted space in simple expression in square brackets', dgn_square_brackets),
            (bad_negative, 'Unwanted space after minus sign (not operator)', dgn_negative)
        ]

        lc = panlint.LineChecks()

        for i, (_, line) in enumerate(good.items()):
            passed, _, message = lc.whitespace_around_operators(panlint.Line('', i, line), [])
            self.assertTrue(passed)
            self.assertEqual(message, '')

        for bad_test, bad_message, bad_diag in bad_tests:
            passed, diag, message = lc.whitespace_around_operators(bad_test, [])
            self.assertFalse(passed)
            self.assertEqual(message, bad_message)
            self.assertEqual(diag, bad_diag)

        # Handling lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
        self.assertEqual(lc.whitespace_around_operators(panlint.Line('', 9216, '+ 42;'), []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(panlint.Line('', 10240, 'variable x = 42 +'), []),
                         (True, '', ''))

    def test_whitespace_after_semicolons(self):
        bad_1 = panlint.Line('', 1, 'foreach(k; v;  things) {')
        dgn_1 = ['             ^^']
        msg_1 = ['Semicolons should be followed exactly one space or end-of-line']
        self.assertEqual(
            panlint.lint_line(bad_1, [], False),
            (dgn_1, set(msg_1), 1, False)
        )

        bad_2 = panlint.Line('', 2, 'foreach(k;    v;  things) {')
        dgn_2 = ['          ^^^^', '                ^^']
        msg_2 = ['Semicolons should be followed exactly one space or end-of-line']
        self.assertEqual(
            panlint.lint_line(bad_2, [], False),
            (dgn_2, set(msg_2), 2, False)
        )

    def test_profilepath_trailing_slash(self):
        good_line_1 = panlint.Line('', 148, '"/system/hostname" = "foo.example.org";')
        self.assertEqual(
            panlint.lint_line(good_line_1, [], False),
            ([], set(), 0, False)
        )

        good_line_2 = panlint.Line('', 151, "prefix '/system/network/interfaces/eth0';")
        self.assertEqual(
            panlint.lint_line(good_line_2, [], False),
            ([], set(), 0, False)
        )

        good_line_3 = panlint.Line('', 795, "'/' = dict();")
        self.assertEqual(
            panlint.lint_line(good_line_3, [], False),
            ([], set(), 0, False)
        )

        bad_line_1 = panlint.Line('', 22, '"/system/hostname/" = "bar.example.org";')
        bad_diag_1 = ['                 ^']
        bad_msg_1 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_1, [], False),
            (bad_diag_1, set(bad_msg_1), 1, False)
        )

        bad_line_2 = panlint.Line('', 77, '"/system/hostname////////" = "bob.example.org";')
        bad_diag_2 = ['                 ^^^^^^^^']
        bad_msg_2 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_2, [], False),
            (bad_diag_2, set(bad_msg_2), 1, False)
        )

        bad_line_3 = panlint.Line('', 182, "prefix '/system/aii/osinstall/ks/';")
        bad_diag_3 = ['                                ^']
        bad_msg_3 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_3, [], False),
            (bad_diag_3, set(bad_msg_3), 1, False)
        )

    def test_lint_line(self):
        good_first = panlint.Line('', 120, 'structure template foo.bar;')
        bad_first = panlint.Line('', 122, 'variable foo = "bar";')

        # Test first line checking
        self.assertEqual(panlint.lint_line(good_first, [], True), ([], set(), 0, False))

        diagnoses, messages, problem_count, first_line = panlint.lint_line(bad_first, [], True)
        self.assertEqual(diagnoses, ['^' * len(bad_first.text)])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test component inclusion check
        diagnoses, messages, problem_count, first_line = panlint.lint_line(
            panlint.Line('', 123, '"/software/components/foo/bar" = 42;'),
            [],
            False,
        )
        self.assertEqual(diagnoses, ['                      ^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test pattern based checking
        diagnoses, messages, problem_count, first_line = panlint.lint_line(
            panlint.Line('', 124, '   x = x + 1; # Bad Indentation'),
            [],
            False,
        )
        self.assertEqual(diagnoses, ['^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test method based checking
        diagnoses, messages, problem_count, first_line = panlint.lint_line(
            panlint.Line('', 125, 'x = x+1; # Missing space'),
            [],
            False,
        )
        self.assertEqual(diagnoses, ['    ^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test that all three check types co-exist
        diagnoses, messages, problem_count, first_line = panlint.lint_line(
            panlint.Line('', 126, '  "/software/components/foo/bar" = 42+7;'),
            [],
            False,
        )
        six.assertCountEqual(self, diagnoses, [
            '^^',
            '                        ^^^',
            '                                    ^^^',
        ])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 3)
        self.assertEqual(first_line, False)

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
        line_prefix = panlint.Line('', 200,
                                   "prefix '/software/components/metaconfig/services/{/etc/sysconfig/fetch-crl}';")
        diag_prefix = "                             ^^^^^^^^^^"
        line_prefix_commented = panlint.Line('', 201, '# ' + line_prefix.text)

        # Test both lines with components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, ['chkconfig'], False),
            ([], set(), 0, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, ['metaconfig'], False),
            ([], set(), 0, False)
        )

        # Test both lines without components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, [], False),
            ([diag_standard], set(['Component chkconfig in use, but component config has not been included']), 1, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, [], False),
            ([diag_prefix], set(['Component metaconfig in use, but component config has not been included']), 1, False)
        )

        # Test both lines without components listed as included but commented out
        self.assertEqual(
            panlint.lint_line(line_standard_commented, [], False),
            ([], set(), 0, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix_commented, [], False),
            ([], set(), 0, False)
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
        ]

        for text, messages in lines:
            line = panlint.Line('patterns.pan', 0, text)
            messages = set(messages)
            m = panlint.check_line_patterns(line, [])[1]
            self.assertEqual(m, messages)


if __name__ == '__main__':
    unittest.main()
