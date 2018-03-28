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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/parser/PanParserUtils.java $
 $Id: PanParserUtils.java 1043 2006-11-28 10:11:11Z loomis $
 */

package org.quattor.pan.parser;

import org.quattor.pan.dml.AbstractOperation;
import org.quattor.pan.dml.DML;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.dml.functions.*;
import org.quattor.pan.dml.functions.Deprecated;
import org.quattor.pan.dml.operators.Add;
import org.quattor.pan.dml.operators.Assign;
import org.quattor.pan.dml.operators.BitAnd;
import org.quattor.pan.dml.operators.BitIOR;
import org.quattor.pan.dml.operators.BitNot;
import org.quattor.pan.dml.operators.BitXOR;
import org.quattor.pan.dml.operators.Div;
import org.quattor.pan.dml.operators.For;
import org.quattor.pan.dml.operators.Foreach;
import org.quattor.pan.dml.operators.IfElse;
import org.quattor.pan.dml.operators.LogicalAnd;
import org.quattor.pan.dml.operators.LogicalEQ;
import org.quattor.pan.dml.operators.LogicalGE;
import org.quattor.pan.dml.operators.LogicalGT;
import org.quattor.pan.dml.operators.LogicalLE;
import org.quattor.pan.dml.operators.LogicalLT;
import org.quattor.pan.dml.operators.LogicalNE;
import org.quattor.pan.dml.operators.LogicalNot;
import org.quattor.pan.dml.operators.LogicalOr;
import org.quattor.pan.dml.operators.Mod;
import org.quattor.pan.dml.operators.Mult;
import org.quattor.pan.dml.operators.SetValue;
import org.quattor.pan.dml.operators.Sub;
import org.quattor.pan.dml.operators.UnaryMinus;
import org.quattor.pan.dml.operators.UnaryPlus;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.dml.operators.While;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.parser.ASTOperation.OperationType;
import org.quattor.pan.parser.ASTStatement.StatementType;
import org.quattor.pan.statement.AssignmentStatement;
import org.quattor.pan.statement.BindStatement;
import org.quattor.pan.statement.FunctionStatement;
import org.quattor.pan.statement.IncludeStatement;
import org.quattor.pan.statement.Statement;
import org.quattor.pan.statement.TypeStatement;
import org.quattor.pan.statement.VariableStatement;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.Template;
import org.quattor.pan.type.*;
import org.quattor.pan.utils.MessageUtils;
import org.quattor.pan.utils.Path;
import org.quattor.pan.utils.Term;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

import static org.quattor.pan.utils.MessageUtils.MSG_ASSIGNMENT_HAS_NON_VARIABLE_CHILD;
import static org.quattor.pan.utils.MessageUtils.MSG_CANNOT_CREATE_FUNCTION_TABLE;
import static org.quattor.pan.utils.MessageUtils.MSG_DEF_VALUE_CANNOT_BE_UNDEF;
import static org.quattor.pan.utils.MessageUtils.MSG_DEF_VALUE_NOT_CONSTANT;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_FUNCTION_NAME;
import static org.quattor.pan.utils.MessageUtils.MSG_REACHED_IMPOSSIBLE_BRANCH;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED;

public class PanParserAstUtils {

    final private static Set<String> AUTOMATIC_VARIABLES;

    static {
        Set<String> vars = new HashSet<String>();
        vars.add("SELF");
        vars.add("OBJECT");
        vars.add("TEMPLATE");
        vars.add("FUNCTION");
        vars.add("ARGV");
        vars.add("ARGC");
        AUTOMATIC_VARIABLES = Collections.unmodifiableSet(vars);
    }

    final private static Map<String, Method> functionConstructors;

