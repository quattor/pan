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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/ObjectContextTest.java $
 $Id: ObjectContextTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.ttemplate;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Resource;

public class IteratorMapTest {

	@Test
	public void checkNullForUndefinedIterator() {
		IteratorMap map = new IteratorMap();
		Resource dummyResource = new ListResource();
		assertTrue(map.get(dummyResource) == null);
	}

	@Test
	public void checkSimpleAssociation() {

		// Setup an argument list with two string values.
		Resource resource = new ListResource();
		Resource.Iterator iterator = resource.iterator();

		// Create the map.
		IteratorMap map = new IteratorMap();

		// Set the value and read it back.
		map.put(resource, iterator);
		assertTrue(map.get(resource) == iterator);

		// Remove the value and be sure it's gone.
		map.put(resource, null);
		assertTrue(map.get(resource) == null);
	}

}
