package org.quattor.pan.repository;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public class ParameterListTest {

	@Test
	public void newParameterListIsEmpty() {
		ParameterList list = new ParameterList();
		assertListIsEmpty(list);
	}

	private void assertListIsEmpty(ParameterList list) {
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	@Test
	public void appendIncreasesSize() {
		Parameter parameter = new Parameter("OK", "OK");
		ParameterList list = new ParameterList();

		int oldSize = list.size();
		list.append(parameter);
		int newSize = list.size();

		assertEquals(1, newSize - oldSize);
	}

	@Test
	public void iteratorGivesCorrectValues() {

		int[] values = { 0, 1, 2, 3, 4 };

		ParameterList list = new ParameterList();
		for (int value : values) {
			Parameter parameter = new Parameter("OK", Integer.toString(value));
			list.append(parameter);
		}

		int[] testValues = new int[list.size()];
		int index = 0;
		for (Parameter parameter : list) {
			int value = Integer.parseInt(parameter.getValue());
			testValues[index++] = value;
		}

		assertArrayEquals(values, testValues);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void iteratorDoesNotSupportRemove() {
		ParameterList list = new ParameterList();
		Parameter parameter = new Parameter("OK", "OK");
		list.append(parameter);

		Iterator<Parameter> iterator = list.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyStringAsNameFails() {
		ParameterList list = new ParameterList();
		list.append("", "OK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullAsNameFails() {
		ParameterList list = new ParameterList();
		list.append(null, "OK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullAsValueFails() {
		ParameterList list = new ParameterList();
		list.append("OK", null);
	}

}
