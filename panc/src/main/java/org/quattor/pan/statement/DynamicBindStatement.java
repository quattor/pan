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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/BindStatement.java $
 $Id: BindStatement.java 2799 2008-01-26 17:10:23Z loomis $
 */

package org.quattor.pan.statement;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.Path;

import static org.quattor.pan.utils.MessageUtils.MSG_SUBSTITUTE_VARIABLE_UNDEFINED;

/**
 * Associates a FullType (which may have a validation function) to a path.
 *
 * @author loomis
 */
public class DynamicBindStatement extends BindStatement {

    private final String pathname;

    private final FullType fullType;

    /**
     * This constructor creates a new BindStatement which associates a FullType to a particular, absolute path.
     *
     * @param sourceRange source location of this statement
     * @param pathname    absolute Path to associate with the FullType
     * @param fullType
     */
    public DynamicBindStatement(SourceRange sourceRange, String pathname, FullType fullType) throws SyntaxException {

        super(sourceRange);

        // Check that the arguments are acceptable.
        assert (pathname != null);
        assert (fullType != null);

        // Copy in the information.
        this.pathname = pathname;
        this.fullType = fullType;
    }

    @Override
    public void execute(Context context) {
        assert (context != null);

        String resolvedPath = null;
        try {
            ContextResolver resolver = new ContextResolver(context);
            StrSubstitutor sub = new StrSubstitutor(resolver);
            resolvedPath = sub.replace(pathname);
        } catch (Exception e) {
            throw new EvaluationException(e.getMessage(), getSourceRange());
        }

        Path path = createPathFromIdentifier(getSourceRange(), resolvedPath);
        context.setBinding(path, fullType, context.getCurrentTemplate(), getSourceRange());
    }

    /**
     * Return a reasonable string representation of this statement.
     *
     * @return String representation of this BindStatement
     */
    @Override
    public String toString() {
        return "BIND: " + pathname + ", " + fullType;
    }


    private static Path createPathFromIdentifier(SourceRange sourceRange, String pathname) {

        try {
            assert (pathname != null);
            return new Path(pathname);
        } catch (EvaluationException ee) {
            throw ee.addExceptionInfo(sourceRange, null);
        } catch (SyntaxException se) {
            throw new EvaluationException(se.getSimpleMessage(), sourceRange);
        }
    }

    /**
     * Resolve variable references in path by global variables in the context.
     */
    private class ContextResolver extends StrLookup<String> {

        private final Context context;

        public ContextResolver(Context context) {
            this.context = context;
        }

        public String lookup(String name) {
            try {
                return context.getGlobalVariable(name).toString();
            } catch (NullPointerException e) {
                throw EvaluationException.create(getSourceRange(), context, MSG_SUBSTITUTE_VARIABLE_UNDEFINED, name);
            }
        }
    }


}
