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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatter.java $
 $Id: PanFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.tasks.FinalResult;

public abstract class AbstractFormatter implements Formatter {

	private final String suffix;

	private final String key;

	protected AbstractFormatter(String suffix, String key) {
		this.suffix = suffix;
		this.key = key;
	}

	public URI getResultURI(String objectName) {
		try {
			return new URI(objectName + "." + suffix);
		} catch (URISyntaxException e) {
			throw new CompilerError(
					"invalid object template name encountered: " + objectName);
		}
	}

	public String getFormatKey() {
		return key;
	}

	public void write(FinalResult result, URI outputURI) throws Exception {

		PrintWriter pw = null;
		try {
			pw = getPrintWriter(new File(outputURI));
			write(result, pw);
		} finally {
			closeReliably(pw);
		}
	}

	protected PrintWriter getPrintWriter(File file) throws Exception {
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
	}

	protected abstract void write(FinalResult result, PrintWriter ps)
			throws Exception;

	private static void closeReliably(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception consumed) {
			}
		}

	}

}
