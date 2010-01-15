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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/template/Template.java $
 $Id: Template.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.template;

import static org.quattor.pan.template.Template.TemplateType.DECLARATION;
import static org.quattor.pan.template.Template.TemplateType.OBJECT;
import static org.quattor.pan.template.Template.TemplateType.ORDINARY;
import static org.quattor.pan.template.Template.TemplateType.STRUCTURE;
import static org.quattor.pan.template.Template.TemplateType.UNIQUE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_STATEMENT_IN_STRUCT_TPL;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_STMT_IN_DECL_TPL;
import static org.quattor.pan.utils.MessageUtils.MSG_MISNAMED_TPL;
import static org.quattor.pan.utils.MessageUtils.MSG_MULTIPLY_DEFINED_FUNCTION;
import static org.quattor.pan.utils.MessageUtils.MSG_MULTIPLY_DEFINED_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_ONLY_ABS_ASSIGNMENT_ALLOWED;
import static org.quattor.pan.utils.MessageUtils.MSG_ONLY_REL_ASSIGNMENT_ALLOWED;
import static org.quattor.pan.utils.MessageUtils.MSG_TEMPLATE_CONTAINS_NON_STATIC_STATEMENTS;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.statement.AssignmentStatement;
import org.quattor.pan.statement.BindStatement;
import org.quattor.pan.statement.FunctionStatement;
import org.quattor.pan.statement.IncludeStatement;
import org.quattor.pan.statement.Statement;
import org.quattor.pan.statement.TypeStatement;
import org.quattor.pan.statement.VariableStatement;

/**
 * An immutable Template class that corresponds to a single pan language
 * template. At creation, some additional constraints are enforced which are not
 * caught by the parsing process. The constructor will throw a
 * <code>SyntaxException</code> if any of the additional constraints are not
 * met.
 * 
 * @author loomis
 * 
 */
public class Template implements Serializable {

	private static final long serialVersionUID = -1382481475232386071L;

	/**
	 * An enumeration of the possible template types. An ordinary template can
	 * contain any valid pan statements, an object template defines a machine
	 * profile, a declaration template contains only function and type
	 * declarations, and a structure template contains only relative paths.
	 */
	public static enum TemplateType {
		ORDINARY, OBJECT, DECLARATION, STRUCTURE, UNIQUE,
	};

	/**
	 * A matrix that gives a boolean flag on whether a particular include
	 * combination is allowed or not. The first index is the includee template
	 * type and the second index is the included template type. This is exposed
	 * through the checkValidInclude() method.
	 */
	private final static boolean[][] allowedIncludes;
	static {

		TemplateType[] allTypes = TemplateType.values();

		// Determine the size of the matrix that holds the allowed include
		// combinations.
		int maxIndex = 0;
		for (TemplateType t : allTypes) {
			int ordinal = t.ordinal();
			if (ordinal > maxIndex) {
				maxIndex = ordinal;
			}
		}

		// Create a matrix of the correct size and initialize everything to
		// false.
		allowedIncludes = new boolean[maxIndex + 1][maxIndex + 1];
		for (TemplateType t1 : allTypes) {
			for (TemplateType t2 : allTypes) {
				allowedIncludes[t1.ordinal()][t2.ordinal()] = false;
			}
		}

		// Object templates can include declaration and unique templates.
		allowedIncludes[OBJECT.ordinal()][ORDINARY.ordinal()] = true;
		allowedIncludes[OBJECT.ordinal()][DECLARATION.ordinal()] = true;
		allowedIncludes[OBJECT.ordinal()][UNIQUE.ordinal()] = true;

		// Ordinary templates can include declaration and unique templates.
		allowedIncludes[ORDINARY.ordinal()][ORDINARY.ordinal()] = true;
		allowedIncludes[ORDINARY.ordinal()][DECLARATION.ordinal()] = true;
		allowedIncludes[ORDINARY.ordinal()][UNIQUE.ordinal()] = true;

		// Unique templates can include declaration and ordinary templates.
		allowedIncludes[UNIQUE.ordinal()][ORDINARY.ordinal()] = true;
		allowedIncludes[UNIQUE.ordinal()][DECLARATION.ordinal()] = true;
		allowedIncludes[UNIQUE.ordinal()][UNIQUE.ordinal()] = true;

		// Structure templates can only include declaration and other structure
		// templates.
		allowedIncludes[STRUCTURE.ordinal()][DECLARATION.ordinal()] = true;
		allowedIncludes[STRUCTURE.ordinal()][STRUCTURE.ordinal()] = true;

		// Declaration templates can only include other declaration templates.
		allowedIncludes[DECLARATION.ordinal()][DECLARATION.ordinal()] = true;

	}

