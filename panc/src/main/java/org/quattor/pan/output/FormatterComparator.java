package org.quattor.pan.output;

import java.util.Comparator;

public class FormatterComparator implements Comparator<Formatter> {

	private final static FormatterComparator instance = new FormatterComparator();

	private FormatterComparator() {

	}

	public static FormatterComparator getInstance() {
		return instance;
	}

	public int compare(Formatter f1, Formatter f2) {
		if (f1.getClass().equals(f2.getClass())) {
			return 0;
		} else if (f1.hashCode() > f2.hashCode()) {
			return 1;
		} else {
			return -1;
		}

	}

	public boolean equals(Object obj) {
		return (obj instanceof FormatterComparator);
	}

}
