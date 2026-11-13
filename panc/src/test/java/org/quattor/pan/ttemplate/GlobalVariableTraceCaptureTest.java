package org.quattor.pan.ttemplate;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Test;

public class GlobalVariableTraceCaptureTest {
    @Test public void retainsPrefixAtEventLimitAndSnapshotsAreImmutable() {
        GlobalVariableTraceCapture.Builder builder = new GlobalVariableTraceCapture.Builder("obj*", "v*", true, 2, 100);
        builder.add("{\"n\":1}");
        GlobalVariableTraceCapture before = builder.snapshot();
        builder.add("{\"n\":2}");
        builder.add("{\"n\":3}");
        builder.add("{}");
        GlobalVariableTraceCapture capture = builder.snapshot();
        assertEquals(Arrays.asList("{\"n\":1}"), before.getEvents());
        assertEquals(Arrays.asList("{\"n\":1}", "{\"n\":2}"), capture.getEvents());
        assertEquals(2, capture.getDroppedEvents());
        assertFalse(capture.isComplete());
        assertEquals("obj*", capture.getObjectGlob());
        assertEquals("v*", capture.getVariableGlob());
        assertTrue(capture.isSelected());
        try { capture.getEvents().clear(); fail("mutable events"); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test public void countsUtf8SerializedBytesAndNeverResumesAfterRejection() {
        String event = "{\"value\":\"é\\n\"}";
        int size = event.getBytes(StandardCharsets.UTF_8).length;
        GlobalVariableTraceCapture.Builder builder = new GlobalVariableTraceCapture.Builder("*", "*", true, 5, size);
        builder.add(event);
        assertTrue(builder.snapshot().isComplete());
        builder.add("{}");
        builder.add("");
        assertEquals(Arrays.asList(event), builder.snapshot().getEvents());
        assertEquals(2, builder.snapshot().getDroppedEvents());
        GlobalVariableTraceCapture.Builder tooSmall = new GlobalVariableTraceCapture.Builder("*", "*", true, 5, size - 1);
        tooSmall.add(event);
        assertTrue(tooSmall.snapshot().getEvents().isEmpty());
        assertEquals(1, tooSmall.snapshot().getDroppedEvents());
    }

    @Test public void productionLimitsAreStable() {
        assertEquals(10000, GlobalVariableTraceCapture.MAX_EVENTS);
        assertEquals(8388608L, GlobalVariableTraceCapture.MAX_BYTES);
    }

    @Test public void saturationWarnsOnlyOnceWithoutPayload() {
        Logger logger = Logger.getLogger(GlobalVariableTraceCapture.Builder.class.getName());
        java.util.logging.Level previousLevel = logger.getLevel();
        logger.setLevel(java.util.logging.Level.WARNING);
        final java.util.List<String> warnings = new java.util.ArrayList<String>();
        Handler handler = new Handler() {
            @Override public void publish(LogRecord record) { warnings.add(record.getMessage()); }
            @Override public void flush() { }
            @Override public void close() { }
        };
        logger.addHandler(handler);
        try {
            GlobalVariableTraceCapture.Builder builder = new GlobalVariableTraceCapture.Builder("*", "*", true, 0, 0);
            builder.add("sensitive payload");
            builder.drop();
            assertEquals(1, warnings.size());
            assertFalse(warnings.get(0).contains("sensitive"));
        } finally { logger.removeHandler(handler); logger.setLevel(previousLevel); }
    }
}
