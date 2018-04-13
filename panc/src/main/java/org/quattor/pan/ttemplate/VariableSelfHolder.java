package org.quattor.pan.ttemplate;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.utils.GlobalVariable;

/**
 * Essentially just a structure to hold values related to the processing of the
 * SELF variable. This is treated as a separate structure because we must save
 * (restore) these values when entering (exiting) a structure template.
 * 
 * @author loomis
 * 
 */
public class VariableSelfHolder extends SelfHolder {

	private GlobalVariable variable;

	public VariableSelfHolder(GlobalVariable variable) {
		this.variable = variable;
		unmodifiable = false;

		// Retrieve an unprotected value of the variable. Since we're going to
		// modify SELF anyway, there is no need to force a copy if the value is
		// modified.
		element = variable.getUnprotectedValue();
	}

	@Override
	public void setElement(Element element) {
		if (this.element != element) {
			super.setElement(element);
			variable.setValue(element);
		}
	}

}