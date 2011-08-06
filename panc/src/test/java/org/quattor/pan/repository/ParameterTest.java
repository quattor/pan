package org.quattor.pan.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ParameterTest {

	@Test(expected = IllegalArgumentException.class)
	public void emptyStringKeyFails() {
		new Parameter("", "OK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullKeyFails() {
		new Parameter(null, "OK");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullValueFails() {
		new Parameter("OK", null);
	}

	@Test
	public void validKeyAndValue() {
		final String KEY = "myKey";
		final String VALUE = "myValue";

		Parameter pair = new Parameter(KEY, VALUE);

		assertNotNull(pair);
		assertEquals(KEY, pair.getKey());
		assertEquals(VALUE, pair.getValue());
	}
}
