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

import unittest
from sys import argv
from os.path import dirname, join

import panlint


class TestPanlint(unittest.TestCase):

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
        self.assertEqual(panlint.get_string_ranges('''there is a "string" in here'''), [(11, 19)])
        self.assertEqual(panlint.get_string_ranges('''"string" + 'string' + something'''), [(0, 8), (11, 19)])

    def test_merge_diagnoses(self):
        diag1 = ' ^'
        diag2 = '       ^^^'
        merged = ' ^     ^^^'
        self.assertEqual(panlint.merge_diagnoses([]), '')
        self.assertEqual(panlint.merge_diagnoses([diag1]), diag1)
        self.assertEqual(panlint.merge_diagnoses([diag2]), diag2)
        self.assertEqual(panlint.merge_diagnoses([diag1, diag2]), merged)

    def test_files(self):
        no_errors = ([], 0)
        dir_base = join(dirname(argv[0]), 'test_files')
        self.assertEqual(panlint.lint_file(join(dir_base, 'test_good_ordinary.pan')), no_errors)
        self.assertEqual(panlint.lint_file(join(dir_base, 'test_good_object.pan')), no_errors)
        self.assertEqual(panlint.lint_file(join(dir_base, 'test_good_structure.pan')), no_errors)
        self.assertEqual(panlint.lint_file(join(dir_base, 'test_good_unique.pan')), no_errors)
        self.assertEqual(panlint.lint_file(join(dir_base, 'test_good_declaration.pan')), no_errors)

    def test_mvn_templates(self):
        dir_base = join(dirname(argv[0]), 'test_files')
        self.assertEqual(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), True)[1], 0)
        self.assertEqual(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), False)[1], 1)

    def test_strip_trailing_comments(self):
        comment_plain = '''Words; # This is a trailing comment'''
        comment_in_string = '''words = '# Not a trailing comment' + pictures;'''
        comment_mixed = '''words = '# Not a trailing comment';#But this is'''

        annotation_plain = '''Words; @{This is a trailing annotation}'''
        annotation_in_string = '''words = '@{Not a trailing annotation}' + pictures;'''
        annotation_mixed = '''words = '@{Not a trailing annotation}';@{But this is}'''

        self.assertEqual(panlint.strip_trailing_comments(comment_plain, []), 'Words;')
        self.assertEqual(panlint.strip_trailing_comments(comment_in_string, panlint.get_string_ranges(comment_in_string)), comment_in_string)
        self.assertEqual(panlint.strip_trailing_comments(comment_mixed, panlint.get_string_ranges(comment_mixed)), '''words = '# Not a trailing comment';''')

        self.assertEqual(panlint.strip_trailing_comments(annotation_plain, []), 'Words;')
        self.assertEqual(panlint.strip_trailing_comments(annotation_in_string, panlint.get_string_ranges(annotation_in_string)), annotation_in_string)
        self.assertEqual(panlint.strip_trailing_comments(annotation_mixed, panlint.get_string_ranges(annotation_mixed)), '''words = '@{Not a trailing annotation}';''')

    def test_whitespace_around_operators(self):
        good = 'variable a = 5 + 3;'

        bad_before = 'variable b = 8* 1;'
        dgn_before = '             ^^'

        bad_after = 'variable b = 16 /2;'
        dgn_after = '                ^^'

        bad_both = 'variable d = 10-2;'
        dgn_both = '              ^^^'

        good_square_brackets = 'variable x = b[c-1];'
        bad_square_brackets = 'variable x = b[c + 1];'
        dgn_square_brackets = '                ^'

        good_negative = 'variable x = -1;'
        bad_negative = 'variable x = - 1;'
        dgn_negative = '             ^^^'

        lc = panlint.LineChecks()

        self.assertEqual(lc.whitespace_around_operators(good, []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(good_square_brackets, []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(good_negative, []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(bad_before, []), (False, dgn_before, 'Missing space before operator'))
        self.assertEqual(lc.whitespace_around_operators(bad_after, []), (False, dgn_after, 'Missing space after operator'))
        self.assertEqual(lc.whitespace_around_operators(bad_both, []), (False, dgn_both, 'Missing space before and after operator'))
        self.assertEqual(lc.whitespace_around_operators(bad_square_brackets, []),
                         (False, dgn_square_brackets, 'Unwanted space in simple expression in square brackets'))
        self.assertEqual(lc.whitespace_around_operators(bad_negative, []),
                         (False, dgn_negative, 'Unwanted space after minus sign (not operator)'))

        # Handling lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
        self.assertEqual(lc.whitespace_around_operators('+ 42;', []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators('variable x = 42 +', []), (True, '', ''))

    def test_whitespace_after_semicolons(self):
        bad_1 = 'foreach(k; v;  things) {'
        dgn_1 = ['             ^^']
        msg_1 = ['Semicolons should be followed exactly one space or end-of-line']
        self.assertEqual(
            panlint.lint_line(bad_1, 1, [], False),
            (dgn_1, set(msg_1), 1, False)
        )

        bad_2 = 'foreach(k;    v;  things) {'
        dgn_2 = ['          ^^^^', '                ^^']
        msg_2 = ['Semicolons should be followed exactly one space or end-of-line']
        self.assertEqual(
            panlint.lint_line(bad_2, 2, [], False),
            (dgn_2, set(msg_2), 2, False)
        )

    def test_profilepath_trailing_slash(self):
        good_line_1 = '"/system/hostname" = "foo.example.org";'
        self.assertEqual(
            panlint.lint_line(good_line_1, 148, [], False),
            ([], set(), 0, False)
        )

        good_line_2 = "prefix '/system/network/interfaces/eth0';"
        self.assertEqual(
            panlint.lint_line(good_line_2, 151, [], False),
            ([], set(), 0, False)
        )

        bad_line_1 = '"/system/hostname/" = "bar.example.org";'
        bad_diag_1 = ['                 ^']
        bad_msg_1 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_1, 22, [], False),
            (bad_diag_1, set(bad_msg_1), 1, False)
        )

        bad_line_2 = '"/system/hostname////////" = "bob.example.org";'
        bad_diag_2 = ['                 ^^^^^^^^']
        bad_msg_2 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_2, 77, [], False),
            (bad_diag_2, set(bad_msg_2), 1, False)
        )

        bad_line_3 = "prefix '/system/aii/osinstall/ks/';"
        bad_diag_3 = ['                                ^']
        bad_msg_3 = ['Unnecessary trailing slash at end of profile path']
        self.assertEqual(
            panlint.lint_line(bad_line_3, 182, [], False),
            (bad_diag_3, set(bad_msg_3), 1, False)
        )

    def test_lint_line(self):
        good_first = 'structure template foo.bar;'
        bad_first = 'variable foo = "bar";'

        # Test first line checking
        self.assertEqual(panlint.lint_line(good_first, 1, [], True), ([], set(), 0, False))

        diagnoses, messages, problem_count, first_line = panlint.lint_line(bad_first, 1, [], True)
        self.assertEqual(diagnoses, ['^'*len(bad_first)])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test component inclusion check
        diagnoses, messages, problem_count, first_line = panlint.lint_line('"/software/components/foo/bar" = 42;', 7, [], False)
        self.assertEqual(diagnoses, ['                      ^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test pattern based checking
        diagnoses, messages, problem_count, first_line = panlint.lint_line('   x = x + 1; # Bad Indentation', 7, [], False)
        self.assertEqual(diagnoses, ['^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test method based checking
        diagnoses, messages, problem_count, first_line = panlint.lint_line('x = x+1; # Missing space', 7, [], False)
        self.assertEqual(diagnoses, ['    ^^^'])
        self.assertNotEqual(messages, set())
        self.assertEqual(problem_count, 1)
        self.assertEqual(first_line, False)

        # Test that all three check types co-exist
        diagnoses, messages, problem_count, first_line = panlint.lint_line('  "/software/components/foo/bar" = 42+7;', 7, [], False)
        self.assertItemsEqual(diagnoses, ['^^', '                        ^^^', '                                    ^^^'])
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

        self.assertItemsEqual(panlint.find_annotation_blocks(test_text), [2, 7])
        self.assertEqual(panlint.find_annotation_blocks('template garbage;\n\n# Nothing to see here.\n\n'), [])

    def test_component_use(self):
        # Test a line containing a standard path assignment
        line_standard = "'/software/components/chkconfig/service/rdma' = dict("
        diag_standard = "                      ^^^^^^^^^"

        # Test a line setting a path prefix
        line_prefix = "prefix '/software/components/metaconfig/services/{/etc/sysconfig/fetch-crl}';"
        diag_prefix = "                             ^^^^^^^^^^"

        # Test both lines with components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, 100, ['chkconfig'], False),
            ([], set(), 0, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, 200, ['metaconfig'], False),
            ([], set(), 0, False)
        )

        # Test both lines without components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, 100, [], False),
            ([diag_standard], set(['Component chkconfig in use, but component config has not been included']), 1, False)
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, 200, [], False),
            ([diag_prefix], set(['Component metaconfig in use, but component config has not been included']), 1, False)
        )

        # Test both lines without components listed as included but commented out
        self.assertEqual(
            panlint.lint_line('# ' + line_standard, 100, [], False),
            ([], set(), 0, False)
        )
        self.assertEqual(
            panlint.lint_line('    #' + line_prefix, 200, [], False),
            ([], set(), 0, False)
        )

if __name__ == '__main__':
    unittest.main()
