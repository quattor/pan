package org.quattor.pan.output;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.CompilerResults;
import org.quattor.pan.tasks.FinalResult;
import org.quattor.pan.tasks.Valid2Result;
import org.quattor.pan.ttemplate.IncludeTrace;
import org.quattor.pan.ttemplate.GlobalVariableTraceCapture;
import org.quattor.pan.ttemplate.Template;
import org.quattor.pan.utils.TestUtils;
import static org.junit.Assert.*;

public class DepxGzipFormatterTest {
    @Test
    public void compressedBytesMatchUtf8DepxIncludingUnicode() throws Exception {
        IncludeTrace.Builder builder = new IncludeTrace.Builder();
        builder.enter(new Template("root"), new Template("child"), null, IncludeTrace.Status.EXECUTED);
        GlobalVariableTraceCapture.Builder capture = new GlobalVariableTraceCapture.Builder("*", "*", true, 10, 1000);
        capture.add("{\"sequence\":1,\"variable\":\"é\"}");
        FinalResult result = new FinalResult(null, new Valid2Result("røøt", null,
                Collections.<String>emptySet(), null, builder.snapshot(), capture.snapshot()));
        File plain = File.createTempFile("depx-", ".depx", TestUtils.getTmpdir());
        File compressed = new File(plain.toString() + ".gz");
        try {
            DepxFormatter.getInstance().write(result, plain.toURI());
            DepxGzipFormatter.getInstance().write(result, compressed.toURI());
            byte[] expected = Files.readAllBytes(plain.toPath());
            assertTrue(new String(expected, StandardCharsets.UTF_8).contains("røøt"));
            assertArrayEquals(expected, decompress(compressed));
        } finally {
            Files.deleteIfExists(plain.toPath());
            Files.deleteIfExists(compressed.toPath());
        }
    }

    @Test
    public void registersCompressedOptInSingleton() {
        assertEquals("depx.gz", DepxGzipFormatter.getInstance().getFormatKey());
        assertEquals("root.depx.gz", DepxGzipFormatter.getInstance().getResultURI("root").toString());
        assertSame(DepxGzipFormatter.getInstance(), FormatterUtils.getFormatterInstance("depx.gz"));
        assertEquals(Collections.singleton(DepxGzipFormatter.getInstance()), CompilerOptions.getFormatters("depx.gz"));
        assertTrue(DepxGzipFormatter.getInstance().requiresIncludeTrace());
    }

    @Test
    public void gzipOnlyCompilationCollectsTraceWithoutPlainOutput() throws Exception {
        File output = Files.createTempDirectory(TestUtils.getTmpdir().toPath(), "depx-gzip-").toFile();
        CompilerOptions options = new CompilerOptions(null, null, 100, 50,
                CompilerOptions.getFormatters("depx.gz"), output,
                Collections.singletonList(new File(System.getProperty("panc.testdir"), "depx")),
                CompilerOptions.DeprecationWarnings.OFF, null, null, null, 0);
        assertTrue(options.requiresIncludeTrace());
        CompilerResults compiled = org.quattor.pan.Compiler.run(options,
                Collections.singletonList("trace_context"), Collections.<File>emptyList());
        assertTrue(compiled.getErrors().toString(), compiled.getErrors().isEmpty());
        String text = new String(decompress(new File(output, "trace_context.depx.gz")), StandardCharsets.UTF_8);
        com.google.gson.JsonObject json = new com.google.gson.JsonParser().parse(text).getAsJsonObject();
        assertEquals("depx", json.get("format").getAsString());
        assertEquals(1, json.get("version").getAsInt());
        assertTrue(json.getAsJsonArray("profiles").get(0).getAsJsonObject()
                .getAsJsonArray("occurrences").size() > 0);
        assertTrue(text.contains("trace_payload.txt"));
        assertFalse(new File(output, "trace_context.depx").exists());
    }

    private byte[] decompress(File file) throws Exception {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file));
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }
}
