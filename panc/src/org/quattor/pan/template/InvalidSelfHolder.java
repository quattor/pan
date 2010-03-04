package org.quattor.pan.template;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.utils.GlobalVariable;
import org.quattor.pan.utils.Path;

/**
 * A class that will throw an exception if SELF is accessed in any way. This is
 * the case for include statements where SELF is undefined.
 * 
 * @author loomis
 * 
 */
public class InvalidSelfHolder extends SelfHolder {

	public InvalidSelfHolder() {
		path = null;
		variable = null;
		element = null;
		unmodifiable = true;
	}

	@Override
	public InvalidSelfHolder clone() {
		return (InvalidSelfHolder) super.clone();
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
	public Path getPath() {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public void setPath(Path path) {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public GlobalVariable getVariable() {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public void setVariable(GlobalVariable variable) {
		throw new EvaluationException("SELF is undefined in this context");
	}

	@Override
	public boolean isUnmodifiable() {
		return true;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		throw new EvaluationException("SELF is undefined in this context");
	}

}