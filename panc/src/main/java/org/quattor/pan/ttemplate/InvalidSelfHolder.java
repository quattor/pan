package org.quattor.pan.ttemplate;

import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_SELF_REF_IN_INCLUDE;

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
		throw new EvaluationException(MSG_INVALID_SELF_REF_IN_INCLUDE);
	}

	@Override
	public void setElement(Element element) {
		throw new EvaluationException(MSG_INVALID_SELF_REF_IN_INCLUDE);
	}

	@Override
	public boolean isUnmodifiable() {
		return true;
	}

}