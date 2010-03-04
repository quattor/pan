package org.quattor.pan.template;

import static org.quattor.pan.utils.MessageUtils.MSG_CLONE_NOT_SUPPORTED;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;

/**
 * Essentially just a structure to hold values related to the processing of the
 * SELF variable. This is treated as a separate structure because we must save
 * (restore) these values when entering (exiting) a structure template.
 * 
 * @author loomis
 * 
 */
public class SelfHolder implements Cloneable {

	protected Element element = null;
	protected boolean unmodifiable = false;

	@Override
	public SelfHolder clone() {

		SelfHolder copy = null;
		try {
			copy = (SelfHolder) super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw CompilerError.create(MSG_CLONE_NOT_SUPPORTED);
		}

		return copy;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public boolean isUnmodifiable() {
		return unmodifiable;
	}

}