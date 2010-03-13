package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_IN_COMPILE_TIME_CONTEXT;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * This is just a marker class to identify those classes that are built-in
 * functions in the pan language. All built-in functions must have a factory
 * method getInstance(SourceRange, Operation, ...) that allows an instance to be
 * created. The return value must be an Operation to allow the method to
 * optimize the given function call.
 * 
 * These restrictions cannot be enforced by the java language itself. They are
 * enforced through the JUnit test functions associated with this class.
 * 
 * @author loomis
 * 
 */
public abstract class BuiltInFunction extends AbstractOperation {

	protected final String name;

	protected BuiltInFunction(String name, SourceRange sourceRange,
			Operation... operations) {
		super(sourceRange, operations);
		assert (name != null);
		this.name = name;
	}

	protected void throwExceptionIfCompileTimeContext(Context context) {
		if (context.isCompileTimeContext()) {
			throw EvaluationException.create(sourceRange,
					MSG_INVALID_IN_COMPILE_TIME_CONTEXT, name);
		}
	}

	@Override
	public String toString() {
		return name + "()";
	}

}
