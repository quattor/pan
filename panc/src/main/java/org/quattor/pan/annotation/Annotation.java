package org.quattor.pan.annotation;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ANNOTATION_NAME_OR_KEY;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ANNOTATION_NULL_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.quattor.pan.utils.MessageUtils;

public class Annotation implements Iterable<Annotation.Entry> {

	private final static Pattern xmlPrefix = Pattern.compile("^[Xx][Mm][Ll].*",
			Pattern.DOTALL);

	private final static Pattern validKeyChars = Pattern
			.compile("^[A-Za-z_]+[A-Za-z_\\.\\-]*$");

	private final String name;

	private final List<Entry> entries;

	public Annotation(String name, List<Entry> entries) {

		this.name = (name != null) ? name : "";
		if (!validName(this.name)) {
			String msg = MessageUtils.format(
					MSG_INVALID_ANNOTATION_NAME_OR_KEY, this.name);
			throw new IllegalArgumentException(msg);
		}

		ArrayList<Entry> copy = null;
		if (entries != null) {
			copy = new ArrayList<Entry>(entries.size());
			copy.addAll(entries);
		} else {
			copy = new ArrayList<Entry>(0);
		}
		copy.trimToSize();

		this.entries = Collections.unmodifiableList(copy);
	}

	public String getName() {
		return name;
	}

	public boolean isAnonymous() {
		return "".equals(name);
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	public static boolean validName(String name) {
		return ("".equals(name) || validKey(name));
	}

	public static boolean validKey(String key) {

		if (key == null) {
			return false;
		}

		if (xmlPrefix.matcher(key).matches()) {
			return false;
		}

		if (!validKeyChars.matcher(key).matches()) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("@" + name + "{\n");
		for (Entry entry : getEntries()) {
			sb.append("  ");
			sb.append(entry.toString());
			sb.append("\n");
		}
		sb.append("}\n");
		return sb.toString();
	}

	public static class Entry {

		private final String key;

		private final String value;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;

			if (!validKey(key)) {
				String msg = MessageUtils.format(
						MSG_INVALID_ANNOTATION_NAME_OR_KEY, key);
				throw new IllegalArgumentException(msg);
			}

			if (value == null) {
				String msg = MessageUtils
						.format(MSG_INVALID_ANNOTATION_NULL_VALUE);
				throw new IllegalArgumentException(msg);
			}
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("%s = %s", key, value);
		}

	}

}
