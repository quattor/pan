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

import panlint

class TestPanlint(unittest.TestCase):

    def test_diagnose(self):
        self.assertEqual(panlint.diagnose(0,0), '')
        self.assertEqual(panlint.diagnose(0,4), '^^^^')
        self.assertEqual(panlint.diagnose(2,8), '  ^^^^^^')
        self.assertEqual(panlint.diagnose(7,7), '       ')
        self.assertEqual(panlint.diagnose(3,-2), '   ')

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
        self.assertEqual(panlint.merge_diagnoses([diag1]), diag1)
        self.assertEqual(panlint.merge_diagnoses([diag2]), diag2)
        self.assertEqual(panlint.merge_diagnoses([diag1, diag2]), merged)


if __name__ == '__main__':
    unittest.main()