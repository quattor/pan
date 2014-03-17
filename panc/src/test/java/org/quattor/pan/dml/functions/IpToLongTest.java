/*
  Copyright 2013 - Luis Fernando Muñoz Mejías and Universiteit Gent.

  Licensed under the Apache License, Version 2.0 (the "License"); you
  may not use this file except in compliance with the License.  You
  may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  implied.  See the License for the specific language governing
  permissions and limitations under the License.

*/

package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.TermFactory;


public class IpToLongTest extends BuiltInFunctionTestUtils {

        @Test
        public void checkGetInstance() {
                checkClassRequirements(IpToLong.class);
        }

        @Test
        public void checkValidIp() throws SyntaxException, InvalidTermException {

                ListResource l = (ListResource) runDml(IpToLong.getInstance(null, StringProperty
                                                             .getInstance("0.0.0.1")));
                long i = ((LongProperty) l.get(TermFactory.create(0))).getValue();
                assertEquals(1, i);
                l = (ListResource) runDml(IpToLong.getInstance(null, StringProperty
                                                .getInstance("1.0.0.1")));
                i = ((LongProperty) l.get(TermFactory.create(0))).getValue();
                assertEquals(0x1000001, i);
        }

        @Test(expected = SyntaxException.class)
        public void checkNoArgs() throws SyntaxException {
                runDml(IpToLong.getInstance(null));
        }

        @Test(expected = EvaluationException.class)
        public void checkInvalidIpRanges() throws SyntaxException {
                runDml(IpToLong.getInstance(null, StringProperty
                                            .getInstance("0.300.0.1")));
        }

        @Test(expected = EvaluationException.class)
        public void checkInvalidIpFormat() throws SyntaxException {
                runDml(IpToLong.getInstance(null, StringProperty
                                            .getInstance("Hello")));
        }

        @Test
        public void checkValidIpAndBitmask() throws SyntaxException, InvalidTermException {
                ListResource l = (ListResource) runDml(IpToLong.getInstance(null, StringProperty
                                                                            .getInstance("0.0.0.1/8")));
                long i = ((LongProperty) l.get(TermFactory.create(0))).getValue();
                assertEquals(1, i);
                i = ((LongProperty) l.get(TermFactory.create(1))).getValue();
                assertEquals(0xFF000000L, i);
        }

        @Test(expected = EvaluationException.class)
        public void checkTooBigBitmask() throws SyntaxException {
                runDml(IpToLong.getInstance(null, StringProperty
                                            .getInstance("0.0.0.0/33")));
        }

        @Test(expected = EvaluationException.class)
        public void checkTooSmallBitmask() throws SyntaxException {
                runDml(IpToLong.getInstance(null, StringProperty
                                            .getInstance("0.0.0.0/-1")));
        }
}
