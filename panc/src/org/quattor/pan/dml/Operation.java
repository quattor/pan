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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/Operation.java $
 $Id: Operation.java 1807 2007-06-11 20:00:39Z loomis $
 */

package org.quattor.pan.dml;

import java.io.Serializable;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;

/**
 * All DML (data manipulation language) components implement this interface and
 * act as operators.
 * 
 * All classes implementing this interface must be serializable, so that
 * expressions can be serialized to disk as necessary.
 * 
 * @author loomis
 * 
 */
public interface Operation extends Serializable {

	/**
	 * Execute this operation within the given context. If an error occurs, an
	 * EvaluationException will be thrown with the details.
	 * 
	 * @param context
	 *            evaluation context for the statement
	 * @throws EvaluationException
	 *             if an error occurs during processing
	 * 
	 * @return Element produced by running operation
	 */
	public Element execute(Context context) throws EvaluationException;

	/**
	 * This method will check that the given operation and any arguments can be
	 * used in a restricted context such as variable indices and function
	 * arguments. This is primarily to avoid ambiguities in evaluation order for
	 * functions which operate by throwing exceptions (like return and error).
	 * This method will throw a SyntaxException if called on an Operation which
	 * cannot be used in a restricted context.
	 * 
	 * @throws SyntaxException
	 *             if this operation cannot be run within a restricted context
	 */
	public void checkRestrictedContext() throws SyntaxException;

	/**
	 * This method will check that if a given reference to SELF occurs in an
	 * invalid context. Currently this is only the include statement that
	 * doesn't define a value for SELF. (It doesn't really make sense as there
	 * is no value being assigned.) For operations that do not reference SELF,
	 * this should be a no-op. For those that do, a SyntaxException should be
	 * thrown.
	 * 
	 * @throws SyntaxException
	 *             if this operation references SELF
	 */
	public void checkInvalidSelfContext() throws SyntaxException;
}
