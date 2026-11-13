package org.quattor.pan.ttemplate;

import java.util.IdentityHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;

/** Read-only, bounded snapshots of Pan values for diagnostic tracing. */
public final class TraceValueWriter {
    private static final int MAX_DEPTH = 16;
    private static final int MAX_NODES = 1024;
    private static final int MAX_STRING_CODE_POINTS = 4096;

    private int nodes;
    private final IdentityHashMap<Element, Boolean> ancestors = new IdentityHashMap<Element, Boolean>();

    private TraceValueWriter() { }

    /** Render one value with a fresh independent traversal budget. */
    public static JsonObject snapshot(Element value) {
        return new TraceValueWriter().render(value, 0);
    }

    private static JsonObject typed(String type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        return json;
    }

    private static void truncated(JsonObject json, String reason, int size) {
        json.addProperty("truncated", true);
        json.addProperty("reason", reason);
        json.addProperty("size", size);
    }

    static JsonObject string(String value) {
        JsonObject json = typed("string");
        int size = value.codePointCount(0, value.length());
        if (size > MAX_STRING_CODE_POINTS) {
            json.addProperty("value", value.substring(0, value.offsetByCodePoints(0, MAX_STRING_CODE_POINTS)));
            truncated(json, "string", size);
        } else {
            json.addProperty("value", value);
        }
        return json;
    }

    private JsonObject render(Element value, int depth) {
        nodes++;
        if (value == null) return typed("absent");
        if (value instanceof Null) return typed("null");
        if (value instanceof Undef) return typed("undef");
        if (value instanceof StringProperty) return string(((StringProperty) value).getValue());
        if (value instanceof LongProperty) {
            JsonObject json = typed("long");
            json.addProperty("value", ((LongProperty) value).getValue());
            return json;
        }
        if (value instanceof DoubleProperty) {
            JsonObject json = typed("double");
            double number = ((DoubleProperty) value).getValue();
            if (Double.isNaN(number) || Double.isInfinite(number)) json.addProperty("value", Double.toString(number));
            else json.addProperty("value", number);
            return json;
        }
        if (value instanceof BooleanProperty) {
            JsonObject json = typed("boolean");
            json.addProperty("value", ((BooleanProperty) value).getValue());
            return json;
        }
        if (value instanceof ListResource || value instanceof HashResource) {
            Resource resource = (Resource) value;
            boolean dict = value instanceof HashResource;
            JsonObject json = typed(dict ? "dict" : "list");
            if (ancestors.containsKey(value)) {
                truncated(json, "cycle", resource.size());
                return json;
            }
            if (depth >= MAX_DEPTH) {
                truncated(json, "depth", resource.size());
                return json;
            }
            JsonArray contents = new JsonArray();
            json.add("value", contents);
            ancestors.put(value, Boolean.TRUE);
            try {
                Resource.Iterator iterator = resource.iterator();
                while (iterator.hasNext()) {
                    if (nodes >= MAX_NODES) {
                        truncated(json, "nodes", resource.size());
                        break;
                    }
                    Resource.Entry entry = iterator.next();
                    if (dict) {
                        JsonObject item = new JsonObject();
                        item.add("key", string(((StringProperty) entry.getKey()).getValue()));
                        item.add("value", render(entry.getValue(), depth + 1));
                        contents.add(item);
                    } else {
                        contents.add(render(entry.getValue(), depth + 1));
                    }
                }
            } finally {
                ancestors.remove(value);
            }
            return json;
        }
        JsonObject json = typed("unsupported");
        json.addProperty("class", value.getClass().getName());
        return json;
    }
}
