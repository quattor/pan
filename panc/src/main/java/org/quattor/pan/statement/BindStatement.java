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
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.type.FullType;
import org.quattor.pan.utils.Path;

import static org.quattor.pan.utils.MessageUtils.MSG_ABSOLUTE_PATH_ONLY_FOR_BIND;

/**
 * Associates a FullType (which may have a validation function) to a path.
 *
 * @author loomis
 */
public abstract class BindStatement extends Statement {

    protected BindStatement(SourceRange sourceRange) {
        super(sourceRange);
    }

    public static BindStatement getInstance(SourceRange sourceRange, String pathname, FullType fullType) throws
            SyntaxException {

        // Check that the arguments are acceptable.
        assert (pathname != null);
        assert (fullType != null);

        // Determine if a static or dynamic bind statement should be used.
        if (isStaticPath(pathname)) {

            Path path = createPathFromIdentifier(sourceRange, pathname);
            if (!path.isAbsolute()) {
                throw SyntaxException.create(sourceRange, MSG_ABSOLUTE_PATH_ONLY_FOR_BIND, path);
            }
            return new StaticBindStatement(sourceRange, path, fullType);

        } else {
            return new DynamicBindStatement(sourceRange, pathname, fullType);
        }
    }

    private static Path createPathFromIdentifier(SourceRange sourceRange, String pathname) throws SyntaxException {

        try {
            assert (pathname != null);
            return new Path(pathname);
        } catch (EvaluationException ee) {
            throw SyntaxException.create(sourceRange, ee);
        } catch (SyntaxException se) {
            throw se.addExceptionInfo(sourceRange, null);
        }
    }

    private static boolean isStaticPath(String pathname) {

        NoOpResolver noOpResolver = new NoOpResolver();
        StrSubstitutor sub = new StrSubstitutor(noOpResolver);
        sub.replace(pathname);

        return !noOpResolver.lookupOccurred();
    }

    /**
     * This resolver will just set a flag if it is ever called.  This
     * is used to determine if a given string has variable references.
     */
    private static class NoOpResolver extends StrLookup<String> {

        private boolean lookupOccurred = false;

        public String lookup(String variable) {
            lookupOccurred = true;
            return "";
        }

        public boolean lookupOccurred() {
            return lookupOccurred;
        }
    }

}
