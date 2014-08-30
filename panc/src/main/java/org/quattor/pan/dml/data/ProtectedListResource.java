package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST;

import java.util.List;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

public class ProtectedListResource extends ListResource {

	private final ListResource baseList;

	public ProtectedListResource(ListResource baseList) {
		this.baseList = baseList;
	}

	@Override
	public Element duplicate() {
		return baseList.duplicate();
	}

	@Override
	public Element get(Term key) throws InvalidTermException {
		return baseList.get(key);
	}

	@Override
	public Element put(Term key, Element newValue) throws InvalidTermException {
		throw CompilerError.create(MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST);
	}

	@Override
	public Element put(int index, Element newValue) {
		throw CompilerError.create(MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST);
	}

	@Override
	public int size() {
		return baseList.size();
	}

	@Override
	public Resource.Iterator iterator() {
		return baseList.iterator();
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		baseList.checkRange(range);
	}

	@Override
	public Element protect() {
		return this;
	}

	@Override
	public int hashCode() {
		return baseList.hashCode();
	}

	// As this class is just a wrapper around another ListResource, the
	// superclass' equals method will work as intended.
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * Override this method to return the list from the base list. This allows
	 * use of the superclass' equals method.
	 *
	 * @return backing list for list resource
	 */
	@Override
	protected List<Element> getBackingList() {
		return baseList.getBackingList();
	}

	@Override
	public String toString() {
		return baseList.toString();
	}

	@Override
	public boolean isProtected() {
		return true;
	}

	@Override
	public void rput(Term[] terms, int index, Element value)
			throws InvalidTermException {
		throw CompilerError.create(MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST);
	}

	@Override
	public Element writableCopy() {
		return new ListResource(baseList);
	}

}