	/**
	 * Pattern with all valid characters for namespaced name. This also requires
	 * that the name not be the empty string.
	 */
	public static final Pattern validTemplateNameChars = Pattern
			.compile("^[\\w\\./+-]+$");

	/**
	 * Information about the source file from which this template was
	 * constructed.
	 */
	public final SourceFile sourceFile;

	/**
	 * The source from which this template was constructed. This must be an
	 * absolute File or null.
	 */
	public final File source;

	/**
	 * The (valid) name of this template.
	 */
	public final String name;

	/**
	 * The type of this template.
	 */
	public final TemplateType type;

	/**
	 * The list of statements for this template that are only executed when the
	 * template is first loaded.
	 */
	private final Statement[] staticStatements;

	/**
	 * The list of ordinary statements to be executed.
	 */
	private final Statement[] normalStatements;

	/**
	 * An empty array for reuse.
	 */
	private final static Statement[] emptyStatements = new Statement[0];

	/**
	 * Constructs a new object template with the given name which contains no
	 * statements. This is intended as a convenience for testing.
	 */
	public Template(String name) throws SyntaxException {
		this(null, null, OBJECT, name, null);
	}

	/**
	 * Constructs a new template from the given information. Additional
	 * constraints are applied which are not caught by the parser. If any of the
	 * additional constraints fail, an SyntaxException is thrown.
	 * 
	 * @param source
	 *            absolute File indicating the source pan language template;
	 *            used for error messages and logging; may be null
	 * @param sourceRange
	 *            the source range for the template declaration
	 * @param type
	 *            TemplateType for this template
	 * @param name
	 *            String indicating the name of this template
	 * @param statements
	 *            List of statements in this template (may be null)
	 */
	public Template(File source, SourceRange sourceRange, TemplateType type,
			String name, List<Statement> statements) throws SyntaxException {

		try {
			sourceFile = new SourceFile(name, true, source);
		} catch (IllegalArgumentException e) {
			throw new SyntaxException(e.getMessage(), sourceRange, source);
		}

		// Check that the given source is an absolute path. The current
		// directory may have changed; consequently we cannot simply generate an
		// absolute file from the one supplied.
		this.name = sourceFile.getName();
		this.source = sourceFile.getPath();

		// Copy in the type of this template.
		assert (type != null);
		this.type = type;

		// If the statements is null, generate an empty list.
		if (statements == null) {
			statements = new LinkedList<Statement>();
		}

		// Create the lists of statements to run. Create two separate lists to
		// allow faster execution.
		ArrayList<Statement> ss = new ArrayList<Statement>(statements.size());
		ArrayList<Statement> ns = new ArrayList<Statement>(statements.size());

		for (Statement s : statements) {
			ss.add(s);
			if (!(type == UNIQUE || type == DECLARATION
					|| s instanceof FunctionStatement
					|| s instanceof TypeStatement || s instanceof BindStatement)) {
				ns.add(s);
			}
		}

		// Create arrays from the collected and sorted statements. Do some small
		// optimizations to avoid creating unnecessary arrays. Use a static
		// empty array if there are no statements; reuse the static statement
		// array if the 'normal' statement array has the same size.
		if (ss.size() > 0) {
			staticStatements = (Statement[]) ss
					.toArray(new Statement[ss.size()]);
		} else {
			staticStatements = emptyStatements;
		}
		if (ss.size() == ns.size()) {
			normalStatements = staticStatements;
		} else if (ns.size() > 0) {
			normalStatements = (Statement[]) ns
					.toArray(new Statement[ns.size()]);
		} else {
			normalStatements = emptyStatements;
		}

		// Check that declaration template contains only type, function, and
		// include statements.
		if (type == DECLARATION) {
			for (Statement s : staticStatements) {
				if (!(s instanceof FunctionStatement)
						&& !(s instanceof TypeStatement)
						&& !(s instanceof IncludeStatement)
						&& !(s instanceof BindStatement)
						&& !(s instanceof VariableStatement)) {
					throw SyntaxException.create(s.getSourceRange(), source,
							MSG_INVALID_STMT_IN_DECL_TPL);
				}
			}
		}

		// Check that the structure template contains only assignment statements
		// with relative paths.
		if (type == STRUCTURE) {
			for (Statement s : staticStatements) {
				if (s instanceof VariableStatement
						|| s instanceof BindStatement
						|| s instanceof FunctionStatement
						|| s instanceof TypeStatement) {
					throw SyntaxException.create(s.getSourceRange(), source,
							MSG_INVALID_STATEMENT_IN_STRUCT_TPL);
				}
				if (s instanceof AssignmentStatement) {
					AssignmentStatement stmt = (AssignmentStatement) s;
					if (stmt.isAbsolute()) {
						throw SyntaxException.create(s.getSourceRange(),
								source, MSG_ONLY_REL_ASSIGNMENT_ALLOWED);
					}
				}
			}
		}

		// Check that non-structure, non-declaration templates only contain
		// assignments to absolute paths.
		if (type != STRUCTURE && type != DECLARATION) {
			for (Statement s : staticStatements) {
				if (s instanceof AssignmentStatement) {
					AssignmentStatement stmt = (AssignmentStatement) s;
					if (stmt.isRelative()) {
						throw SyntaxException.create(s.getSourceRange(),
								source, MSG_ONLY_ABS_ASSIGNMENT_ALLOWED);
					}
				}
			}
		}

		// Check for multiply-defined types.
		HashMap<String, SourceRange> mdefns = new HashMap<String, SourceRange>();
		for (Statement s : staticStatements) {
			if (s instanceof TypeStatement) {
				TypeStatement ts = (TypeStatement) s;
				SourceRange oldvalue = mdefns.put("T$" + ts.getName(), ts
						.getSourceRange());
				if (oldvalue != null) {
					throw SyntaxException.create(ts.getSourceRange(), source,
							MSG_MULTIPLY_DEFINED_TYPE, ts.getName(), oldvalue);
				}
			} else if (s instanceof FunctionStatement) {
				FunctionStatement fs = (FunctionStatement) s;
				SourceRange oldvalue = mdefns.put("F$" + fs.getName(), fs
						.getSourceRange());
				if (oldvalue != null) {
					throw SyntaxException.create(fs.getSourceRange(), source,
							MSG_MULTIPLY_DEFINED_FUNCTION, fs.getName(),
							oldvalue);
				}
			}
		}

		// Sanity check for unique and declaration templates.
		if (type == UNIQUE || type == DECLARATION) {
			if (normalStatements.length != 0) {
				throw CompilerError
						.create(MSG_TEMPLATE_CONTAINS_NON_STATIC_STATEMENTS);
			}
		}
	}

