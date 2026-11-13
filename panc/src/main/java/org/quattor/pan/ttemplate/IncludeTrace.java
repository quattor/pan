package org.quattor.pan.ttemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Immutable per-profile execution occurrences; metadata shares compiler references. */
public final class IncludeTrace {
    public enum Status { EXECUTED, SKIPPED_UNIQUE, SKIPPED_DECLARATION, CREATE }
    private static final Status[] STATUSES = Status.values();

    /** A shared include site, rather than a copy of names and paths. */
    public static final class Metadata {
        public final Template caller;
        public final Template target;
        public final SourceRange sourceRange;

        private Metadata(Template caller, Template target, SourceRange sourceRange) {
            this.caller = caller;
            this.target = target;
            this.sourceRange = sourceRange;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Metadata)) { return false; }
            Metadata site = (Metadata) other;
            return caller == site.caller && target == site.target && sourceRange == site.sourceRange;
        }

        @Override
        public int hashCode() {
            return (System.identityHashCode(caller) * 31 + System.identityHashCode(target)) * 31
                    + System.identityHashCode(sourceRange);
        }
    }

    private final List<Metadata> metadata;
    private final int[] sites;
    private final int[] parents;
    private final byte[] statuses;

    private IncludeTrace(Builder builder) {
        metadata = Collections.unmodifiableList(new ArrayList<Metadata>(builder.metadata));
        sites = Arrays.copyOf(builder.sites, builder.size);
        parents = Arrays.copyOf(builder.parents, builder.size);
        statuses = Arrays.copyOf(builder.statuses, builder.size);
    }

    /** Returns the number of execution occurrences. */
    public int size() {
        return sites.length;
    }

    /** Returns immutable shared include-site metadata. */
    public List<Metadata> getMetadata() {
        return metadata;
    }

    /** Returns the metadata index for an occurrence. */
    public int getMetadataIndex(int occurrence) {
        return sites[occurrence];
    }

    /** Returns the parent occurrence index, or -1 for a root. */
    public int getParentIndex(int occurrence) {
        return parents[occurrence];
    }

    /** Returns execution or suppression status without allocating an enum array. */
    public Status getStatus(int occurrence) {
        return STATUSES[statuses[occurrence]];
    }

    /** Mutable storage owned solely by the build context. */
    public static final class Builder {
        private final List<Metadata> metadata = new ArrayList<Metadata>();
        private final Map<Metadata, Integer> indexes = new HashMap<Metadata, Integer>();
        private int[] sites = new int[16];
        private int[] parents = new int[16];
        private byte[] statuses = new byte[16];
        private int size;
        private int parent = -1;

        /** Returns the active parent for restoration after execution. */
        public int getParent() {
            return parent;
        }

        /** Appends an occurrence and makes it the active parent. */
        public int enter(Template caller, Template target, SourceRange range, Status status) {
            Metadata site = new Metadata(caller, target, range);
            Integer index = indexes.get(site);
            if (index == null) {
                index = metadata.size();
                metadata.add(site);
                indexes.put(site, index);
            }
            if (size == sites.length) {
                sites = Arrays.copyOf(sites, size * 2);
                parents = Arrays.copyOf(parents, size * 2);
                statuses = Arrays.copyOf(statuses, size * 2);
            }
            sites[size] = index;
            parents[size] = parent;
            statuses[size] = (byte) status.ordinal();
            parent = size++;
            return parent;
        }

        /** Restores the parent saved before entering an occurrence. */
        public void leave(int previousParent) {
            parent = previousParent;
        }

        /** Copies primitive storage into an immutable validation result. */
        public IncludeTrace snapshot() {
            return new IncludeTrace(this);
        }
    }
}
