package org.quattor.pan.output;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.tasks.FinalResult;
import org.quattor.pan.ttemplate.IncludeTrace;
import org.quattor.pan.ttemplate.SourceRange;
import org.quattor.pan.ttemplate.Template;

/** Streams independently versioned JSON from the existing compact include trace. */
public class DepxFormatter extends AbstractFormatter {
    private static final DepxFormatter INSTANCE = new DepxFormatter();
    private static final Gson RANGE_JSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private DepxFormatter() {
        this("depx", "depx");
    }

    protected DepxFormatter(String suffix, String key) {
        super(suffix, key);
    }

    /** Returns the shared JSON formatter. */
    public static DepxFormatter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean requiresIncludeTrace() {
        return true;
    }

    @Override
    protected void write(FinalResult result, PrintWriter writer) throws IOException {
        JsonWriter json = new JsonWriter(writer);
        json.setSerializeNulls(true);
        List<File> roots = result.getIncludeRoots();
        json.beginObject();
        json.name("format").value("depx");
        json.name("version").value(1);
        json.name("roots").beginArray();
        for (File root : roots) {
            json.value(root.toPath().toAbsolutePath().normalize().toString());
        }
        json.endArray();
        json.name("profiles").beginArray();
        for (Map.Entry<String, IncludeTrace> profile : new TreeMap<String, IncludeTrace>(
                result.getIncludeTraces()).entrySet()) {
            json.beginObject();
            json.name("name").value(profile.getKey());
            IncludeTrace trace = profile.getValue();
            json.name("sites").beginArray();
            for (IncludeTrace.Metadata site : trace.getMetadata()) {
                json.beginObject();
                json.name("caller");
                writeTemplate(json, site.caller, roots);
                json.name("target");
                writeTemplate(json, site.target, roots);
                json.name("location");
                // Gson streams the compiler's private coordinate fields without a JSON mirror.
                RANGE_JSON.toJson(site.sourceRange, SourceRange.class, json);
                json.endObject();
            }
            json.endArray();
            json.name("occurrences").beginArray();
            for (int i = 0; i < trace.size(); i++) {
                json.beginObject();
                json.name("site").value(trace.getMetadataIndex(i));
                json.name("parent");
                int parent = trace.getParentIndex(i);
                if (parent < 0) { json.nullValue(); }
                else { json.value(parent); }
                json.name("status").value(trace.getStatus(i).name().toLowerCase(Locale.ROOT));
                json.endObject();
            }
            json.endArray();
            json.endObject();
        }
        json.endArray();
        json.name("dependencies").beginArray();
        for (SourceFile source : result.getDependencies()) {
            writeSource(json, source, roots);
        }
        json.endArray();
        json.endObject();
        json.flush();
    }

    private static void writeTemplate(JsonWriter json, Template template, List<File> roots) throws IOException {
        if (template == null) {
            json.nullValue();
            return;
        }
        json.beginObject();
        json.name("name").value(template.name);
        json.name("source");
        writeSource(json, template.source == null ? null : template.sourceFile, roots);
        json.endObject();
    }

    /** Writes structured source metadata, retaining literal root-relative load directories. */
    static void writeSource(JsonWriter json, SourceFile source, List<File> roots) throws IOException {
        if (source == null) {
            json.nullValue();
            return;
        }
        json.beginObject();
        json.name("name").value(source.getName());
        json.name("type").value(source.getType().toString());
        json.name("location");
        File file = source.getLocation();
        if (file == null) {
            json.nullValue();
        } else {
            Path location = file.toPath().normalize();
            int matched = -1;
            for (int i = 0; i < roots.size(); i++) {
                if (location.startsWith(roots.get(i).toPath().toAbsolutePath().normalize())) {
                    matched = i;
                    break;
                }
            }
            json.beginObject();
            if (matched >= 0) {
                Path root = roots.get(matched).toPath().toAbsolutePath().normalize();
                json.name("root").value(matched);
                json.name("path").value(root.relativize(location).toString().replace(File.separatorChar, '/'));
            } else {
                json.name("uri").value(file.toURI().toASCIIString());
            }
            json.endObject();
        }
        json.endObject();
    }
}
