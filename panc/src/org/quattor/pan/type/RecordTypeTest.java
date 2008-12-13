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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/RecordTypeTest.java $
 $Id: RecordTypeTest.java 3602 2008-08-18 14:34:24Z loomis $
 */

package org.quattor.pan.type;

import java.util.LinkedList;
import java.util.TreeMap;

import org.junit.Test;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;
import org.quattor.pan.utils.TermFactory;

public class RecordTypeTest {

	@Test
	public void testRecordStructure() throws SyntaxException,
			ValidationException, InvalidTermException {

		TreeMap<Term, FullType> reqFields = new TreeMap<Term, FullType>();
		TreeMap<Term, FullType> optFields = new TreeMap<Term, FullType>();

		reqFields.put(TermFactory.create("alpha"), new FullType(new AliasType(
				null, null, "string", null)));
		reqFields.put(TermFactory.create("beta"), new FullType(new AliasType(
				null, null, "string", null)));

		optFields.put(TermFactory.create("one"), new FullType(new AliasType(
				null, null, "string", null)));
		optFields.put(TermFactory.create("two"), new FullType(new AliasType(
				null, null, "string", null)));

		Context context = new BuildContext();

		// Create the record type from the information above.
		BaseType base = new RecordType(null, null, false, null,
				new LinkedList<String>(), reqFields, optFields);
		FullType type = new FullType(base);

		// Create a valid record.
		HashResource record = new HashResource();
		record.put(TermFactory.create("alpha"), StringProperty
				.getInstance("alpha"));
		record.put(TermFactory.create("beta"), StringProperty
				.getInstance("beta"));
		record
				.put(TermFactory.create("one"), StringProperty
						.getInstance("one"));
		record
				.put(TermFactory.create("two"), StringProperty
						.getInstance("two"));

		// Check that it's OK.
		type.validate(context, record);

		// Remove a required field.
		record.put(TermFactory.create("alpha"), null);

		// Should now throw an exception.
		try {
			type.validate(context, record);
		} catch (ValidationException ve) {
			// OK.
		}

		// Put that field back and add an unexpected child element.
		record.put(TermFactory.create("alpha"), StringProperty
				.getInstance("alpha"));
		record.put(TermFactory.create("three"), StringProperty
				.getInstance("three"));

		// Should now throw an exception.
		try {
			type.validate(context, record);
		} catch (ValidationException ve) {
			// OK.
		}

		// Remove the unexpected child.
		record.put(TermFactory.create("three"), null);

		// Add a range requirement, make it extensible.
		base = new RecordType(null, null, true, new Range(0, 5),
				new LinkedList<String>(), reqFields, optFields);
		type = new FullType(base);

		// Should be OK. (Only four children.)
		type.validate(context, record);

		// Add back the undeclared child. Should be OK now.
		record.put(TermFactory.create("three"), StringProperty
				.getInstance("three"));

		// Should be OK; five children.
		type.validate(context, record);

		// Add another child.
		record.put(TermFactory.create("four"), StringProperty
				.getInstance("four"));

		// Should fail now that there are six children.
		try {
			type.validate(context, record);
		} catch (ValidationException ve) {
			// OK.
		}

	}

	@Test
	public void testRecordIncludes() throws SyntaxException,
			ValidationException, InvalidTermException {

		TreeMap<Term, FullType> reqFields = new TreeMap<Term, FullType>();
		TreeMap<Term, FullType> optFields = new TreeMap<Term, FullType>();
		LinkedList<String> includes = new LinkedList<String>();

		reqFields.put(TermFactory.create("alpha"), new FullType(new AliasType(
				null, null, "string", null)));

		Context context = new BuildContext();

		// Create the included record type.
		BaseType base = new RecordType(null, null, true, null, includes,
				reqFields, optFields);
		FullType type = new FullType(base);

		// Add it to the context.
		context.setFullType("included_type", type, null, null);

		// Clear the required fields. Add new ones.
		reqFields.clear();

		includes.add("included_type");

		reqFields.put(TermFactory.create("beta"), new FullType(new AliasType(
				null, null, "string", null)));

		optFields.put(TermFactory.create("one"), new FullType(new AliasType(
				null, null, "string", null)));
		optFields.put(TermFactory.create("two"), new FullType(new AliasType(
				null, null, "string", null)));

		// Create the record type from the information above.
		base = new RecordType(null, null, true, null, includes, reqFields,
				optFields);
		type = new FullType(base);

		// Create a valid record.
		HashResource record = new HashResource();
		record.put(TermFactory.create("alpha"), StringProperty
				.getInstance("alpha"));
		record.put(TermFactory.create("beta"), StringProperty
				.getInstance("beta"));
		record
				.put(TermFactory.create("one"), StringProperty
						.getInstance("one"));
		record
				.put(TermFactory.create("two"), StringProperty
						.getInstance("two"));

		// Check that it's OK.
		type.validate(context, record);

		// Remove a required field for included type.
		record.put(TermFactory.create("alpha"), null);

		// Should now throw an exception.
		try {
			type.validate(context, record);
		} catch (ValidationException ve) {
			// OK.
		}

	}

}
