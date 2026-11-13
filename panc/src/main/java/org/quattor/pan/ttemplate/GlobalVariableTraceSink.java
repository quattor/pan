package org.quattor.pan.ttemplate;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

/** Invocation-owned, exclusively locked destination for global trace lines. */
public final class GlobalVariableTraceSink implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;
    private UncheckedIOException failure;

    private GlobalVariableTraceSink(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /** Acquire the file lock before replacing any previous trace. */
    public static GlobalVariableTraceSink open(File outputDirectory) {
        FileChannel channel = null;
        try {
            channel = FileChannel.open(new File(outputDirectory, "trace.log").toPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock;
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                throw new IOException("trace file is already locked", e);
            }
            if (lock == null) throw new IOException("trace file is already locked");
            channel.truncate(0);
            return new GlobalVariableTraceSink(channel, lock);
        } catch (IOException | RuntimeException e) {
            if (channel != null) {
                try { channel.close(); } catch (IOException closeError) { e.addSuppressed(closeError); }
            }
            if (e instanceof IOException) throw new UncheckedIOException("cannot open trace file", (IOException) e);
            throw (RuntimeException) e;
        }
    }

    /** Write one complete UTF-8 line, serializing concurrent collectors. */
    public synchronized void emit(String line) {
        throwIfFailed();
        try {
            ByteBuffer bytes = ByteBuffer.wrap((line + "\n").getBytes(StandardCharsets.UTF_8));
            while (bytes.hasRemaining()) channel.write(bytes);
        } catch (IOException e) {
            failure = new UncheckedIOException("cannot write trace file", e);
            throw failure;
        }
    }

    /** Report any earlier write failure even if a worker handled it. */
    public synchronized void throwIfFailed() {
        if (failure != null) throw failure;
    }

    /** Release the lock and file without deleting the trace. */
    @Override public synchronized void close() {
        IOException error = null;
        try { lock.release(); } catch (IOException e) { error = e; }
        try { channel.close(); } catch (IOException e) {
            if (error == null) error = e; else error.addSuppressed(e);
        }
        if (error != null) throw new UncheckedIOException("cannot close trace file", error);
    }
}
