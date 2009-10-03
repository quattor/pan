package org.quattor.pan.parser;

import java.util.Map;
import java.util.Map.Entry;

import org.quattor.pan.parser.annotation.AnnotationProcessor;

public class AnnotationToken extends Token {

	private Object map;

	private static boolean dumpAnnotation = false;

	public AnnotationToken() {
		super();
	}

	public AnnotationToken(int kind) {
		this(kind, null);
	}

	public AnnotationToken(int kind, String image) {
		super(kind, image);

		try {
			map = AnnotationProcessor.process(image);
		} catch (org.quattor.pan.parser.annotation.ParseException e) {
			map = e;
		}
	}

	@Override
	public Object getValue() {
		return map;
	}

	public static void setDumpAnnotation(boolean dump) {
		dumpAnnotation = dump;
	}

	public void dump() {
		if (dumpAnnotation) {
			System.out.println("@(");
			if (map instanceof Map<?, ?>) {
				Map<?, ?> mymap = (Map<?, ?>) map;
				for (Entry<?, ?> entry : mymap.entrySet()) {
					System.out.println(entry.getKey() + " = '"
							+ entry.getValue() + "'");
				}
			}
			System.out.println(")");
		}
	}
}
