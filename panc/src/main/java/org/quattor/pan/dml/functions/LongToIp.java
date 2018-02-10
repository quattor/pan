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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Format.java $
 $Id: Format.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_TYPE_LONG_TO_IP;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_RANGE_LONG_TO_IP;


import java.util.IllegalFormatException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Produces a string representing an IPv4 address in dotted format
 * from a long
 *
 * @author Luis Fernando Muñoz Mejías
 *
 */
final public class LongToIp extends BuiltInFunction {

    private LongToIp(SourceRange sourceRange, Operation... operations)
            throws SyntaxException {
        super("long_to_ip4", sourceRange, operations);

        // There must be exactly one argument.
        if (operations.length != 1) {
            throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ,
                    name);
        }
    }

    public static Operation getInstance(SourceRange sourceRange,
            Operation... operations) throws SyntaxException {
        return new LongToIp(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) {

        assert (ops.length == 1);

        Element[] args = calculateArgs(context);
        String[] ip = new String[4];
        long nip;

        try {
            nip = ((LongProperty) args[0]).getValue();
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context,
                                             MSG_INVALID_TYPE_LONG_TO_IP);
        }

        if (nip > 0xFFFFFFFFL || nip < 0)
            throw EvaluationException.create(sourceRange, context,
                                             MSG_INVALID_RANGE_LONG_TO_IP);

        for (int i = 3; i >= 0; i--, nip >>= 8)
            ip[i] = Long.toString(nip & 0xFF);

        /* Java doesn't provide with any join function in its standard
         * library -- rlly??? */
        StringProperty result = StringProperty.getInstance(
            String.format("%s.%s.%s.%s", ip[0], ip[1], ip[2], ip[3]));

        // Return the value. It should never be null if we reach this part of
        // the code.
        assert (result != null);
        return result;
    }
}
