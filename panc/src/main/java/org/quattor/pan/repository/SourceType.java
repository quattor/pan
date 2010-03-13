package org.quattor.pan.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source files are either a pan language template or a text file.
 * 
 * @author loomis
 * 
 */
public enum SourceType {
	TPL(false, ".tpl"), PAN(false, ".pan"), PANX(false, ".panx"), TEXT(false,
			""), ABSENT_SOURCE(true, ""), ABSENT_TEXT(true, "");

	private boolean absent;
	private String extension;

	private final static List<String> extensions;

	static {

		ArrayList<String> values = new ArrayList<String>();

		for (SourceType type : SourceType.values()) {
			if (type.isSource()) {
				values.add(type.getExtension());
			}
		}

		values.trimToSize();

		extensions = Collections.unmodifiableList(values);
	}

	private SourceType(boolean absent, String extension) {
		this.absent = absent;
		this.extension = extension;
	}

	public boolean isSource() {
		return (!"".equals(extension));
	}

	public boolean isAbsent() {
		return absent;
	}

	public String getExtension() {
		return extension;
	}

	public static List<String> getExtensions() {
		return extensions;
	}

	public static boolean hasSourceFileExtension(String filename) {
		for (String extension : extensions) {
			if (filename.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}