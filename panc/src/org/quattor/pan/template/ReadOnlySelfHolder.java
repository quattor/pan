package org.quattor.pan.template;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;

/**
 * Essentially just a structure to hold values related to the processing of the
 * SELF variable. This is treated as a separate structure because we must save
 * (restore) these values when entering (exiting) a structure template.
 * 
 * @author loomis
 * 
 */
public class ReadOnlySelfHolder extends SelfHolder {

	public ReadOnlySelfHolder(Element element) {
		this.element = element;
		unmodifiable = true;
	}

	@Override
	public ReadOnlySelfHolder clone() {
		return (ReadOnlySelfHolder) super.clone();
	}

	public void setElement(Element element) {
		if (this.element != element) {
			throw new EvaluationException(
					"cannot modify SELF from validation function");
		}
	}

}