/*
 Copyright 2013 - Luis Fernando Muñoz Mejías and Universiteit Gent.

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

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IP4_FORMAT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_TYPE_IP_TO_LONG;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IP4_BITMASK;

import java.util.IllegalFormatException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

/**
 * Produces the binary representation of an IPv4 address, maybe with
 * the binary representation of the network mask part of the string.
 *
 * @author Luis Fernando Muñoz Mejías
 *
 */
final public class IpToLong extends BuiltInFunction {

    private final static Pattern IP4_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)(?:/(\\d+))?$");

    private final static int MAX_BITS_BITMASK = 32;

    private IpToLong(SourceRange sourceRange, Operation... operations)
            throws SyntaxException {
        super("ip4_to_long", sourceRange, operations);

        // There must be exactly one argument.
        if (operations.length != 1) {
            throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ,
                    name);
        }
    }

    public static Operation getInstance(SourceRange sourceRange,
            Operation... operations) throws SyntaxException {
        return new IpToLong(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) {

        assert (ops.length == 1);

        long nip, mask = -1;
        // Calculate arguments.
        Element[] args = calculateArgs(context);
        String ip;
        int i;

        try {
            ip = ((StringProperty) args[0]).getValue();
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context,
                                             MSG_INVALID_TYPE_IP_TO_LONG);
        }

        Matcher m = IP4_PATTERN.matcher(ip);

        if (!m.find())
            throw EvaluationException.create(sourceRange, context,
                                             MSG_INVALID_IP4_FORMAT);

        for (nip = 0, i = 1; i <= 4; i++, nip <<= 8) {
            long j = Long.parseLong(m.group(i));
            if (j > 255) {
                throw EvaluationException.create(sourceRange, context,
                                                 MSG_INVALID_IP4_FORMAT);
            }
            nip |= j;
        }

        nip >>= 8;

        try {
            mask = 0xFFFFFFFFL;
            i = Integer.parseInt(m.group(5));
            if (i < 0 || i > MAX_BITS_BITMASK)
                throw EvaluationException.create(sourceRange, context,
                                                 MSG_INVALID_IP4_BITMASK);
            i = MAX_BITS_BITMASK - i;
            mask = (mask >> i) << i;
        } catch (NumberFormatException e) {
            /* Nothing to do if there is no bitmask */
        }

        ListResource result = new ListResource();

        ((ListResource) result).append(LongProperty.getInstance(nip));

        if (mask != -1)
            ((ListResource) result).append(LongProperty.getInstance(mask));

        // Return the value. It should never be null if we reach this part of
        // the code.
        assert (result != null);
        return result;

    }
}
