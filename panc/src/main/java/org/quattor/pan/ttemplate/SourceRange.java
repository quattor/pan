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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/SourceRange.java $
 $Id: SourceRange.java 1339 2007-02-16 23:14:24Z loomis $
 */

package org.quattor.pan.ttemplate;

/**
 * Defines a range of characters within a source pan template that is used to
 * provide detailed error messages.
 * 
 * @author loomis
 * 
 */
public class SourceRange {

	private final int beginLine;

	private final int beginColumn;

	private final int endLine;

	private final int endColumn;

	public SourceRange(int beginLine, int beginColumn, int endLine,
			int endColumn) {

		// Check that all values are positive.
		if (beginLine < 1) {
			throw new IllegalArgumentException("beginLine must be positive");
		}
		if (beginColumn < 1) {
			throw new IllegalArgumentException("beginLine must be positive");
		}
		if (endLine < 1) {
			throw new IllegalArgumentException("endLine must be positive");
		}
		if (endColumn < 1) {
			throw new IllegalArgumentException("endLine must be positive");
		}

		// Check that ending point is after (or the same as) the starting point.
		if (endLine < beginLine) {
			throw new IllegalArgumentException(
					"endLine must be the same or larger than beginLine");
		}
		if ((endLine == beginLine) && (endColumn < beginColumn)) {
			throw new IllegalArgumentException(
					"endLine must be the same or larger than beginLine");
		}

		// Copy the data into this instance.
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;

	}

	@Override
	public String toString() {
		return beginLine + "." + beginColumn + "-" + endLine + "." + endColumn;
	}

	public static SourceRange combineSourceRanges(SourceRange... ranges) {

		int bline = 0, bcol = 0, eline = 0, ecol = 0;

		// Find the first non-null SourceRange and copy in the data.
		int firstIndex = -1;
		for (int i = 0; i < ranges.length; i++) {
			SourceRange r = ranges[i];
			if (r != null) {
				bline = r.beginLine;
				bcol = r.beginColumn;
				eline = r.endLine;
				ecol = r.endColumn;
				firstIndex = i + 1;
				break;
			}
		}

		// If no SourceRange was found, then return null.
		if (firstIndex < 0) {
			return null;
		}

		// Ok, got the first one, not combine with the rest.
		for (int i = firstIndex; i < ranges.length; i++) {

			SourceRange r = ranges[i];

			if (r != null) {
				if (r.beginLine < bline) {
					bline = r.beginLine;
					bcol = r.beginColumn;
				} else if ((r.beginLine == bline) && (r.beginColumn < bcol)) {
					bcol = r.beginColumn;
				}

				if (r.endLine < eline) {
					eline = r.endLine;
					ecol = r.endColumn;
				} else if ((r.endLine == eline) && (r.endColumn > ecol)) {
					ecol = r.endColumn;
				}
			}
		}

		return new SourceRange(bline, bcol, eline, ecol);
	}
}
