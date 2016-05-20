package org.quattor.pan.utils;

import java.io.IOException;

import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.ProtectedHashResource;
import org.quattor.pan.dml.data.ProtectedListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;

import static org.quattor.pan.utils.MessageUtils.MSG_FILE_BUG_REPORT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_JSON_UNDEF;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonUtils {
	private static final JsonUtils instance = new JsonUtils();

	private final Gson gson;

	private JsonUtils() {
		ElementAdapter elementAdapter = new ElementAdapter();

		gson = new GsonBuilder()
				.registerTypeAdapter(Element.class, elementAdapter)
				.registerTypeAdapter(Null.class, elementAdapter)
				.registerTypeAdapter(Undef.class, elementAdapter)
				.registerTypeAdapter(BooleanProperty.class, elementAdapter)
				.registerTypeAdapter(DoubleProperty.class, elementAdapter)
				.registerTypeAdapter(LongProperty.class, elementAdapter)
				.registerTypeAdapter(StringProperty.class, elementAdapter)
				.registerTypeAdapter(HashResource.class, elementAdapter)
				.registerTypeAdapter(ProtectedHashResource.class,
						elementAdapter)
				.registerTypeAdapter(ListResource.class, elementAdapter)
				.registerTypeAdapter(ProtectedListResource.class,
						elementAdapter)
				.setPrettyPrinting()
				.create();
	}

	public static JsonUtils getInstance() {
		return instance;
	}

	public String toJson(Element root) {
		try {
			return gson.toJson(root);
		} catch (JsonIOException jpe) {
			Throwable cause = jpe.getCause();
			if (cause instanceof EvaluationException) {
				throw (EvaluationException) cause;
			}
			throw jpe;
		}
	}

	public void toJson(Element root, Appendable writer) {
		try {
			gson.toJson(root, writer);
		} catch (JsonIOException jpe) {
			Throwable cause = jpe.getCause();
			if (cause instanceof EvaluationException) {
				throw (EvaluationException) cause;
			}
			throw jpe;
		}
	}

	private static class ElementAdapter extends TypeAdapter<Element> {

		@Override
		public Element read(final JsonReader in) throws IOException {
			/* XXX Not implemented yet - see next commit */
			return null;
		}

		@Override
		public void write(final JsonWriter out, final Element src)
				throws IOException {
			if (src instanceof Null) {
				out.nullValue();
			} else if (src instanceof Undef) {
				throw new JsonIOException(EvaluationException.create(MSG_INVALID_JSON_UNDEF));
			} else if (src instanceof BooleanProperty) {
				out.value(((BooleanProperty) src).getValue());
			} else if (src instanceof DoubleProperty) {
				out.value(((DoubleProperty) src).getValue());
			} else if (src instanceof LongProperty) {
				out.value(((LongProperty) src).getValue());
			} else if (src instanceof Property) {
				out.value(((Property) src).getValue().toString());
			} else if (src instanceof HashResource || src instanceof ProtectedHashResource) {
				out.beginObject();

				for (Resource.Entry entry : (HashResource) src) {
					String property = entry.getKey().toString();
					write(out.name(property), entry.getValue());
				}

				out.endObject();
			} else if (src instanceof ListResource || src instanceof ProtectedListResource) {
				out.beginArray();

				for (Resource.Entry entry : (ListResource) src) {
					write(out, entry.getValue());
				}

				out.endArray();
			} else {
				throw new JsonIOException(CompilerError.create(MSG_FILE_BUG_REPORT));
			}
		}
	}

}
