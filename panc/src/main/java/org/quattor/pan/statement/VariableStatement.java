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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/VariableStatement.java $
 $Id: VariableStatement.java 3612 2008-08-20 10:16:35Z loomis $
 */

package org.quattor.pan.statement;

import static org.quattor.pan.utils.MessageUtils.MSG_AUTO_VAR_CANNOT_BE_SET;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY_OR_INDEX;

import java.util.LinkedList;
import java.util.List;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.TermFactory;

/**
 * Sets a global variable to a constant or computed value.
 * 
 * @author loomis
 * 
 */
abstract public class VariableStatement extends Statement {

    /**
     * This array contains the names of 'automatic' variables. These names are
     * reserved by the pan compiler and cannot be set directly by the user.
     */
    private static final String[] automaticVariables = new String[] { "OBJECT",
            "SELF", "ARGC", "ARGV", "FUNCTION", "TEMPLATE" };

    protected final String name;

    protected final boolean conditional;

    protected final boolean modifiable;

    /**
     * Creates a VariableStatement which assign a global variable to the result
     * of a DML block.
     * 
     * @param sourceRange
     *            source location of this statement
     * @param name
     *            name of the global variable
     * @param conditional
     *            flag indicating if this is a conditional assignment
     * @param modifiable
     *            flag indicating if the variable can be modified after this
     *            statement executes (i.e. final functionality)
     */
    protected VariableStatement(SourceRange sourceRange, String name,
            boolean conditional, boolean modifiable) throws SyntaxException {

        super(sourceRange);

        assert (name != null);

        // Is name permitted? Need to ensure that this is not one of the
        // protected variable names.
        validName(name);

        // Copy in the information.
        this.name = name;
        this.conditional = conditional;
        this.modifiable = modifiable;
    }

    public static VariableStatement getInstance(SourceRange sourceRange,
            String name, Operation dml, boolean conditional, boolean modifiable)
            throws SyntaxException {

        VariableStatement result = null;

        // Run the DML block to see if it evaluates to a compile-time constant.
        // If so, use the value to produce an optimized statement.
        Element value = null;
        try {
            Context context = new CompileTimeContext();
            value = context.executeDmlBlock(dml);
        } catch (EvaluationException consumed) {
            // Ignore the exception and allow it to be caught at run time.
        }

        // Choose optimized statement to produce.
        if (value != null) {
            if ("LOADPATH".equals(name)) {
                result = new ConstantLoadpathStatement(sourceRange, value,
                        conditional, modifiable);
            } else {
                result = new ConstantVariableStatement(sourceRange, name,
                        value, conditional, modifiable);
            }
        } else {
            if ("LOADPATH".equals(name)) {
                result = new ComputedLoadpathStatement(sourceRange, dml,
                        conditional, modifiable);
            } else {
                result = new ComputedVariableStatement(sourceRange, name, dml,
                        conditional, modifiable);
            }
        }

        return result;
    }

    /**
     * A utility method to determine if the variable name collides with one of
     * the reserved 'automatic' variables.
     * 
     * @param name
     *            variable name to check
     */
    private void validName(String name) throws SyntaxException {
        for (String varName : automaticVariables) {
            if (varName.equals(name)) {
                throw SyntaxException.create(getSourceRange(),
                        MSG_AUTO_VAR_CANNOT_BE_SET, varName);
            }
        }
    }

    /**
     * Convert the given element (which should be the "LOADPATH" global
     * variable) to a list of strings. The list can be used in the template
     * loading method.
     * 
     * @param element
     *            element to convert to list of string (should be "LOADPATH"
     *            global variable)
     */
    static public List<String> convertLoadpathVariable(Element element) {

        // Create the list of strings. Always add the empty string as this will
        // force the lookup routine to always include the "current" directory.
        LinkedList<String> filelist = new LinkedList<String>();
        filelist.add("");

        // If the argument is null (the variable doesn't exist), then just
        // return the list with the current directory.
        if (element == null) {
            return filelist;
        }

        // Verify that the element is of the correct type (i.e. a list).
        if (!(element instanceof ListResource)) {
            throw new EvaluationException("LOADPATH variable must be a list");
        }

        // Loop over the values in the list.
        ListResource loadpath = (ListResource) element;
        for (int i = 0; i < loadpath.size(); i++) {
            try {
                Element child = loadpath.get(TermFactory.create(i));
                if (child instanceof StringProperty) {
                    String sfile = ((StringProperty) child).getValue();
                    if (sfile.startsWith("/")) {
                        throw new EvaluationException(
                                "LOADPATH can only contain relative paths");
                    }
                    filelist.add(sfile);
                } else {
                    throw new EvaluationException(
                            "LOADPATH contains non-string value");
                }
            } catch (InvalidTermException ite) {
                // This exception should never be encountered because the terms
                // are all indexes and the resource is a list.
                throw CompilerError.create(MSG_INVALID_KEY_OR_INDEX);
            }

        }

        return filelist;
    }

}
