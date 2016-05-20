package org.quattor.pan.utils;

import java.lang.reflect.Type;

import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.ProtectedHashResource;
import org.quattor.pan.dml.data.ProtectedListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonUtils {
	private static final JsonUtils instance = new JsonUtils();

	private final Gson gson;

	private JsonUtils() {
		gson = new GsonBuilder()
				.registerTypeAdapter(BooleanProperty.class,
						new PropertySerializer())
				.registerTypeAdapter(DoubleProperty.class,
						new PropertySerializer())
				.registerTypeAdapter(LongProperty.class,
						new PropertySerializer())
				.registerTypeAdapter(StringProperty.class,
						new PropertySerializer())
				.registerTypeAdapter(HashResource.class, new HashSerializer())
				.registerTypeAdapter(ProtectedHashResource.class,
						new HashSerializer())
				.registerTypeAdapter(ListResource.class, new ListSerializer())
				.registerTypeAdapter(ProtectedListResource.class,
						new ListSerializer())
				.setPrettyPrinting()
				.create();
	}

	public static JsonUtils getInstance() {
		return instance;
	}

	public String toJson(Element root) throws Exception {
		return gson.toJson(root);
	}

	private static class PropertySerializer implements JsonSerializer<Property> {
		public JsonElement serialize(Property src, Type typeOfSrc,
				JsonSerializationContext context) {

			if (src instanceof BooleanProperty) {
				return new JsonPrimitive(((BooleanProperty) src).getValue());
			} else if (src instanceof DoubleProperty) {
				return new JsonPrimitive(((DoubleProperty) src).getValue());
			} else if (src instanceof LongProperty) {
				return new JsonPrimitive(((LongProperty) src).getValue());
			} else {
				return new JsonPrimitive(src.getValue().toString());
			}
		}
	}

	private static class HashSerializer implements JsonSerializer<HashResource> {

		public JsonElement serialize(HashResource src, Type typeOfSrc,
				JsonSerializationContext context) {

			JsonObject map = new JsonObject();

			for (Resource.Entry entry : src) {
				String property = entry.getKey().toString();
				JsonElement value = context.serialize(entry.getValue());
				map.add(property, value);
			}

			return map;
		}
	}

	private static class ListSerializer implements JsonSerializer<ListResource> {

		public JsonElement serialize(ListResource src, Type typeOfSrc,
				JsonSerializationContext context) {

			JsonArray array = new JsonArray();

			for (Resource.Entry entry : src) {
				JsonElement value = context.serialize(entry.getValue());
				array.add(value);
			}

			return array;
		}
	}

}