	/**
	 * Check to see if the given name is a valid template name. Valid template
	 * names may include only letters, digits, underscores, hyphens, periods,
	 * pluses, and slashes. In addition, each term when split by slashes must
	 * not be empty and must not start with a period. The second case excludes
	 * potential hidden files and special names like "." and "..".
	 * 
	 * @param name
	 *            template name to check for validity
	 * 
	 * @return boolean value indicating if the given name is a valid template
	 *         name
	 */
	static public boolean isValidTemplateName(String name) {

		// First do the easy check to make sure that the string isn't empty and
		// contains only valid characters.
		if (!validTemplateNameChars.matcher(name).matches()) {
			return false;
		}

		// Split the string on slashes and ensure that each one of the terms is
		// valid. Cannot be empty or start with a period. (Above check already
		// guarantees that only allowed characters are in the string.)
		for (String t : name.split("/")) {
			if ("".equals(t) || t.startsWith(".")) {
				return false;
			}
		}

		// If we make it to this point, then the string has passed all of the
		// checks and is valid.
		return true;
	}

	/**
	 * Execute each of the statements in turn.
	 * 
	 * @param runStatic
	 *            flag which indicates whether to run the static statements or
	 *            not
	 * @param context
	 *            context for the evaluation of the template
	 */
	public void execute(boolean runStatic, Context context) {
		for (Statement s : ((runStatic) ? staticStatements : normalStatements)) {
			s.execute(context);
		}
	}

	/**
	 * Determine whether a particular include combination is legal.
	 * 
	 * @param includeeType
	 *            type of the template that is including another one
	 * @param includedType
	 *            type of the included template
	 * 
	 * @return flag indicating whether this is a legal combination
	 */
	public static boolean checkValidInclude(TemplateType includeeType,
			TemplateType includedType) {
		return allowedIncludes[includeeType.ordinal()][includedType.ordinal()];
	}

	/**
	 * Check that the internal template name matches the expected template name.
	 * 
	 * @param expectedName
	 *            expected name of the compiled template
	 */
	public void templateNameVerification(String expectedName)
			throws SyntaxException {

		if (!name.equals(expectedName)) {
			throw SyntaxException.create(null, source, MSG_MISNAMED_TPL,
					expectedName);
		}
	}

	/**
	 * Create a reasonable string representation of this template.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.toString());
		sb.append(" template ");
		sb.append(name);
		sb.append("\n");
		sb.append("Source: ");
		sb.append(source.toString());
		sb.append("\n");
		for (Statement s : staticStatements) {
			sb.append(s.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
