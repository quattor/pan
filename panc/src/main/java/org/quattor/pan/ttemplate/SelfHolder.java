package org.quattor.pan.ttemplate;

import org.quattor.pan.dml.data.Element;

/**
 * Essentially just a structure to hold values related to the processing of the
 * SELF variable. This is treated as a separate structure because we must save
 * (restore) these values when entering (exiting) a structure template.
 * 
 * @author loomis
 * 
 */
abstract public class SelfHolder {

	protected Element element = null;
	protected boolean unmodifiable = false;

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