    static {
        try {

            // Create a hash for the function constructor lookup table.
            HashMap<String, Method> fc = new HashMap<String, Method>();

            // Fill the lookup table with all of the function constructors.
            fc.put("append", (Append.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("prepend", (Prepend.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("first", (First.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("next", (Next.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("delete", (Delete.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("exists", (Exists.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("return", (Return.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("nlist", (Hash.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("dict", (Hash.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("list", (org.quattor.pan.dml.functions.List.class)
                    .getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_boolean",
                    (IsBoolean.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_defined",
                    (IsDefined.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_double",
                    (IsDouble.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_long", (IsLong.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_nlist", (IsHash.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_dict", (IsHash.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_list", (IsList.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_null", (IsNull.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_number",
                    (IsNumber.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_property",
                    (IsProperty.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_resource",
                    (IsResource.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_string",
                    (IsString.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("is_valid", (IsValid.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("value", (Value.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("error",
                    (ErrorMessage.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("length", (Length.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("match", (Match.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("matches", (Matches.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("path_exists",
                    (PathExists.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_lowercase",
                    (ToLowerCase.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_uppercase",
                    (ToUpperCase.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_string",
                    (ToString.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_boolean",
                    (ToBoolean.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_long", (ToLong.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("to_double",
                    (ToDouble.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("base64_decode",
                    (Base64Decode.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("base64_encode",
                    (Base64Encode.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("json_decode",
                    (JsonDecode.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("json_encode",
                    (JsonEncode.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("escape", (Escape.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("unescape", (Unescape.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("key", (Key.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("merge", (Merge.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("substr", (Substr.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("replace",
                    (Substitute.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("replace", (Replace.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("split", (Split.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("splice", (Splice.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("index", (Index.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("clone", (Clone.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("create", (Create.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("if_exists",
                    (IfExists.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("format", (Format.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("deprecated",
                    (Deprecated.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("digest", (Digest.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("file_contents",
                    (FileContents.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("file_exists",
                    (FileExists.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("ip4_to_long",
                    (IpToLong.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("long_to_ip4",
                    (LongToIp.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("substitute",
                    (Substitute.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("debug", (Debug.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("-suppress-debug-",
                    (DebugSuppressed.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("traceback",
                    (Traceback.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("-suppress-traceback-",
                    (TracebackSuppressed.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            fc.put("join",
                    (Join.class).getDeclaredMethod("getInstance", SourceRange.class, Operation[].class));

            // Make the visible map invariant, just to make sure that no one
            // makes unexpected changes.
            functionConstructors = Collections.unmodifiableMap(fc);

        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            throw CompilerError.create(MSG_CANNOT_CREATE_FUNCTION_TABLE);
        }
    }

    static public Template convertAstToTemplate(File file, ASTTemplate ast) throws SyntaxException {

        // Create a list containing all of the statements.
        LinkedList<Statement> statements = new LinkedList<Statement>();

        // Prefixes list:
        //    1st element is the active prefix
        //        updated when new absolute prefix is defined
        //        or when relative prefix is defined
        //        (updated with absolute prefix in 2nd element prepended with relative prefix)
        //    2nd element the most recent absolute prefix
        //        updated when a new absolute prefix is defined)
        //    See convertAstToPrefixStatement
        // By default the prefix and absprefix for paths are empty.
        Path[] prefixes = {null, null};

        // Loop over all of the children, convert them to Statements, and add
        // them to the list.
        int nchild = ast.jjtGetNumChildren();
        for (int i = 0; i < nchild; i++) {
            Node n = ast.jjtGetChild(i);

            assert (n instanceof ASTStatement);

            ASTStatement snode = (ASTStatement) n;
            StatementType stype = snode.getStatementType();
            switch (stype) {
                case NOOP:
                    // Empty statement. Do nothing.
                    break;
                case BIND:
                    statements.add(convertAstToBindStatement(file.getAbsolutePath(), snode));
                    break;
                case ASSIGN:
                    statements.add(convertAstToAssignStatement(snode, prefixes[0]));
                    break;
                case VARIABLE:
                    statements.add(convertAstToVariableStatement(snode));
                    break;
                case TYPE:
                    statements.add(convertAstToTypeStatement(file.getAbsolutePath(), snode));
                    break;
                case FUNCTION:
                    statements.add(convertAstToFunctionStatement(snode));
                    break;
                case INCLUDE:
                    Statement stmt = convertAstToIncludeStatement(snode);
                    if (stmt != null) {
                        statements.add(stmt);
                    }
                    break;
                case PREFIX:
                    prefixes = convertAstToPrefixStatement(snode, prefixes[1]);
                    break;
                default:
                    assert (false);
                    break;
            }
        }

        Template t = new Template(file, ast.getSourceRange(), ast.getTemplateType(), ast.getIdentifier(), statements);
        return t;
    }

    static private Statement convertAstToBindStatement(String source, ASTStatement ast) throws SyntaxException {

        // Sanity check. Ensure that this is a bind statement.
        assert (ast.getStatementType() == StatementType.BIND);

        // Verify that there is exactly one child.
        assert (ast.jjtGetNumChildren() == 1);

        // Now check to see if the node is a FullTypeSpec or DML.
        SimpleNode child = (SimpleNode) ast.jjtGetChild(0);
        FullType fullType = null;
        if (child instanceof ASTFullTypeSpec) {
            fullType = astToFullType(source, (ASTFullTypeSpec) child);
        } else if (child instanceof ASTOperation) {
            Operation dml = astToDml((ASTOperation) child, true);
            AliasType elementType = new AliasType(null, child.getSourceRange(), "element", null);
            fullType = new FullType(source, child.getSourceRange(), elementType, null, dml);
        } else {
            assert (false);
        }

        return BindStatement.getInstance(ast.getSourceRange(), ast.getIdentifier(), fullType);
    }

    private static Path createPathFromIdentifier(ASTStatement ast) throws SyntaxException {

        try {
            String pathname = ast.getIdentifier();
            assert (pathname != null);
            return new Path(pathname);
        } catch (EvaluationException ee) {
            throw SyntaxException.create(ast.getSourceRange(), ee);
        } catch (SyntaxException se) {
            throw se.addExceptionInfo(ast.getSourceRange(), null);
        }
    }

    static private Statement convertAstToAssignStatement(ASTStatement ast, Path prefix) throws SyntaxException {

        // Sanity check. Ensure that this is a assignment statement.
        assert (ast.getStatementType() == StatementType.ASSIGN);

        AssignmentStatement statement = null;

        // Get the identifier and create the path. Resolve this against the
        // prefix if the given path is relative.
        Path path = createPathFromIdentifier(ast);
        path = Path.resolve(prefix, path);

        // Create the assignment statement.
        assert (ast.jjtGetNumChildren() <= 1);
        if (ast.jjtGetNumChildren() == 0) {

            // This is a delete statement.
            Element element = Null.VALUE;
            statement = AssignmentStatement
                    .createAssignmentStatement(ast.getSourceRange(), path, element, ast.getConditionalFlag(),
                            !ast.getFinalFlag());
        } else {

            // This is a normal assignment statement.
            ASTOperation child = (ASTOperation) ast.jjtGetChild(0);
            Operation dml = astToDml(child, true);
            statement = AssignmentStatement
                    .createAssignmentStatement(ast.getSourceRange(), path, dml, ast.getConditionalFlag(),
                            !ast.getFinalFlag());

        }

        return statement;
    }

    static private Statement convertAstToVariableStatement(ASTStatement ast) throws SyntaxException {

        // Sanity check.
        assert (ast.getStatementType() == StatementType.VARIABLE);

        // Verify that the identifier is not null.
        String vname = ast.getIdentifier();
        assert (vname != null);
        assert (ast.jjtGetNumChildren() == 1);

        // Create the assignment statement.
        ASTOperation child = (ASTOperation) ast.jjtGetChild(0);
        Operation dml = astToDml(child, true);
        return VariableStatement
                .getInstance(ast.getSourceRange(), vname, dml, ast.getConditionalFlag(), !ast.getFinalFlag());
    }

    static private Statement convertAstToTypeStatement(String source, ASTStatement ast) throws SyntaxException {

        // Sanity check.
        assert (ast.getStatementType() == StatementType.TYPE);

        // Verify that the identifier is not null.
        String tname = ast.getIdentifier();
        assert (tname != null);
        assert (ast.jjtGetNumChildren() == 1);

        // Create the assignment statement.
        ASTFullTypeSpec child = (ASTFullTypeSpec) ast.jjtGetChild(0);
        FullType fullType = astToFullType(source, child);

        return new TypeStatement(ast.getSourceRange(), tname, fullType);
    }

    static private Statement convertAstToFunctionStatement(ASTStatement ast) throws SyntaxException {

        // Sanity check.
        assert (ast.getStatementType() == StatementType.FUNCTION);

        // Get identifier and verify it isn't null.
        String fname = ast.getIdentifier();
        assert (fname != null);
        assert (ast.jjtGetNumChildren() == 1);

        // Create the assignment statement.
        ASTOperation child = (ASTOperation) ast.jjtGetChild(0);
        Operation dml = astToDml(child, true);
        return new FunctionStatement(ast.getSourceRange(), fname, dml);
    }

    static private Statement convertAstToIncludeStatement(ASTStatement ast) throws SyntaxException {

        // Sanity check.
        assert (ast.getStatementType() == StatementType.INCLUDE);

        // Include statement must always have an associated DML block. If it
        // evaluates to a compile time constant, then the operation will be
        // optimized into a static include statement.
        assert (ast.jjtGetNumChildren() == 1);

        ASTOperation child = (ASTOperation) ast.jjtGetChild(0);
        Operation dml = astToDml(child, true);

        return IncludeStatement.newIncludeStatement(ast.getSourceRange(), dml);

    }

    static private Path[] convertAstToPrefixStatement(ASTStatement ast, Path absprefix) throws SyntaxException {

        // Sanity check.
        assert (ast.getStatementType() == StatementType.PREFIX);

        // Default empty path was given so just return null to indicate there is no prefix.
        Path path = null;

        if (!"".equals(ast.getIdentifier())) {

            path = createPathFromIdentifier(ast);
            if (path.isAbsolute()) {
                // Normal path was given is absolute.
                // This is both the new/next absolute prefix and the current prefix
                absprefix = path;
            } else {
                // Normal path was given is relative.
                if (absprefix == null) {
                    // Error if absprefix does not exist
                    throw SyntaxException
                        .create(ast.getSourceRange(), MessageUtils.MSG_RELATIVE_PREFIX_REQUIRES_ABSOLUTE_PREFIX, path.toString());
                } else if (absprefix.isAbsolute()) {
                    // Do not update absprefix
                    // Relative prefix is always relative to latest absolute prefix
                    path = Path.resolve(absprefix, path);
                } else {
                    throw SyntaxException
                        .create(ast.getSourceRange(), MessageUtils.MSG_ABSOLUTE_PREFIX_REQUIRES_ABSOLUTE_PATH, path.toString(), absprefix.toString());
                }
            }

        }

        return new Path[] {path, absprefix};

    }

    static private FullType astToFullType(String source, ASTFullTypeSpec ast) throws SyntaxException {
        Operation withDml = null;
        Element defaultValue = null;

        // Sanity checks.
        assert (ast.getId() == PanParserTreeConstants.JJTFULLTYPESPEC);
        assert (ast.jjtGetNumChildren() >= 1);

        // Retrieve the base type of this full type.
        ASTTypeSpec typeSpec = (ASTTypeSpec) ast.jjtGetChild(0);
        BaseType baseType = astToType(source, typeSpec);
        assert (baseType != null);

        // Create the source range for the full type specification.
        SourceRange sourceRange = SourceRange.combineSourceRanges(typeSpec.getSourceRange());

        // Loop over the rest of the children which must be with, default, or
        // description nodes.
        int nchild = ast.jjtGetNumChildren();
        for (int i = 1; i < nchild; i++) {
            SimpleNode child = (SimpleNode) ast.jjtGetChild(i);
            assert (child.getId() == PanParserTreeConstants.JJTOPERATION);
            ASTOperation op = (ASTOperation) child;
            switch (op.getOperationType()) {
                case DEFAULT:
                    assert (op.jjtGetNumChildren() == 1);
                    assert (op.jjtGetChild(0) instanceof ASTOperation);
                    ASTOperation dml = (ASTOperation) op.jjtGetChild(0);
                    assert (dml.getOperationType() == OperationType.DML);

                    // Do not optimize DML. This guarantees that the returned value
                    // is actually a DML object with the SourceRange information.
                    DML defaultDml = (DML) astToDml(dml, false);
                    defaultValue = runDefaultDml(defaultDml);
                    sourceRange = SourceRange.combineSourceRanges(sourceRange, dml.getSourceRange());
                    break;
                case WITH:
                    assert (op.jjtGetNumChildren() == 1);
                    assert (op.jjtGetChild(0) instanceof ASTOperation);
                    ASTOperation with = (ASTOperation) op.jjtGetChild(0);
                    assert (with.getOperationType() == OperationType.DML);
                    withDml = astToDml(with, false);
                    sourceRange = SourceRange.combineSourceRanges(sourceRange, with.getSourceRange());
                    break;
                default:
                    throw CompilerError.create(MSG_REACHED_IMPOSSIBLE_BRANCH);
            }
        }

        return new FullType(source, ast.getSourceRange(), baseType, defaultValue, withDml);
    }

    static private Operation astToOperation(SimpleNode node) throws SyntaxException {

        Operation op = null;
        switch (node.getId()) {
            case PanParserTreeConstants.JJTOPERATION:
                ASTOperation onode = (ASTOperation) node;
                switch (onode.getOperationType()) {
                    case DML:
                        op = astToDml(onode, true);
                        break;
                    case PLUS:
                        op = UnaryPlus
                                .newOperation(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)));
                        break;
                    case MINUS:
                        op = UnaryMinus
                                .newOperation(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)));
                        break;
                    case NOT:
                        op = LogicalNot
                                .newOperation(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)));
                        break;
                    case BIT_NOT:
                        op = BitNot
                                .newOperation(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)));
                        break;
                    case LITERAL:
                        op = onode.getOperation();
                        break;
                    case ASSIGN:
                        op = astToAssign(onode);
                        break;
                    case IF:
                        op = astToIfElse(onode);
                        break;
                    case WHILE:
                        op = While
                                .newOperation(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                        break;
                    case FOREACH:
                        op = new Foreach(onode.getSourceRange(), astToSetValue((ASTVariable) node.jjtGetChild(0)),
                                astToSetValue((ASTVariable) node.jjtGetChild(1)),
                                astToOperation((SimpleNode) node.jjtGetChild(2)),
                                astToOperation((SimpleNode) node.jjtGetChild(3)));
                        break;
                    case FOR:
                        op = new For(onode.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                                astToOperation((SimpleNode) node.jjtGetChild(1)),
                                astToOperation((SimpleNode) node.jjtGetChild(2)),
                                astToOperation((SimpleNode) node.jjtGetChild(3)));
                        break;
                    default:
                        op = null;
                }
                break;
            case PanParserTreeConstants.JJTVARIABLE:
                op = astToVariable((ASTVariable) node, false);
                break;
            case PanParserTreeConstants.JJTLOGICALOREXPRESSION:
                op = LogicalOr.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTLOGICALANDEXPRESSION:
                op = LogicalAnd.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTBITWISEINCLUSIVEOROPERATION:
                op = BitIOR.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTBITWISEEXCLUSIVEOROPERATION:
                op = BitXOR.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTBITWISEANDOPERATION:
                op = BitAnd.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTEQOPERATION:
                op = LogicalEQ.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTNEOPERATION:
                op = LogicalNE.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTLTOPERATION:
                op = LogicalLT.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTGTOPERATION:
                op = LogicalGT.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTLEOPERATION:
                op = LogicalLE.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTGEOPERATION:
                op = LogicalGE.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTADDOPERATION:
                op = Add.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTSUBOPERATION:
                op = Sub.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTMULOPERATION:
                op = Mult.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTDIVOPERATION:
                op = Div.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTMODOPERATION:
                op = Mod.newOperation(node.getSourceRange(), astToOperation((SimpleNode) node.jjtGetChild(0)),
                        astToOperation((SimpleNode) node.jjtGetChild(1)));
                break;
            case PanParserTreeConstants.JJTFUNCTION:
                op = astToFunction((ASTFunction) node);
                break;
            default:
                assert (false);
                break;
        }
        assert (op != null);
        return op;
    }

    static public Operation astToDml(ASTOperation node, boolean optimized) throws SyntaxException {

        // Verify that this really is a DML block.
        assert (node.getOperationType() == OperationType.DML);

        // There must be at least one child of a DML statement.
        int count = node.jjtGetNumChildren();
        assert (count >= 1);

        // Create an array to hold the expressions.
        Operation[] operations = new Operation[count];

        SourceRange sourceRange = node.getSourceRange();

        // If there are any children, then process them in post-traversal order.
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                SimpleNode n = (SimpleNode) node.jjtGetChild(i);
                operations[i] = astToOperation(n);

                // Update the source range for this operation. This must be done
                // afterwards because the source range for the children may be
                // modified by iterating over them.
                sourceRange = SourceRange.combineSourceRanges(sourceRange, n.getSourceRange());
            }
        }

        if (optimized) {
            return DML.getInstance(sourceRange, operations);
        } else {
            return DML.getUnoptimizedInstance(sourceRange, operations);
        }
    }

    static private Operation astToIfElse(ASTOperation node) throws SyntaxException {

        int count = node.jjtGetNumChildren();
        assert (count >= 2);

        Operation condition = astToDml((ASTOperation) node.jjtGetChild(0), true);

        Operation trueClause = astToDml((ASTOperation) node.jjtGetChild(1), true);

        Operation falseClause = Undef.VALUE;
        if (count == 3) {
            falseClause = astToDml((ASTOperation) node.jjtGetChild(2), true);
        }

        return IfElse.newOperation(node.getSourceRange(), condition, trueClause, falseClause);
    }

    static private Operation astToFunction(ASTFunction node) throws SyntaxException {

        // Convert each of the arguments and check that the operation can appear
        // in a restricted context.
        int count = node.jjtGetNumChildren();
        Operation[] ops = new Operation[count];
        for (int i = 0; i < count; i++) {
            ops[i] = (astToDml((ASTOperation) node.jjtGetChild(i), true));
            ops[i].checkRestrictedContext();
        }

        // Explicitly disallow automatic variables being called as functions.
        if (AUTOMATIC_VARIABLES.contains(node.getName())) {
            throw SyntaxException.create(node.getSourceRange(), MSG_INVALID_FUNCTION_NAME, node.getName());
        }

        // Process 'normal' built-in functions. These don't need special
        // processing of the arguments. The constructor takes a source range and
        // a list of operations.
        Method c = functionConstructors.get(node.getName());
        if (c != null) {

            // Built-in function. Look up constructor in table and create new
            // instance.
            try {

                return (Operation) c.invoke(null, node.getSourceRange(), ops);

            } catch (InvocationTargetException ite) {

                // There may be SyntaxExceptions thrown during construction.
                // If so, rethrow them. All other exceptions are not expected
                // and should generate a compiler error.
                Throwable t = ite.getCause();
                if (t instanceof SyntaxException) {
                    throw (SyntaxException) t;
                } else {
                    CompilerError error = CompilerError.create(MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED);
                    error.initCause(t);
                    throw error;
                }

            } catch (IllegalAccessException iae) {
                CompilerError error = CompilerError.create(MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED);
                error.initCause(iae);
                throw error;

            } catch (ClassCastException cce) {
                CompilerError error = CompilerError.create(MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED);
                error.initCause(cce);
                throw error;
            }
        } else {

            // User-defined function. Create generic function wrapper.
            return new Function(node.getSourceRange(), node.getName(), ops);
        }
    }

    static private Operation astToSetValue(ASTVariable node) throws SyntaxException {

        int count = node.jjtGetNumChildren();
        Operation[] ops = new Operation[count];

        // Convert each of the children to a DML block and ensure that all of
        // the contained operations can appear in a restricted context.
        for (int i = 0; i < count; i++) {
            ops[i] = astToDml((ASTOperation) node.jjtGetChild(i), true);
            ops[i].checkRestrictedContext();
        }

        return SetValue.getInstance(node.getSourceRange(), node.getName(), ops);
    }

    static private Operation astToAssign(ASTOperation node) throws SyntaxException {

        int count = node.jjtGetNumChildren();
        Operation[] ops = new Operation[count];

        // The last child is the DML block giving the value. Put that first into
        // the operations.
        ops[0] = astToOperation((SimpleNode) node.jjtGetChild(count - 1));

        // Convert all of the rest to SetValue operations. This is done in
        // reverse order.
        for (int i = count - 2; i >= 0; i--) {
            try {
                int index = count - 1 - i;
                ops[index] = astToSetValue((ASTVariable) node.jjtGetChild(i));
            } catch (ClassCastException cce) {
                throw CompilerError.create(MSG_ASSIGNMENT_HAS_NON_VARIABLE_CHILD);
            }
        }

        return new Assign(node.getSourceRange(), ops);
    }

    static private Operation astToVariable(ASTVariable node, boolean lookupOnly) throws SyntaxException {

        int count = node.jjtGetNumChildren();
        Operation[] ops = new Operation[count];

        // Convert each child index and ensure all operations are valid in a
        // restricted context.
        for (int i = 0; i < count; i++) {
            ops[i] = (astToDml((ASTOperation) node.jjtGetChild(i), true));
            ops[i].checkRestrictedContext();
        }

        return Variable.getInstance(node.getSourceRange(), node.getName(), lookupOnly, ops);
    }

    static private BaseType astToType(String source, SimpleNode node) throws SyntaxException {

        BaseType baseType = null;

        // Sanity checking.
        assert (node.jjtGetNumChildren() >= 1);
        assert (node.getId() == PanParserTreeConstants.JJTTYPESPEC);

        // Get the base type specification.
        ASTBaseTypeSpec base = (ASTBaseTypeSpec) node.jjtGetChild(0);
        String identifier = base.getIdentifier();
        if (identifier == null) {
            // This is a record specification.

            // Create the list and map to hold the field specifications.
            List<String> includes = new LinkedList<String>();
            TreeMap<Term, FullType> reqFields = new TreeMap<Term, FullType>();
            TreeMap<Term, FullType> optFields = new TreeMap<Term, FullType>();

            // Loop over all of the children. These must be field
            // specifications.
            int nfields = base.jjtGetNumChildren();
            for (int i = 0; i < nfields; i++) {
                SimpleNode fchild = (SimpleNode) base.jjtGetChild(i);
                assert (fchild instanceof ASTFieldSpec);
                ASTFieldSpec field = (ASTFieldSpec) fchild;

                String include = field.getInclude();
                if (include == null) {
                    // Do normal field.
                    Term term = field.getKey();
                    assert (field.jjtGetNumChildren() == 1);
                    SimpleNode ftypeNode = (SimpleNode) field.jjtGetChild(0);
                    assert (ftypeNode.getId() == PanParserTreeConstants.JJTFULLTYPESPEC);
                    FullType ftype = astToFullType(source, (ASTFullTypeSpec) ftypeNode);

                    if (field.isRequired()) {
                        reqFields.put(term, ftype);
                    } else {
                        optFields.put(term, ftype);
                    }
                } else {
                    // Do include.
                    includes.add(include);
                }
            }

            baseType = new RecordType(source, base.getSourceRange(), base.isExtensible(), base.getRange(), includes,
                    reqFields, optFields);
        } else {
            // This is an alias or an advanced type.
            if (identifier.equals("choice")) {
                baseType = astToChoiceType(base, source);
            } else {
                baseType = new AliasType(source, base.getSourceRange(), identifier, base.getRange());
            }
        }

        // Loop over all remaining children. They must all be type clauses.
        int nchild = node.jjtGetNumChildren();
        for (int i = 1; i < nchild; i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            assert (child.getId() == PanParserTreeConstants.JJTTYPECLAUSE);
            ASTTypeClause clause = (ASTTypeClause) child;

            switch (clause.getClauseType()) {
                case LIST:
                    baseType = new ListType(source, clause.getSourceRange(), baseType, clause.getRange());
                    break;
                case HASH:
                    baseType = new HashType(source, clause.getSourceRange(), baseType, clause.getRange());
                    break;
                case LINK:
                    baseType = new LinkType(source, clause.getSourceRange(), baseType);
                    break;
                default:
                    assert (false);
                    break;
            }
        }

        return baseType;
    }

    static private ChoiceType astToChoiceType(ASTBaseTypeSpec node, String source) throws SyntaxException {
        List<Element> list = new ArrayList<Element>();

        // Extract all the possible choices.
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode sn = (SimpleNode) node.jjtGetChild(i);
            Element e = (StringProperty) astToOperation(sn);
            list.add(e);
        }

        return new ChoiceType(source, node.getSourceRange(), list);
    }

    static private Element runDefaultDml(Operation dml) throws SyntaxException {

        // If the argument is null, return null as the value immediately.
        if (dml == null) {
            return null;
        }

        // Create a nearly empty execution context. There are no global
        // variables by default (including no 'self' variable). Only the
        // standard built-in functions are accessible.
        Context context = new CompileTimeContext();

        Element value = null;

        // IF this is an AbstractOperation, pull out the source location.
        SourceRange sourceRange = null;
        if (dml instanceof AbstractOperation) {
            AbstractOperation op = (AbstractOperation) dml;
            sourceRange = op.getSourceRange();
        }

        // Execute the DML block. The block must evaluate to an Element. Any
        // error is fatal for the compilation.
        try {
            value = context.executeDmlBlock(dml);
        } catch (EvaluationException ee) {
            SyntaxException se = SyntaxException.create(sourceRange, MSG_DEF_VALUE_NOT_CONSTANT);
            se.initCause(ee);
            throw se;
        }

        // The default value cannot be either undef or null. Throw a syntax
        // error if that is the case.
        if (value instanceof Undef) {
            throw SyntaxException.create(sourceRange, MSG_DEF_VALUE_CANNOT_BE_UNDEF);
        } else if (value instanceof Null) {
            throw SyntaxException.create(sourceRange, MSG_DEF_VALUE_CANNOT_BE_UNDEF);
        }

        // Looks Ok; return the value.
        return value;
    }
}
