package org.quattor.pan.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void stringBuilderAcceptsZeroLength() {
		StringBuilder sb = new StringBuilder();
		char[] cbuf = new char[10];
		sb.append(cbuf, 0, 0);
		assertTrue(sb.length() == 0);
	}

	@Test
	public void resultAndSourceMatchReader() throws IOException {
		String source = "Mary had a little lamb.\n";
		Reader reader = new StringReader(source);
		String result = StringUtils.readCompletely(reader);
		assertEquals(source, result);
	}

	@Test
	public void resultAndSourceMatchStream() throws IOException {
		String source = "Mary had a little lamb.\n";
		byte[] bytes = source.getBytes("UTF-8");
		InputStream inputStream = new ByteArrayInputStream(bytes);
		String result = StringUtils.readCompletely(inputStream);
		assertEquals(source, result);
	}
	
}
