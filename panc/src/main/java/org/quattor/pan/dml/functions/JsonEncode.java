package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_JSON_UNDEF;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_NO_ARGS_JSON_ENCODE;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.utils.JsonUtils;
import org.quattor.pan.utils.MessageUtils;

final public class JsonEncode extends BuiltInFunction {

	private JsonEncode(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("json_encode", sourceRange, operations);

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_NO_ARGS_JSON_ENCODE);
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new JsonEncode(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);
		JsonUtils json = JsonUtils.getInstance();
		try {
			return StringProperty.getInstance(json.toJson(result));
		} catch (EvaluationException eve) {
			eve.addExceptionInfo(sourceRange, context);
			throw eve;
		}
	}

}
