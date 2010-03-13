package org.quattor.pan.repository;

import net.jcip.annotations.Immutable;

@Immutable
public class Parameter {

	final private String name;
	final private String value;

	public Parameter(String key, String value) {
		this.name = validateName(key);
		this.value = validateValue(value);
	}

	public String getKey() {
		return name;
	}

	public String getValue() {
		return value;
	}

	private static String validateName(String name)
			throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException("null is not a valid name");
		} else if ("".equals(name)) {
			throw new IllegalArgumentException(
					"empty string is not a valid name");
		}
		return name;
	}

	private static String validateValue(String value)
			throws IllegalArgumentException {
		if (value == null) {
			throw new IllegalArgumentException("null is not a valid value");
		}
		return value;
	}

}
