package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ARGS_JSON_DECODE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_NO_ARGS_JSON_DECODE;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.JsonUtils;
import org.quattor.pan.utils.MessageUtils;

import java.nio.charset.Charset;

final public class JsonDecode extends BuiltInFunction {

	private JsonDecode(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("json_decode", sourceRange, operations);

		// Ensure that there is exactly one argument. Since the parser does
		// little argument checking for function calls, this explicit check is
		// needed.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange,
					MSG_INVALID_NO_ARGS_JSON_DECODE);
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new JsonDecode(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		assert (ops.length == 1);

		Element result = ops[0].execute(context);
		JsonUtils json = JsonUtils.getInstance();
		try {
			String s = ((StringProperty) result).getValue();
			return json.fromJson(s);
		} catch (ClassCastException cce) {
			throw EvaluationException.create(sourceRange, context,
					MSG_INVALID_ARGS_JSON_DECODE);
		} catch (EvaluationException eve) {
			eve.addExceptionInfo(sourceRange, context);
			throw eve;
		}
	}

}
