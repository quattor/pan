package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.*;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

import java.util.ArrayList;

import static org.quattor.pan.utils.MessageUtils.MSG_TWO_ARGS_REQ;

/**
 * Created by iliclaey.
 */
public class Join extends BuiltInFunction {
    protected Join(SourceRange sourceRange, Operation... operations) {
        super("join", sourceRange, operations);
    }

    public static Operation getInstance(SourceRange sourceRange,
                                        Operation... operations) throws SyntaxException {

        // There must be two arguments, a delimeter and a list of elements to join.
        if (operations.length != 2) {
            throw SyntaxException.create(sourceRange, MSG_TWO_ARGS_REQ, "join");
        }

        return new Join(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) throws EvaluationException {

        assert (ops.length == 2);

        // Extract the first argument. This must be a string value.
        String delimeter = null;
        try {
            delimeter = ((StringProperty) ops[0]).getValue();
        } catch (ClassCastException cce) {
            throw new EvaluationException(
                    "first argument in join() must be a string",
                    getSourceRange(), context);
        }

        // Get the list with elements to join.
        ArrayList<String> result = null;

        // Check whether the second argument is actually a list.
        Element list = ops[1].execute(context);
        if (list instanceof ListResource) {
            result = new ArrayList<String>();

            // Loop over the list items and convert them to strings.
            Resource.Iterator it = ((ListResource) list).iterator();
            while(it.hasNext()) {
                Element e = it.next().getValue();
                if (e instanceof Resource) {
                    throw new EvaluationException(
                            "the passed list in join() can't contain nested elements",
                            getSourceRange(), context);
                }
                result.add(e.toString());
            }

        } else {
            throw new EvaluationException(
                    "the second argument in join() should be a list",
                    getSourceRange(), context);
        }

        return StringProperty.getInstance(String.join(delimeter, result));
    }
}
