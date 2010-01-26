package org.quattor.pan.parser.annotation;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ANNOTATION_SYNTAX;

import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quattor.pan.annotation.Annotation;
import org.quattor.pan.annotation.Annotation.Entry;
import org.quattor.pan.utils.MessageUtils;

public class AnnotationProcessor {

	private static final Pattern isKeyPairSyntax = Pattern.compile(
			"^(\\s*)([^=\\n\\r\\f]*)=(.*)", Pattern.DOTALL);

	private static final Pattern isAnnotationSyntax = Pattern
			.compile(
					"^\\s*@\\s*([a-zA-Z_0-9\\-\\.]*)\\s*(?:[\\{\\[\\(])(.*)(?:[\\}\\]\\)])\\s*$",
					Pattern.DOTALL);

	private AnnotationProcessor() {

	}

	public static Annotation process(String s) throws ParseException {

		List<Entry> entries = null;

		Matcher m = isAnnotationSyntax.matcher(s);

		if (m.matches()) {

			assert (m.groupCount() == 2);

			String name = m.group(1);
			String contents = m.group(2);

			// Try a descriptive annotation first.
			entries = parseAsDesc(contents);

			// If not, then an annotation with key-value pairs.
			if (entries == null) {
				Reader reader = new StringReader(contents);
				AnnotationParser parser = new AnnotationParser(reader);
				try {
					entries = parser.annotation();
				} catch (IllegalArgumentException e) {
					throw new ParseException(e.getLocalizedMessage());
				}
			}

			if (entries == null) {
				String msg = MessageUtils.format(MSG_INVALID_ANNOTATION_SYNTAX);
				throw new ParseException(msg);
			}

			try {

				return new Annotation(name, entries);

			} catch (IllegalArgumentException e) {
				throw new ParseException(e.getLocalizedMessage());
			}

		} else {
			String msg = MessageUtils.format(MSG_INVALID_ANNOTATION_SYNTAX);
			throw new ParseException(msg);
		}

	}

	public static List<Entry> parseAsDesc(String s) {

		List<Entry> entries = null;

		if (!isKeyPairSyntax.matcher(s).matches()) {
			entries = new LinkedList<Entry>();
			entries.add(new Entry("desc", s));
		}

		return entries;
	}

}
