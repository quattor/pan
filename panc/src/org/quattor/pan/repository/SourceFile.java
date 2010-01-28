package org.quattor.pan.repository;

import static org.quattor.pan.utils.MessageUtils.MSG_ABSENT_FILE_MUST_HAVE_NULL_PATH;
import static org.quattor.pan.utils.MessageUtils.MSG_ABSOLUTE_PATH_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_TPL_NAME;
import static org.quattor.pan.utils.MessageUtils.MSG_MISNAMED_TPL;
import static org.quattor.pan.utils.MessageUtils.MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.jcip.annotations.Immutable;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.template.Template;
import org.quattor.pan.utils.MessageUtils;

@Immutable
public class SourceFile implements Comparable<SourceFile>, Serializable {

	private static final long serialVersionUID = 6307480037651666260L;

	/**
	 * Source files are either a pan language template or a text file.
	 * 
	 * @author loomis
	 * 
	 */
	public enum Type {
		TPL(false, ".tpl"), PAN(false, ".pan"), PANX(false, ".panx"), TEXT(
				false, ""), ABSENT_SOURCE(true, ""), ABSENT_TEXT(true, "");

		private boolean absent;
		private String extension;

		private Type(boolean absent, String extension) {
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

		public static boolean hasSourceFileExtension(String filename) {
			for (Type type : Type.values()) {
				if (type.isSource()) {
					if (filename.endsWith(type.getExtension())) {
						return true;
					}
				}
			}
			return false;
		}
	};

	private final String name;

	private final Type type;

	private final File location;

	private final File path;

	public SourceFile(String name, boolean isSource, File path)
			throws IllegalArgumentException {

		this.name = name;
		this.path = path;

		if (isSource) {

			if (path == null) {
				this.type = Type.ABSENT_SOURCE;
			} else {
				String extension = getFileExtension(path);
				if (".tpl".equals(extension)) {
					this.type = Type.TPL;
				} else if (".pan".equals(extension)) {
					this.type = Type.PAN;
				} else if (".panx".equals(extension)) {
					this.type = Type.PANX;
				} else {
					throw new IllegalArgumentException(MessageUtils.format(
							MSG_INVALID_TPL_NAME, name));
				}
			}

		} else {
			this.type = (path != null) ? Type.TEXT : Type.ABSENT_TEXT;
		}

		validateFields(name, type, path);

		// Ensure that the name, type, and source are consistent. An exception
		// will be thrown if the values are not consistent.
		location = weakTemplateNameVerification(name, type, path);

	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public File getLocation() {
		return location;
	}

	public File getPath() {
		return path;
	}

	public boolean isAbsent() {
		return type.isAbsent();
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(path);
	}

	public Reader getReader() throws IOException {
		return new InputStreamReader(getInputStream(), "UTF-8");
	}

	public int hashCode() {
		int hc = name.hashCode() ^ type.hashCode();
		if (path != null) {
			hc ^= path.hashCode();
		}
		return hc;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof SourceFile) {
			SourceFile s = (SourceFile) o;
			return this.compareTo(s) == 0;
		} else {
			return false;
		}
	}

	public int compareTo(SourceFile o) {

		if (o == null) {
			throw new NullPointerException();
		}

		int value = name.compareTo(o.name);
		if (value != 0) {
			return value;
		}

		value = type.compareTo(o.type);
		if (value != 0) {
			return value;
		}

		if (path != null && o.path != null) {
			value = path.compareTo(o.path);
			return value;
		} else if (path == null && o.path == null) {
			return 0;
		} else if (path == null) {
			return -1;
		} else if (o.path == null) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		String uri = (location != null) ? location.toURI().toASCIIString() : "";
		return String.format("%s %s %s", name, type, uri);
	}

	private String getFileExtension(File path) {

		String extension = null;
		if (path != null) {
			String s = path.toString();
			int index = s.lastIndexOf('.');
			if (index >= 0) {
				extension = s.substring(index);
			}
		}
		return extension;
	}

	private void validateFields(String name, Type type, File path)
			throws CompilerError {

		// Check that name and type are not null.
		if (name == null || type == null) {
			throw CompilerError.create(MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL);
		}

		// If the file is absent, then the path must be empty.
		if (type.isAbsent() && path != null) {
			throw CompilerError.create(MSG_ABSOLUTE_PATH_REQ);
		}

		// The path can be null, but if it isn't it must be an absolute path.
		// The current working directory may have changed so we can not reliably
		// create an absolute path from a relative one.
		if (path != null && !path.isAbsolute()) {
			throw CompilerError.create(MSG_ABSENT_FILE_MUST_HAVE_NULL_PATH);
		}

		// The name must be a valid template name, even if it is just a normal
		// text file to be included through a file_contents() call.
		if (!Template.isValidTemplateName(name)) {
			throw new IllegalArgumentException(MessageUtils.format(
					MSG_INVALID_TPL_NAME, name));
		}
	}

	/**
	 * Perform some weak verification checks between the source file name, type,
	 * and the source location. It will return the presumed loadpath (location)
	 * of the source file.
	 * 
	 * @param name
	 * @param type
	 * @param source
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static File weakTemplateNameVerification(String name, Type type,
			File source) throws IllegalArgumentException {

		File location = null;

		if (source != null) {

			// From the name and type determine the correct ending of the source
			// File.
			StringBuilder sb = new StringBuilder("/");
			sb.append(name);
			sb.append(type.getExtension());
			String ending = sb.toString();

			// Ensure that the source File really ends with the required string.
			// The change to a URI handles any differences with file separators
			// on different platforms.
			String uri = source.toURI().toString();
			if (!uri.endsWith(ending)) {
				throw new IllegalArgumentException(MessageUtils.format(
						MSG_MISNAMED_TPL, name));
			}

			// Strip off the ending to get the load path for this file.
			try {
				location = new File(new URI(uri.substring(0, uri
						.lastIndexOf(ending))));
			} catch (URISyntaxException consumed) {

			}
		}
		return location;
	}

}
