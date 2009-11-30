package org.quattor.pan.repository;

import static org.quattor.pan.utils.MessageUtils.MSG_ABSOLUTE_PATH_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_TPL_NAME;
import static org.quattor.pan.utils.MessageUtils.MSG_MISNAMED_TPL;
import static org.quattor.pan.utils.MessageUtils.MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL;

import java.io.File;
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
		PAN, TXT, MISSING
	};

	public final String name;

	public final Type type;

	public final File location;

	public final File path;

	public SourceFile(String name, Type type, File path)
			throws IllegalArgumentException {
		this.name = name;
		this.type = type;

		// Check that name and type are not null.
		if (name == null || type == null) {
			throw CompilerError.create(MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL);
		}

		// The path will be ignored if the SourceFile is MISSING.
		if (!type.equals(Type.MISSING)) {
			this.path = path;
		} else {
			this.path = null;
		}

		// The path can be null, but if it isn't it must be an absolute path.
		// The current working directory may have changed so we can not reliably
		// create an absolute path from a relative one.
		if (path != null && !path.isAbsolute()) {
			throw CompilerError.create(MSG_ABSOLUTE_PATH_REQ);
		}

		// The name must be a valid template name, even if it is just a normal
		// text file to be included through a file_contents() call.
		if (!Template.isValidTemplateName(name)) {
			throw new IllegalArgumentException(MessageUtils.format(
					MSG_INVALID_TPL_NAME, name));
		}

		// Ensure that the name, type, and source are consistent. An exception
		// will be thrown if the values are not consistent.
		location = weakTemplateNameVerification(name, type, path);

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

		// TODO: Update dependency file format to allow read dependency mgt.

		// For backward compatibility, the TXT and MISSING files are not
		// included in the dependency file. Unfortunately adding this
		// information requires a non-backward compatible change to the file
		// format.

		switch (type) {
		case PAN:
			String s = (location != null) ? location.toString() : "";
			if (!s.endsWith("/")) {
				return String.format("\"%s\" \"%s/\"%n", name, s);
			} else {
				return String.format("\"%s\" \"%s\"%n", name, s);
			}
		case TXT:
			// s = (location != null) ? location.toString() : "";
			// if (!s.endsWith("/")) {
			// return String.format("\"%s\" \"%s/\"%n", name, s);
			// } else {
			// return String.format("\"%s\" \"%s\"%n", name, s);
			// }
		case MISSING:
			// return String.format("\"%s\" \"\"%n", name);
		default:
			return "";
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
			if (type == Type.PAN) {
				sb.append(".tpl");
			}
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
