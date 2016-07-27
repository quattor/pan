package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.operators.SimpleVariable;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.type.FullType;

import static org.quattor.pan.utils.MessageUtils.*;

/**
 * Created by iliclaey.
 *
 * Function to check whether an element meets the requirements of a certain type.
 */
public final class IsValid extends BuiltInFunction {

    protected IsValid(SourceRange sourceRange, Operation... operations) {
        super("is_valid", sourceRange, operations);
    }

    public static Operation getInstance(SourceRange sourceRange, Operation... operations) throws SyntaxException {

        // IsValid requires two arguments.
        if (operations.length != 2) {
            throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ, "is_valid");
        }

        // The first argument should be the type, which will be contained in a SimpleVariable.
        if (!(operations[0] instanceof Variable)) {
            throw SyntaxException.create(sourceRange, MSG_FIRST_ARG_TYPE_REQ, "is_valid");
        }

        return new IsValid(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) {

        throwExceptionIfCompileTimeContext(context);

        // Get the name of the type.
        SimpleVariable type = null;
        try {
            type = (SimpleVariable) ops[0];
        } catch (ClassCastException cce) {
            throw EvaluationException.create(sourceRange, MSG_FIRST_ARG_TYPE_REQ, "is_valid");
        }

        // Get the element and type to validate.
        Element el = ops[1].execute(context);
        FullType ft = context.getFullType(type.getIdentifier());

        // Make sure the passed type exists.
        if (ft == null) {
            throw EvaluationException.create(sourceRange, MSG_NONEXISTANT_TYPE, type.getIdentifier());
        }

        // Check if the element indeed corresponds to the passed type.
        try {
            ft.validate(context, el);
        } catch (ValidationException ve) {
            // If the validation did not succeed, return false.
            return BooleanProperty.FALSE;
        }

        return BooleanProperty.TRUE;
    }
}
