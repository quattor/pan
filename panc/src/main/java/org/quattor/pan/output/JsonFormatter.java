/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatter.java $
 $Id: PanFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import java.io.PrintWriter;
import java.lang.reflect.Type;

import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Property;
import org.quattor.pan.dml.data.ProtectedHashResource;
import org.quattor.pan.dml.data.ProtectedListResource;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.tasks.FinalResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonFormatter extends AbstractFormatter {

	private static final JsonFormatter instance = new JsonFormatter();

	private JsonFormatter() {
		super("json", "json");
	}
	
	protected JsonFormatter(String suffix, String key) {
		super(suffix, key);
	}

	public static JsonFormatter getInstance() {
		return instance;
	}

	protected void write(FinalResult result, PrintWriter ps) throws Exception {

		Gson gson = new GsonBuilder()
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
						new ListSerializer()).setPrettyPrinting().create();

		ps.write(gson.toJson(result.getRoot()));
		ps.close();

	}

	private class PropertySerializer implements JsonSerializer<Property> {
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

	private class HashSerializer implements JsonSerializer<HashResource> {

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

	private class ListSerializer implements JsonSerializer<ListResource> {

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
