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
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.TermFactory;

import static org.quattor.pan.utils.MessageUtils.MSG_FILE_BUG_REPORT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_JSON_UNDEF;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_JSON_VALUE;
import static org.quattor.pan.utils.MessageUtils.MSG_JSON_INVALID_PAN_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_VALUE_AT_PATH_UNDEFINED;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

public class JsonUtils {
	private static final JsonUtils instance = new JsonUtils();

	private final Gson gson;

	private JsonUtils() {
		ElementAdapter elementAdapter = new ElementAdapter();

		/* For decoding, Element.class needs to be registered, so we
		 * don't need to know the exact type beforehand.  For encoding,
		 * all subclasses of Element which the compiler may pass to
		 * toJson() need to be registered. */
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

	public Element fromJson(String s) {
		try {
			return gson.fromJson(s, Element.class);
		} catch (JsonParseException jpe) {
			Throwable cause = jpe.getCause();
			if (cause instanceof EvaluationException) {
				throw (EvaluationException) cause;
			}
			throw EvaluationException.create(MSG_INVALID_JSON_VALUE,
					s, jpe.getMessage());
		}
	}

	private static class ElementAdapter extends TypeAdapter<Element> {

		@Override
		public Element read(final JsonReader in) throws IOException {
			JsonToken peek = in.peek();
			switch (peek) {
				case BOOLEAN:
					return BooleanProperty.getInstance(in.nextBoolean());
				case STRING:
					return StringProperty.getInstance(in.nextString());
				case NUMBER:
					String s = in.nextString();
					if (s.indexOf(".") < 0) {
						return LongProperty.getInstance(s);
					} else {
						return DoubleProperty.getInstance(s);
					}
				case NULL:
					in.nextNull();
					return Null.getInstance();
				case BEGIN_ARRAY:
					ListResource list = new ListResource();
					in.beginArray();
					while (in.hasNext()) {
						list.append(read(in));
					}
					in.endArray();
					return list;
				case BEGIN_OBJECT:
					HashResource dict = new HashResource();
					in.beginObject();
					while (in.hasNext()) {
						Term key = TermFactory.create(in.nextName());
						try {
							dict.put(key, read(in));
						} catch (InvalidTermException ite) {
							throw new JsonParseException(EvaluationException.create(MSG_JSON_INVALID_PAN_PATH,
									in.getPath()));
						}
					}
					in.endObject();
					return dict;
				default:
					throw new JsonParseException(CompilerError.create(MSG_FILE_BUG_REPORT));
			}
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
