package org.quattor.pan.output;

import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_CREATE_OUTPUT_DIRECTORY;
import static org.quattor.pan.utils.MessageUtils.MSG_DUPLICATE_FORMATTER_KEY;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SystemException;
import org.quattor.pan.utils.MessageUtils;

public class FormatterUtils {

	private final static HashMap<String, Formatter> formatters = new HashMap<String, Formatter>();

	// Setup the map between a formatter name and the formatter instance. Since
	// the formatters are singletons, we can give the same instance to everyone
	// that asks.
	static {

		// If more formats are added, the instances should be added to this
		// array.
		Formatter[] instances = new Formatter[] { DotFormatter.getInstance(),
				PanFormatter.getInstance(), TxtFormatter.getInstance(),
				XmlDBFormatter.getInstance(), JsonFormatter.getInstance() };

		// Insert the values, letting the instances choose their key values.
		// Ensure that the values are in lowercase.
		for (Formatter f : instances) {
			String key = f.getFormatKey().toLowerCase();
			Formatter existingValue = formatters.put(key, f);
			if (existingValue != null) {
				throw CompilerError.create(MSG_DUPLICATE_FORMATTER_KEY);
			}
		}
	}

	private final static Formatter defaultFormatter = PanFormatter
			.getInstance();

	/**
	 * This method maps a formatter name to a <code>Formatter</code> instance.
	 * If the formatter name is unknown, then null will be returned. The name
	 * comparison ignores the case of the given name.
	 * 
	 * @param name
	 *            name of the formatter
	 * 
	 * @return <code>Formatter</code> instance corresponding to the name or null
	 *         if the formatter name is unknown
	 */
	public static Formatter getFormatterInstance(String name) {
		String key = (name != null) ? name.toLowerCase() : "";
		return formatters.get(key);
	}

	/**
	 * This method returns the default formatter to use if the user does not
	 * specify one explicitly.
	 * 
	 * @return default Formatter for output files
	 */
	public static Formatter getDefaultFormatterInstance() {
		return defaultFormatter;
	}

	/**
	 * Utility to provide the output URI for a pan object template, given the
	 * output directory and the extension.
	 * 
	 * @param outputDirectory
	 *            directory to use for output files
	 * @param objectName
	 *            full namespaced pan object name
	 * @param fileExtension
	 *            file extension without a leading period (".")
	 * 
	 * @throws URISyntaxException
	 * 
	 */
	public static File getOutputFile(File outputDirectory, String objectName,
			String fileExtension) throws URISyntaxException {

		URI odir = outputDirectory.toURI();
		URI oname = new URI(objectName + "." + fileExtension);
		URI resolvedAbsoluteURI = odir.resolve(oname);
		String resolvedAbsolutePath = resolvedAbsoluteURI
				.getSchemeSpecificPart();
		return new File(resolvedAbsolutePath);
	}

	/**
	 * Creates parent directories of the given file. The file must be absolute.
	 * 
	 * @param file
	 * 
	 * @throws SystemException if directory or directories cannot be created
	 */
	public static void createParentDirectories(File file) {
		File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new SystemException(MessageUtils.format(
					MSG_CANNOT_CREATE_OUTPUT_DIRECTORY,
					parent.getAbsolutePath()), parent);
		}

	}

}
