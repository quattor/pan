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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/Statement.java $
 $Id: Statement.java 3506 2008-07-30 18:09:38Z loomis $
 */

package org.quattor.pan.statement;

import clojure.lang.AFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.ttemplate.Context;
import org.quattor.pan.ttemplate.SourceRange;

import java.util.concurrent.atomic.AtomicReference;

import static org.quattor.pan.utils.MessageUtils.MSG_OPERATION_WITHOUT_CONTEXT;

/**
 * Provides the superclass of all declarative statements in the pan
 * configuration language. All declarative pan statements operate on the Context
 * of a particular machine profile through the <code>execute</code> method. The
 * arguments and flags for each statement are expected to be different, but this
 * is not important for evaluating the configuration.
 *
 * All statements must be immutable.
 *
 * Subclasses are expected to throw an exception (SyntaxException or
 * EvaluationException) if illegal parameters are passed to the constructor.
 * Similarly, if an error is encountered during the execute() method, an
 * EvaluationException should be thrown.
 *
 * @author loomis
 *
 */
abstract public class Statement extends AFn implements Operation {

	private final SourceRange sourceRange;

    private final AtomicReference<IPersistentMap> metadataRef = new AtomicReference<IPersistentMap>();

    /**
	 * The base constructor for Statement takes a SourceRange object which
	 * indicates the location of the statement within the source file. All
	 * subclasses must call this constructor as part of their constructors.
	 *
	 * @param sourceRange
	 *            the location of this statement within the source file
	 */
	public Statement(SourceRange sourceRange) {
		assert (sourceRange != null);
		this.sourceRange = sourceRange;
	}

    @Override
    public IPersistentMap meta() {
        return metadataRef.get();
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        metadataRef.set(iPersistentMap);
        return this;
    }

    public Object invoke(Object o1) {
        try {
            return execute((Context) o1);
        } catch (ClassCastException ex) {
            throw CompilerError.create(MSG_OPERATION_WITHOUT_CONTEXT);
        }
    }
	/**
	 * Retrieve the source location for this Statement.
	 *
	 * @return SourceRange object indicating the source location
	 */
	public SourceRange getSourceRange() {
		return sourceRange;
	}

	/**
	 * Execute this Statement within the given context.
	 *
	 * @param context
	 *            DML context to use for the evalution of this statement
	 *
	 * @throws EvaluationException
	 */
	abstract public Element execute(Context context) throws EvaluationException;

    public void checkRestrictedContext() throws SyntaxException {
        // not applicable for statements; no-op
    }

    public void checkInvalidSelfContext() throws SyntaxException {
        // not applicable for statements; no-op
    }

}
