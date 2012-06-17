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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/TxtFormatter.java $
 $Id: TxtFormatter.java 3515 2008-07-31 13:20:05Z loomis $
 */

package org.quattor.pan.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.tasks.Valid2Result;

public class TxtFormatter extends PanTxtFormatter {

	private static final String suffix = "txt";

	private static final String key = "text";

	/**
	 * Instance
	 */
	private static final TxtFormatter instance = new TxtFormatter();

	/**
	 * Constructor
	 */
	private TxtFormatter() {
	}

	/**
	 * creation of a singleton
	 */
	public synchronized static TxtFormatter getInstance() {
		return instance;
	}

	public String getFileExtension() {
		return suffix;
	}

	public URI getResultURI(String objectName) {
		return FormatterUtils.getResultURI(objectName, suffix);
	}

	public String getFormatKey() {
		return key;
	}

	public void write(String objectName, URI outputDirectory,
			Valid2Result result) throws Exception {

		URI resultURI = getResultURI(objectName);
		URI absoluteURI = outputDirectory.resolve(resultURI);
		File absolutePath = new File(absoluteURI);

		FormatterUtils.createParentDirectories(absolutePath);

		OutputStream os = new FileOutputStream(absolutePath);

		// GZIP OUTPUT
		// absolutePath = new File(absolutePath.toString() + ".gz");
		// os = new GZIPOutputStream(new FileOutputStream(absolutePath));

		PrintWriter ps = new PrintWriter(os);
		write(result.getRoot(), "profile", ps);
		ps.close();

		// Make sure that the file has the timestamp passed into the
		// constructor.
		if (!absolutePath.setLastModified(result.timestamp)) {
			// Probably a warning should be emitted here, but currently
			// there are no facilities for warnings in the pan compiler
			// yet.
		}

	}

	@Override
	public void write(Element root, String rootname, PrintWriter ps) {
		int level = 0;

		readChild(root, ps, level, rootname);
		ps.close();
	}

	@Override
	public void writeBegin(PrintWriter ps, String nbTab, String name, int n,
			String s) {
		ps.printf("%s+-%s\n", nbTab, name);
	}

	@Override
	public void writeEnd(PrintWriter ps, String s1, String s) {
	}

	@Override
	public void writeProperties(PrintWriter ps, String nbTab, String name,
			String type, String value) {
		ps.printf("%s$ %s : (%s) '%s'\n", nbTab, name, type, value);
	}
}
