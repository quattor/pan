package org.quattor.pan.ttemplate;

import static org.junit.Assert.*;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.quattor.pan.utils.TestUtils;

public class GlobalVariableTraceSinkTest {
    @Test public void lockContentionNeverTruncatesAndClosePreservesFile() throws Exception {
        File directory = Files.createTempDirectory(TestUtils.getTmpdir().toPath(), "trace-lock-").toFile();
        File log = new File(directory, "trace.log");
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(directory)) {
            sink.emit("first é");
            AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
            Thread contender = new Thread(() -> {
                try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(directory)) {
                    failure.set(new AssertionError("second sink acquired lock"));
                } catch (UncheckedIOException expected) { }
            });
            contender.start();
            contender.join();
            assertNull(failure.get());
            assertEquals("first é\n", new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8));
        }
        assertTrue(log.exists());
        try (GlobalVariableTraceSink ignored = GlobalVariableTraceSink.open(directory)) { }
        assertEquals(0, log.length());
    }

    @Test public void concurrentWritersEmitCompleteUtf8Lines() throws Exception {
        File directory = Files.createTempDirectory(TestUtils.getTmpdir().toPath(), "trace-parallel-").toFile();
        final int threads = 4, count = 100;
        try (GlobalVariableTraceSink sink = GlobalVariableTraceSink.open(directory)) {
            CountDownLatch start = new CountDownLatch(1);
            Thread[] workers = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                final int id = i;
                workers[i] = new Thread(() -> {
                    try { start.await(); } catch (InterruptedException e) { throw new AssertionError(e); }
                    for (int j = 0; j < count; j++) sink.emit("PAN_GLOBAL_TRACE é-" + id + "-" + j);
                });
                workers[i].start();
            }
            start.countDown();
            for (Thread worker : workers) worker.join();
        }
        java.util.List<String> lines = Files.readAllLines(new File(directory, "trace.log").toPath(), StandardCharsets.UTF_8);
        assertEquals(threads * count, lines.size());
        assertEquals(threads * count, new java.util.HashSet<String>(lines).size());
        for (String line : lines) assertTrue(line.matches("PAN_GLOBAL_TRACE é-[0-3]-[0-9]+"));
    }

    @Test public void openingDirectoryAsLogFailsClearly() throws Exception {
        File directory = Files.createTempDirectory(TestUtils.getTmpdir().toPath(), "trace-open-").toFile();
        assertTrue(new File(directory, "trace.log").mkdir());
        try {
            GlobalVariableTraceSink.open(directory);
            fail("directory cannot be opened as trace log");
        } catch (UncheckedIOException expected) {
            assertTrue(expected.getMessage().contains("trace"));
        }
    }
}
