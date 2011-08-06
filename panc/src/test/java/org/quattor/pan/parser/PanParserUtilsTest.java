/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/parser/PanParserTokenManagerTest.java $
 $Id: PanParserTokenManagerTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.parser;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PanParserUtilsTest {

    @Test
    public void testValidEscapeSequences() {

        Map<String, String> testPairs = new HashMap<String, String>();

        testPairs.put("\"\\t\"", "\t");
        testPairs.put("\"\\b\"", "\b");
        testPairs.put("\"\\n\"", "\n");
        testPairs.put("\"\\r\"", "\r");
        testPairs.put("\"\\f\"", "\f");
        testPairs.put("\"\\\"\"", "\"");
        testPairs.put("\"\\\\\"", "\\");
        testPairs.put("\"\\x12\"", "\u0012");

        for (Map.Entry<String, String> entry : testPairs.entrySet()) {

            StringBuilder input = new StringBuilder(entry.getKey());
            PanParserUtils.processDoubleQuotedString(input, null);
            String result = input.toString();

            String correct = entry.getValue();

            assertEquals(correct, result);

        }

    }

    @Test
    public void testIgnoredEscapeSequence() {

        String result = "a\nb";

        // Because of an error in scanning the string, escape replacements after
        // a line continuation were ignored. (See SF bug #2533401.)
        String[] inputs = { "\"a\\\n\\nb\"", "\"a\\\r\\nb\"", "\"a\\\r\n\\nb\"" };

        for (String input : inputs) {
            StringBuilder sb = new StringBuilder(input);
            PanParserUtils.processDoubleQuotedString(sb, null);
            assertEquals(result, sb.toString());
        }

    }

}
