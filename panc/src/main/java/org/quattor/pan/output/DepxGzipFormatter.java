package org.quattor.pan.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/** Gzip-compressed UTF-8 runtime include trace and file dependencies. */
public class DepxGzipFormatter extends DepxFormatter {
    private static final DepxGzipFormatter instance = new DepxGzipFormatter();

    private DepxGzipFormatter() {
        super("depx.gz", "depx.gz");
    }

    /** Returns the shared compressed formatter. */
    public static DepxGzipFormatter getInstance() {
        return instance;
    }

    @Override
    protected PrintWriter getPrintWriter(File file) throws Exception {
        OutputStream os = new FileOutputStream(file);
        OutputStream gzip = new GZIPOutputStream(os);
        return new PrintWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8));
    }
}
