package org.quattor.pan.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class ParameterList implements Iterable<Parameter> {

	List<Parameter> parameters;

	public ParameterList() {
		parameters = new ArrayList<Parameter>();
	}

	public int size() {
		return parameters.size();
	}

	public boolean isEmpty() {
		return (size() == 0);
	}

	public void append(Parameter parameter) {
		parameters.add(parameter);
	}

	public void append(String name, String value) {
		Parameter parameter = new Parameter(name, value);
		append(parameter);
	}

	public Iterator<Parameter> iterator() {
		return Collections.unmodifiableList(parameters).iterator();
	}

}
