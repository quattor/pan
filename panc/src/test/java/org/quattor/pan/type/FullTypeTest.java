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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/FullTypeTest.java $
 $Id: FullTypeTest.java 3599 2008-08-17 13:57:57Z loomis $
 */

package org.quattor.pan.type;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.DoubleProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.TermFactory;

public class FullTypeTest {

	private void validateBuiltInType(String name, Element element)
			throws SyntaxException, ValidationException {

		Context context = new BuildContext();

		BaseType base = new AliasType(null, null, name, null);
		FullType type = new FullType(base);
		type.validate(context, element);
	}

	private void validateInvalidBuiltInType(String name, Element element)
			throws SyntaxException {

		Context context = new BuildContext();

		try {
			BaseType base = new AliasType(null, null, name, null);
			FullType type = new FullType(base);
			type.validate(context, element);
			fail("checking built in type of " + name
					+ " did not throw an error with element of type "
					+ element.getTypeAsString());
		} catch (ValidationException ve) {
			// OK.
		}
	}

	private void validateBuiltInTypeWithRange(String name, Element element)
			throws SyntaxException, ValidationException {

		Context context = new BuildContext();
		Range range = new Range(1, 5);

		BaseType base = new AliasType(null, null, name, range);
		FullType type = new FullType(base);
		type.validate(context, element);
	}

	private void validateInvalidBuiltInTypeWithRange(String name,
			Element element) throws SyntaxException {

		Context context = new BuildContext();
		Range range = new Range(1, 5);

		try {
			BaseType base = new AliasType(null, null, name, range);
			FullType type = new FullType(base);
			type.validate(context, element);
			fail("checking built in type of " + name + " with range " + range
					+ " did not throw an exception");
		} catch (ValidationException ve) {
			// OK.
		}
	}

	@Test
	public void testValidBuiltInTypes() throws SyntaxException,
			ValidationException {
		validateBuiltInType("boolean", BooleanProperty.getInstance(true));
		validateBuiltInType("double", DoubleProperty.getInstance(1.0));
		validateBuiltInType("element", StringProperty.getInstance("dummy"));
		validateBuiltInType("nlist", new HashResource());
		validateBuiltInType("dict", new HashResource());
		validateBuiltInType("list", new ListResource());
		validateBuiltInType("long", LongProperty.getInstance(10));
		validateBuiltInType("number", LongProperty.getInstance(10));
		validateBuiltInType("property", StringProperty.getInstance("dummy"));
		validateBuiltInType("resource", new HashResource());
		validateBuiltInType("string", StringProperty.getInstance("dummy"));
	}

	@Test
	public void testInvalidBuiltInTypes() throws SyntaxException {
		validateInvalidBuiltInType("boolean", StringProperty
				.getInstance("dummy"));
		validateInvalidBuiltInType("double", BooleanProperty.getInstance(true));
		validateInvalidBuiltInType("nlist", new ListResource());
		validateInvalidBuiltInType("list", new HashResource());
		validateInvalidBuiltInType("long", DoubleProperty.getInstance(1.0));
		validateInvalidBuiltInType("number", StringProperty
				.getInstance("dummy"));
		validateInvalidBuiltInType("property", new HashResource());
		validateInvalidBuiltInType("resource", StringProperty
				.getInstance("dummy"));
		validateInvalidBuiltInType("string", DoubleProperty.getInstance(1.0));
	}

	@Test
	public void testValidBuiltInTypesWithRange() throws SyntaxException,
			ValidationException, InvalidTermException {

		HashResource dict = new HashResource();
		dict.put(TermFactory.create("a"), DoubleProperty.getInstance(1.0));

		ListResource list = new ListResource();
		list.put(TermFactory.create(0), DoubleProperty.getInstance(1.0));

		validateBuiltInTypeWithRange("double", DoubleProperty.getInstance(1.0));
		validateBuiltInTypeWithRange("nlist", dict);
		validateBuiltInTypeWithRange("list", list);
		validateBuiltInTypeWithRange("long", LongProperty.getInstance(3));
		validateBuiltInTypeWithRange("string", StringProperty
				.getInstance("dummy"));
	}

	@Test
	public void testInvalidBuiltInTypesWithRange() throws SyntaxException {
		validateInvalidBuiltInTypeWithRange("double", DoubleProperty
				.getInstance(0.0));
		validateInvalidBuiltInTypeWithRange("nlist", new HashResource());
		validateInvalidBuiltInTypeWithRange("list", new ListResource());
		validateInvalidBuiltInTypeWithRange("long", LongProperty.getInstance(0));
		validateInvalidBuiltInTypeWithRange("string", StringProperty
				.getInstance("too long"));
	}

}
