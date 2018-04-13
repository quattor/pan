package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

import java.util.IllegalFormatException;

import static org.quattor.pan.utils.MessageUtils.MSG_FORMAT_REQUIRES_PRIMITIVE;
import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_FORMAT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FIRST_ARG_FORMAT;

/**
 * Created by iliclaey.
 *
 * This class performs the actual formatting.
 */
abstract public class Formatter extends BuiltInFunction {

    protected Formatter(String name, SourceRange sourceRange, Operation... operations) {
        super(name, sourceRange, operations);
    }

    public StringProperty format(Context context) {

        // Calculate arguments.
        Element[] args = calculateArgs(context);

        // Pull out the format string.
        String format = null;
        try {
            format = ((StringProperty) args[0]).getValue();
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, context,
                    MSG_INVALID_FIRST_ARG_FORMAT);
        }

        // Now pull out all of the arguments for formatting.
        Object[] jargs = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            Element e = args[i].execute(context);

            if (e instanceof Resource) {
                jargs[i - 1] = e.toString();
            } else if (e instanceof Property) {
                jargs[i - 1] = ((Property) e).getValue();
            } else {
                throw EvaluationException.create(sourceRange, context,
                        MSG_FORMAT_REQUIRES_PRIMITIVE);
            }
        }

        StringProperty result = null;
        try {
            result = StringProperty.getInstance(String.format(format, jargs));
        } catch (IllegalFormatException ife) {
            throw EvaluationException.create(sourceRange, context,
                    MSG_ILLEGAL_FORMAT, ife.getLocalizedMessage());
        }

        return result;
    }
}
