/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/Format.java $
 $Id: Format.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_ONE_ARG_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_RELATIVE_FILE_REQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Searches for a file on the load path and reads the contents of the file into
 * a String if found. An error occurs if the file is not found.
 * 
 * @author loomis
 * 
 */
final public class ReadFile extends BuiltInFunction {
	
	// TODO: Should this be named "file_contents"?

	// TODO: This class is incomplete. Need to think about generalizing source
	// files.

	private static final long serialVersionUID = 1053594650245882162L;

	private ReadFile(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super(sourceRange, operations);

		// There must be exactly one argument.
		if (operations.length != 1) {
			throw SyntaxException.create(sourceRange, MSG_ONE_ARG_REQ,
					"read_file");
		}

		// If there is already a fixed argument, then check that it is valid.
		if (operations[0] instanceof Element) {
			File f = processArgument((Element) operations[0]);
			if (f == null) {
				throw SyntaxException.create(sourceRange,
						MSG_RELATIVE_FILE_REQ, "read_file");
			}
		}
	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {
		return new ReadFile(sourceRange, operations);
	}

	/**
	 * Function to validate the argument of the function and return a File
	 * object with the relative file name. If the argument is not valid, then
	 * null will be returned. It is the caller's responsibility to throw an
	 * appropriate exception.
	 * 
	 * @param element
	 *            argument to validate and process
	 * 
	 * @return File with relative file name or null if the argument is invalid
	 */
	private static File processArgument(Element element) {

		try {

			String s = ((StringProperty) element).getValue();

			// Replace all of the slashes by the platform's file separator.
			s = s.replaceAll("/", System.getProperty("file.separator"));

			// Create a File object from this and verify that it is a relative
			// path.
			File f = new File(s);
			if (f.isAbsolute()) {
				return null;
			}

			// Everything's OK, so return the created file.
			return f;

		} catch (ClassCastException e) {

			// The argument was not a StringProperty so return null to indicate
			// that the argument is not valid.
			return null;
		}

	}

	/**
	 * Return the contents of the given absolute file as a String.
	 * 
	 * @param absolutePath
	 *            absolute path for the given file
	 * 
	 * @return StringProperty containing the contents of the file
	 * 
	 * @throws IOException
	 */
	// TODO: This should be private.
	public static StringProperty readFileAsString(File absolutePath)
			throws IOException {
		FileInputStream stream = new FileInputStream(absolutePath);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc
					.size());
			// TODO: Determine if UTF-8 should be used instead of default.
			String contents = Charset.defaultCharset().decode(bb).toString();
			return StringProperty.getInstance(contents);
		} finally {
			stream.close();
		}
	}

	@Override
	public Element execute(Context context) {

		// Calculate arguments.
		Element[] args = calculateArgs(context);
		assert (args.length == 1);

		// Get the relative file name to find.
		File f = processArgument(args[0]);
		if (f == null) {
			throw EvaluationException.create(sourceRange,
					MSG_RELATIVE_FILE_REQ, "read_file");
		}

		// Use the lookup algorithm to convert this to an absolute file name for
		// an existing file.

		return StringProperty.getInstance("OK");
	}
}
