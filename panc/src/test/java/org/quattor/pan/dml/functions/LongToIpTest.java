/*
 Copyright (c) 2013 Luis Fernando Muñoz Mejías and Universiteit Gent

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

/**
 * Test the long_to_ip4 built-in.
 */
public class LongToIpTest extends BuiltInFunctionTestUtils {

        @Test
        public void checkGetInstance() {
                checkClassRequirements(LongToIp.class);
        }

        @Test
        public void checkConvertIp() throws SyntaxException {
                LongProperty ip = LongProperty.getInstance(0x01020304L);

                Element r = runDml(LongToIp.getInstance(null, ip));
                assertTrue(r instanceof StringProperty);
                assertEquals("1.2.3.4", ((StringProperty) r).getValue());
        }

        @Test(expected = SyntaxException.class)
        public void checkNoArgs() throws SyntaxException {
                runDml(LongToIp.getInstance(null));
        }

        @Test(expected = EvaluationException.class)
        public void checkInvalidIpRanges() throws SyntaxException {
                runDml(LongToIp.getInstance(null, LongProperty
                                            .getInstance(0x1FFFFFFFFL)));
        }
}
