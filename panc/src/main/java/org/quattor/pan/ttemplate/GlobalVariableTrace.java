package org.quattor.pan.ttemplate;

import java.io.PrintStream;
import java.util.regex.Pattern;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.utils.Term;

/** Emits trace events for one selected object context. */
public final class GlobalVariableTrace {
    private static final Gson JSON = new GsonBuilder().setFieldNamingPolicy(
            FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).serializeNulls().create();
    private final String object;
    private final String objectFile;
    private final Pattern variables;
    private long sequence;

    public GlobalVariableTrace(Template object, String variableGlob) {
        this(object, compileGlob(variableGlob));
    }

    public GlobalVariableTrace(Template object, Pattern variablePattern) {
        this.object = object.name;
        this.objectFile = object.source == null ? null : object.source.getPath();
        this.variables = variablePattern;
    }

    /** Compile a case-sensitive glob matching an entire logical name. */
    public static Pattern compileGlob(String glob) {
        if (glob == null || glob.isEmpty()) throw new IllegalArgumentException("glob must not be empty");
        StringBuilder regex = new StringBuilder();
        for (int offset = 0; offset < glob.length();) {
            int codePoint = glob.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '*') regex.append(".*");
            else if (codePoint == '?') regex.append('.');
            else regex.append(Pattern.quote(new String(Character.toChars(codePoint))));
        }
        return Pattern.compile(regex.toString(), Pattern.DOTALL);
    }

    public boolean matchesVariable(String name) {
        return variables.matcher(name).matches();
    }

    private JsonObject event(Template template, SourceRange location, String action, String variable) {
        JsonObject event = new JsonObject();
        event.addProperty("format", "pan-global-trace");
        event.addProperty("version", 1);
        event.addProperty("sequence", ++sequence);
        event.addProperty("object", object);
        event.addProperty("object_file", objectFile);
        event.addProperty("template", template == null ? null : template.name);
        event.addProperty("template_file", template == null || template.source == null ? null : template.source.getPath());
        event.add("location", location == null ? JsonNull.INSTANCE : JSON.toJsonTree(location));
        event.addProperty("action", action);
        event.addProperty("variable", variable);
        return event;
    }

    /** Emit a read and optional evaluated access path. */
    public synchronized void read(Template template, SourceRange location, String variable,
            Term[] path, JsonObject value) {
        JsonObject event = event(template, location, "read", variable);
        if (path != null && path.length > 0) {
            JsonArray accessPath = new JsonArray();
            for (Term term : path) {
                JsonObject part;
                try {
                    if (term.isKey()) {
                        part = TraceValueWriter.string(term.getKey());
                    } else {
                        part = new JsonObject();
                        part.addProperty("type", "long");
                        part.addProperty("value", term.getIndex());
                    }
                } catch (InvalidTermException ignored) {
                    return;
                }
                accessPath.add(part);
            }
            event.add("access_path", accessPath);
        }
        event.add("value", value);
        emit(event);
    }

    /** Emit a successful assignment or a skipped conditional assignment. */
    public synchronized void write(Template template, SourceRange location, String variable,
            String action, boolean conditional, boolean isFinal, JsonObject oldValue, JsonObject newValue) {
        JsonObject event = event(template, location, action, variable);
        event.addProperty("conditional", conditional);
        event.addProperty("final", isFinal);
        if ("skip_default".equals(action)) event.add("existing", oldValue);
        else {
            event.add("old", oldValue);
            event.add("new", newValue);
        }
        emit(event);
    }

    private static void emit(JsonObject event) {
        String line = "PAN_GLOBAL_TRACE " + JSON.toJson(event);
        try {
            PrintStream stream = System.err;
            synchronized (stream) { stream.println(line); }
        } catch (RuntimeException ignored) {
            // Diagnostic output must not change a successful compilation.
        }
    }
}
