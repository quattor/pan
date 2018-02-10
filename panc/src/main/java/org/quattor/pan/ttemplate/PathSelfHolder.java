package org.quattor.pan.ttemplate;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.utils.Path;

/**
 * Essentially just a structure to hold values related to the processing of the
 * SELF variable. This is treated as a separate structure because we must save
 * (restore) these values when entering (exiting) a structure template.
 * 
 * @author loomis
 * 
 */
public class PathSelfHolder extends SelfHolder {

	private Path path;
	private Context context;

	public PathSelfHolder(Path path, Context context) {
		this.path = path;
		this.context = context;
		element = null;
		unmodifiable = false;

		element = context.getElement(path, false);
		if (element == null) {
			context.putElement(path, Undef.VALUE);
			element = Undef.VALUE;
		}
	}

	@Override
	public void setElement(Element element) {
		if (this.element != element) {
			super.setElement(element);
			context.putElement(path, element);
		}
	}

}