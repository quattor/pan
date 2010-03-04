package org.quattor.pan.template;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;

/**
 * A class that will throw an exception if SELF is accessed in any way. This is
 * the case for include statements where SELF is undefined.
 * 
 * @author loomis
 * 
 */
public class InvalidSelfHolder extends SelfHolder {

	public InvalidSelfHolder() {
		element = null;
		unmodifiable = true;
	}

	@Override
	public Element getElement() {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public void setElement(Element element) {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public boolean isUnmodifiable() {
		return true;
	}

}