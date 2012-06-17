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

import java.io.PrintWriter;
import java.net.URI;

import org.quattor.pan.dml.data.Element;

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
