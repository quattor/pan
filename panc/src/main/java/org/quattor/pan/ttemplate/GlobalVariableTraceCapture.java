package org.quattor.pan.ttemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** Immutable, bounded serialized global trace for one profile. */
public final class GlobalVariableTraceCapture {
    public static final int MAX_EVENTS = 10000;
    public static final long MAX_BYTES = 8388608L;
    private final String objectGlob, variableGlob;
    private final boolean selected, complete;
    private final long droppedEvents, maxBytes;
    private final int maxEvents;
    private final List<String> events;

    private GlobalVariableTraceCapture(Builder builder) {
        objectGlob = builder.objectGlob;
        variableGlob = builder.variableGlob;
        selected = builder.selected;
        complete = builder.droppedEvents == 0;
        droppedEvents = builder.droppedEvents;
        maxEvents = builder.maxEvents;
        maxBytes = builder.maxBytes;
        events = Collections.unmodifiableList(new ArrayList<String>(builder.events));
    }

    public String getObjectGlob() { return objectGlob; }
    public String getVariableGlob() { return variableGlob; }
    public boolean isSelected() { return selected; }
    public boolean isComplete() { return complete; }
    public long getDroppedEvents() { return droppedEvents; }
    public List<String> getEvents() { return events; }
    public int getMaxEvents() { return maxEvents; }
    public long getMaxBytes() { return maxBytes; }

    /** Profile-local mutable prefix storage. */
    public static final class Builder {
        private static final Logger LOGGER = Logger.getLogger(Builder.class.getName());
        private final String objectGlob, variableGlob;
        private final boolean selected;
        private final int maxEvents;
        private final long maxBytes;
        private final List<String> events = new ArrayList<String>();
        private long bytes, droppedEvents;

        public Builder(String objectGlob, String variableGlob, boolean selected, int maxEvents, long maxBytes) {
            if (maxEvents < 0 || maxBytes < 0) throw new IllegalArgumentException("negative trace limit");
            this.objectGlob = objectGlob;
            this.variableGlob = variableGlob;
            this.selected = selected;
            this.maxEvents = maxEvents;
            this.maxBytes = maxBytes;
        }

        /** Whether another event could fit without examining its payload. */
        public boolean needsPayload() {
            return droppedEvents == 0 && events.size() < maxEvents && bytes < maxBytes;
        }

        /** Retain a serialized event if it fits; otherwise permanently drop the suffix. */
        public void add(String event) {
            long size = event.getBytes(StandardCharsets.UTF_8).length;
            if (droppedEvents != 0 || events.size() >= maxEvents || size > maxBytes - bytes) {
                drop();
            } else {
                events.add(event);
                bytes += size;
            }
        }

        /** Account for an event omitted without constructing a payload. */
        public void drop() {
            if (droppedEvents++ == 0) LOGGER.warning("Global trace capture limit reached; subsequent events omitted");
        }

        public GlobalVariableTraceCapture snapshot() { return new GlobalVariableTraceCapture(this); }
    }
}
