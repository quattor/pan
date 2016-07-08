/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/type/AliasType.java $
 $Id: AliasType.java 3601 2008-08-18 14:16:29Z loomis $
 */

package org.quattor.pan.type;

import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_REFERENCED_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_TYPE;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.TypeMap;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Range;

/**
 * Defines an alias type that references another defined type and can optionally
 * have an additional range defined.
 *
 * @author loomis
 *
 */
public class AliasType extends BaseType {

	protected final String identifier;

	private final Range range;

	/**
	 * Constructor for AliasType takes the name of the type identifier.
	 *
	 * @param source
	 *            String describing the source containing this definition
	 * @param identifier
	 *            name of the referenced type
	 * @param range
	 *            range associated with this type
	 */
	public AliasType(String source, SourceRange sourceRange, String identifier,
			Range range) {

		super(source, sourceRange);

		assert (identifier != null);

		this.identifier = identifier;
		this.range = range;
	}

	@Override
	public Element findDefault(Context context) {

		try {

			FullType type = context.getFullType(identifier);
			return type.findDefault(context);

		} catch (NullPointerException npe) {
			npe.printStackTrace();
			throw CompilerError.create(MSG_NONEXISTANT_TYPE, identifier);
		}
	}

	@Override
	public Element setDefaults(Context context, Element self)
			throws EvaluationException {

		assert (context != null);
		assert (self != null);

		Element replacement = null;

		try {

			FullType type = context.getFullType(identifier);
			replacement = type.setDefaults(context, self);

		} catch (NullPointerException npe) {
			npe.printStackTrace();
			throw CompilerError.create(MSG_NONEXISTANT_TYPE, identifier);
		}

		return replacement;
	}

	@Override
	public Object validate(final Context context, final Element self)
			throws ValidationException {

		FullType type = context.getFullType(identifier);

		try {
			type.validate(context, self);

			// Check if the range is defined.
			if (range != null) {
				self.checkRange(range);
			}

		} catch (ValidationException ve) {
			throw ve.addTypeToStack(identifier, type);

		} catch (NullPointerException npe) {
			npe.printStackTrace();
			throw CompilerError.create(MSG_NONEXISTANT_TYPE, identifier);
		}
        return null;
	}

	@Override
	public void verifySubtypesDefined(TypeMap types) {
		if (types.get(identifier) == null) {
			throw new EvaluationException(MessageUtils.format(
					MSG_NONEXISTANT_REFERENCED_TYPE, identifier));
		}
	}

	@Override
	public String toString() {
		if (range == null) {
			return identifier;
		} else {
			return identifier + "(" + range.toString() + ")";
		}
	}

}
