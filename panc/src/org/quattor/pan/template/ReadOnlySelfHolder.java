package org.quattor.pan.template;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.utils.GlobalVariable;
import org.quattor.pan.utils.Path;

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
		path = null;
		variable = null;
		unmodifiable = true;
	}

	@Override
	public ReadOnlySelfHolder clone() {
		return (ReadOnlySelfHolder) super.clone();
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		if (this.element != element) {
			throw new EvaluationException(
					"cannot modify SELF from validation function");
		}
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public GlobalVariable getVariable() {
		return variable;
	}

	public void setVariable(GlobalVariable variable) {
		this.variable = variable;
	}

	public boolean isUnmodifiable() {
		return unmodifiable;
	}

	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}

}