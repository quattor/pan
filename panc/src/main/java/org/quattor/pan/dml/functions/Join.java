package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.*;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

import java.util.ArrayList;

import static org.quattor.pan.utils.MessageUtils.MSG_SECOND_ARG_LIST_OR_VARIABLE_REF;
import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_CANNOT_BE_NULL;
import static org.quattor.pan.utils.MessageUtils.MSG_FIRST_STRING_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_TWO_OR_MORE_ARG_REQ;

/**
 * Created by iliclaey.
 *
 * This only allows strings to be passed as arguments.
 * If you want to be able to pass other types, remove the check whether the arguments are strings.
 * In execute, make sure the values are somehow converted to strings, since a list of strings is
 * used to perform the join.
 */
final public class Join extends BuiltInFunction {

    protected Join(SourceRange sourceRange, Operation... operations) {
        super("join", sourceRange, operations);
    }

    public static Operation getInstance(SourceRange sourceRange,
                                        Operation... operations) throws SyntaxException {

        if (operations.length < 2) {
            throw SyntaxException.create(sourceRange,
                    MSG_TWO_OR_MORE_ARG_REQ, "join");
        }

        // The first argument can't be null.
        if (operations[0] instanceof Null) {
            throw SyntaxException.create(sourceRange, MSG_VALUE_CANNOT_BE_NULL,
                    "join");
        }

        // The first argument needs to be a string.
        if (operations[0] instanceof Element) {
            if (!(operations[0] instanceof StringProperty)) {
                throw SyntaxException.create(sourceRange, MSG_FIRST_STRING_ARG_REQ,
                        "join");
            }
        }

        // If there are only two arguments, the second argument can't be null and needs to be a list.
        if (operations.length == 2) {
            if (operations[1] instanceof Null) {
                throw SyntaxException.create(sourceRange, MSG_VALUE_CANNOT_BE_NULL,
                        "join");
            }

            if (operations[1] instanceof Element) {
                if (!(operations[1] instanceof ListResource)) {
                    throw SyntaxException.create(sourceRange,
                            MSG_SECOND_ARG_LIST_OR_VARIABLE_REF, "join");
                }
            }
        }

        return new Join(sourceRange, operations);
    }

    @Override
    public Element execute(Context context) throws EvaluationException {

        assert (ops.length != 1);

        // Extract the first argument. This must be a string value.
        String delimeter = null;
        try {
            delimeter = ((StringProperty) ops[0].execute(context)).getValue();
        } catch (ClassCastException cce) {
            throw new EvaluationException(
                    "first argument in join() must be a string",
                    getSourceRange(), context);
        }

        // Create a list containing all elements we need to join.
        ArrayList<String> result = new ArrayList<String>();

        // If the second argument is a ListResource, add the strings to the result-list.
        if (ops.length == 2) {
            // Double check whether the second argument is actually a list.
            Element list = ops[1].execute(context);

            if (list instanceof ListResource) {

                // Loop over the list items and convert them to strings.
                Resource.Iterator it = ((ListResource) list).iterator();
                while (it.hasNext()) {
                    Element e = it.next().getValue();

                    if (e instanceof Resource) {
                        throw new EvaluationException(
                                "the passed list in join() can't contain nested elements",
                                getSourceRange(), context);
                    }

                    if (!(e instanceof StringProperty)) {
                        throw new EvaluationException(
                                "all elements in the list need to be strings for join()",
                                getSourceRange(), context);
                    }

                    result.add(((StringProperty) e).getValue());
                }

            } else {
                throw new EvaluationException(
                        "the second argument in join() should be a list",
                        getSourceRange(), context);
            }

        // If all arguments are passed individually, add them to a list.
        } else {
            int length = ops.length;

            for (int i = 1; i < length; i++) {
                Element e = ops[i].execute(context);

                if (e instanceof ListResource) {
                    throw new EvaluationException(
                            "join() only accepts a single list as an argument or all the arguments individually",
                            getSourceRange(), context);
                } else if (!(e instanceof StringProperty)) {
                    throw new EvaluationException(
                            "join() only accepts strings",
                            getSourceRange(), context);
                }

                StringProperty sp = (StringProperty) e;
                result.add(sp.getValue());
            }
        }

        return StringProperty.getInstance(String.join(delimeter, result));
    }
}
