package org.quattor.pan.ttemplate;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.quattor.pan.dml.data.*;

public class TraceValueWriterTest {
    private static String type(Element value) {
        return TraceValueWriter.snapshot(value).get("type").getAsString();
    }

    @Test public void typedValuesRemainDistinct() {
        assertEquals("absent", type(null));
        assertEquals("null", type(Null.VALUE));
        assertEquals("undef", type(Undef.VALUE));
        assertEquals("string", type(StringProperty.getInstance("null")));
        assertEquals("", TraceValueWriter.snapshot(StringProperty.getInstance("")).get("value").getAsString());
        ListResource list = new ListResource(new Element[] { LongProperty.getInstance(1),
                BooleanProperty.TRUE, new ListResource(new Element[] { DoubleProperty.getInstance(1.5) }) });
        JsonArray items = TraceValueWriter.snapshot(list).getAsJsonArray("value");
        assertEquals("long", items.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("boolean", items.get(1).getAsJsonObject().get("type").getAsString());
        assertEquals("double", items.get(2).getAsJsonObject().getAsJsonArray("value").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test public void stringsAndKeysAreEscaped() throws Exception {
        HashResource dict = new HashResource();
        dict.put(StringProperty.getInstance("key\n\\n\""), StringProperty.getInstance("\n\\n\""));
        JsonObject json = new JsonParser().parse(TraceValueWriter.snapshot(dict).toString()).getAsJsonObject();
        JsonObject entry = json.getAsJsonArray("value").get(0).getAsJsonObject();
        assertEquals("key\n\\n\"", entry.getAsJsonObject("key").get("value").getAsString());
        assertEquals("\n\\n\"", entry.getAsJsonObject("value").get("value").getAsString());
    }

    @Test public void boundsAndCyclesAreExplicit() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 4097; i++) { text.append("\ud83d\ude00"); }
        JsonObject string = TraceValueWriter.snapshot(StringProperty.getInstance(text.toString()));
        assertEquals("string", string.get("reason").getAsString());
        assertEquals(4097, string.get("size").getAsInt());
        assertEquals(4096, string.get("value").getAsString().codePointCount(0, string.get("value").getAsString().length()));
        ListResource cycle = new ListResource();
        cycle.append(cycle);
        JsonObject cyclic = TraceValueWriter.snapshot(cycle).getAsJsonArray("value").get(0).getAsJsonObject();
        assertEquals("cycle", cyclic.get("reason").getAsString());
        ListResource root = new ListResource();
        ListResource current = root;
        for (int i = 0; i < 17; i++) { ListResource child = new ListResource(); current.append(child); current = child; }
        JsonObject deep = TraceValueWriter.snapshot(root);
        for (int i = 0; i < 16; i++) { deep = deep.getAsJsonArray("value").get(0).getAsJsonObject(); }
        assertEquals("depth", deep.get("reason").getAsString());
        CountingList many = new CountingList();
        for (int i = 0; i < 2000; i++) { many.append(LongProperty.getInstance(i)); }
        JsonObject limited = TraceValueWriter.snapshot(many);
        assertEquals("nodes", limited.get("reason").getAsString());
        assertEquals(1023, many.visited);
        assertEquals(2000, limited.get("size").getAsInt());
        assertEquals(1023, TraceValueWriter.snapshot(many).getAsJsonArray("value").size());
    }

    @Test public void snapshotDoesNotMutateOrProtectResources() {
        CountingList list = new CountingList();
        list.append(StringProperty.getInstance("value"));
        TraceValueWriter.snapshot(list);
        assertEquals(1, list.size());
        assertFalse(list.isProtected());
        assertEquals(1, list.visited);
    }

    @Test public void nonfiniteDoubleIsValidJson() {
        for (double value : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
            JsonObject json = new JsonParser().parse(TraceValueWriter.snapshot(DoubleProperty.getInstance(value)).toString()).getAsJsonObject();
            assertEquals("double", json.get("type").getAsString());
            assertEquals(Double.toString(value), json.get("value").getAsString());
        }
    }

    @Test public void sharedChildrenAreNotCyclesAndKeysHaveCodePointLimits() throws Exception {
        ListResource child = new ListResource(new Element[] { LongProperty.getInstance(3) });
        ListResource parent = new ListResource(new Element[] { child, child });
        JsonArray values = TraceValueWriter.snapshot(parent).getAsJsonArray("value");
        assertEquals(3, values.get(1).getAsJsonObject().getAsJsonArray("value").get(0).getAsJsonObject().get("value").getAsInt());
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 4097; i++) key.append("\ud83d\ude00");
        HashResource dict = new HashResource();
        dict.put(StringProperty.getInstance(key.toString()), child);
        JsonObject result = TraceValueWriter.snapshot(dict).getAsJsonArray("value").get(0).getAsJsonObject().getAsJsonObject("key");
        assertEquals("string", result.get("reason").getAsString());
        assertEquals(4097, result.get("size").getAsInt());
        String truncated = result.get("value").getAsString();
        assertEquals(4096, truncated.codePointCount(0, truncated.length()));
    }

    @Test public void unsupportedElementsAreMarkedWithoutRendering() {
        Element value = (Element) Proxy.newProxyInstance(Element.class.getClassLoader(),
                new Class<?>[] { Element.class }, (proxy, method, args) -> {
                    throw new AssertionError("Unexpected element method: " + method.getName());
                });
        JsonObject result = TraceValueWriter.snapshot(value);
        assertEquals("unsupported", result.get("type").getAsString());
        assertEquals(value.getClass().getName(), result.get("class").getAsString());
    }

    private static class CountingList extends ListResource {
        int visited;
        @Override public Element protect() { fail("protect called"); return null; }
        @Override public Element duplicate() { fail("duplicate called"); return null; }
        @Override public Resource.Iterator protectedIterator() { fail("protected iterator called"); return null; }
        @Override public Resource.Iterator iterator() {
            final Resource.Iterator delegate = super.iterator();
            return new Resource.Iterator() {
                public boolean hasNext() { return delegate.hasNext(); }
                public Resource.Entry next() { visited++; return delegate.next(); }
                public void remove() { fail("remove called"); }
            };
        }
    }
}
