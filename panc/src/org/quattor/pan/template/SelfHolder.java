package org.quattor.pan.template;

import static org.quattor.pan.utils.MessageUtils.MSG_CLONE_NOT_SUPPORTED;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
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
public class SelfHolder implements Cloneable {

	public Element element = null;
	public Path path = null;
	public GlobalVariable variable = null;
	public boolean unmodifiable = false;

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

}