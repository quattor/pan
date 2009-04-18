package org.quattor.pan.template;

import java.io.File;
import java.util.Comparator;

public class TemplateSourceComparator implements Comparator<Template> {

	private static TemplateSourceComparator instance = new TemplateSourceComparator();

	private TemplateSourceComparator() {
	}

	public static TemplateSourceComparator getInstance() {
		return instance;
	}

	public int compare(Template o1, Template o2) {
		File f1 = o1.source;
		File f2 = o2.source;
		if (f1 == null) {
			if (f2 == null) {
				// Both are null, so should be equal.
				return 0;
			} else {
				// The null value should be less than any non-null value.
				return -1;
			}
		} else {
			if (f2 == null) {
				// A non-null value should be greater than any null value.
				return 1;
			} else {
				// Both a non-null; use the file comparator for the answer.
				return f1.compareTo(f2);
			}
		}
	}

	public boolean equals(Object obj) {
		return (obj instanceof TemplateSourceComparator);
	}

}
