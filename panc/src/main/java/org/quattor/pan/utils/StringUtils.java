package org.quattor.pan.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StringUtils {

	private StringUtils() {

	}

	public static String readCompletely(InputStream inputStream)
			throws IOException {
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		return readCompletely(reader);
	}

	public static String readCompletely(Reader reader) throws IOException {

		try {

			StringBuilder sb = new StringBuilder();

			char[] cbuf = new char[0x2048];
			for (int nchar = reader.read(cbuf); nchar >= 0; nchar = reader
					.read(cbuf)) {
				sb.append(cbuf, 0, nchar);
			}

			return sb.toString();

		} finally {
			reader.close();
		}

	}
}
