package org.quattor.pan.utils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.quattor.pan.exceptions.CompilerError;

/**
 * Utilities to allow localization of compiler error messages.
 * 
 * @author loomis
 */
public class MessageUtils {

	public final static String MSG_FILE_BUG_REPORT = "MSG_FILE_BUG_REPORT";

	public final static String MSG_INVALID_ARGS_ADD = "MSG_INVALID_ARGS_ADD";

	public final static String MSG_MISMATCHED_ARGS_ADD = "MSG_MISMATCHED_ARGS_ADD";

	public final static String MSG_INVALID_ARGS_BITAND = "MSG_INVALID_ARGS_BITAND";

	public final static String MSG_INVALID_ARGS_BITIOR = "MSG_INVALID_ARGS_BITIOR";

	public final static String MSG_INVALID_ARGS_BITNOT = "MSG_INVALID_ARGS_BITNOT";

	public final static String MSG_INVALID_ARGS_BITXOR = "MSG_INVALID_ARGS_BITXOR";

	public final static String MSG_INVALID_ARGS_DIV = "MSG_INVALID_ARGS_DIV";

	public final static String MSG_DIVISION_BY_ZERO = "MSG_DIVISION_BY_ZERO";

	public final static String MSG_INVALID_FOREACH_RESOURCE = "MSG_INVALID_FOREACH_RESOURCE";

	public final static String MSG_CONCURRENT_MODIFICATION = "MSG_CONCURRENT_MODIFICATION";

	public final static String MSG_INVALID_IF_ELSE_TEST = "MSG_INVALID_IF_ELSE_TEST";

	public final static String MSG_INVALID_FIRST_ARG_L_AND = "MSG_INVALID_FIRST_ARG_L_AND";

	public final static String MSG_INVALID_SECOND_ARG_L_AND = "MSG_INVALID_SECOND_ARG_L_AND";

	public final static String MSG_INVALID_ARGS_L_AND = "MSG_INVALID_ARGS_L_AND";

	public final static String MSG_INVALID_ARGS_EQ = "MSG_INVALID_ARGS_EQ";

	public final static String MSG_MISMATCHED_ARGS_EQ = "MSG_MISMATCHED_ARGS_EQ";

	public final static String MSG_INVALID_ARGS_GE = "MSG_INVALID_ARGS_GE";

	public final static String MSG_MISMATCHED_ARGS_GE = "MSG_MISMATCHED_ARGS_GE";

	public final static String MSG_INVALID_ARGS_GT = "MSG_INVALID_ARGS_GT";

	public final static String MSG_MISMATCHED_ARGS_GT = "MSG_MISMATCHED_ARGS_GT";

	public final static String MSG_INVALID_ARGS_LE = "MSG_INVALID_ARGS_LE";

	public final static String MSG_MISMATCHED_ARGS_LE = "MSG_MISMATCHED_ARGS_LE";

	public final static String MSG_INVALID_ARGS_LT = "MSG_INVALID_ARGS_LT";

	public final static String MSG_MISMATCHED_ARGS_LT = "MSG_MISMATCHED_ARGS_LT";

	public final static String MSG_INVALID_ARGS_NE = "MSG_INVALID_ARGS_NE";

	public final static String MSG_MISMATCHED_ARGS_NE = "MSG_MISMATCHED_ARGS_NE";

	public final static String MSG_INVALID_ARGS_NOT = "MSG_INVALID_ARGS_NOT";

	public final static String MSG_INVALID_FIRST_ARG_L_OR = "MSG_INVALID_FIRST_ARG_L_OR";

	public final static String MSG_INVALID_SECOND_ARG_L_OR = "MSG_INVALID_SECOND_ARG_L_OR";

	public final static String MSG_INVALID_ARGS_L_OR = "MSG_INVALID_ARGS_L_OR";

	public final static String MSG_INVALID_ARGS_MOD = "MSG_INVALID_ARGS_MOD";

	public final static String MSG_INVALID_SECOND_ARG_MOD = "MSG_INVALID_SECOND_ARG_MOD";

	public final static String MSG_INVALID_ARGS_MULT = "MSG_INVALID_ARGS_MULT";

	public final static String MSG_ILLEGAL_SELF_REF = "MSG_ILLEGAL_SELF_REF";

	public final static String MSG_INVALID_ARGS_SUB = "MSG_INVALID_ARGS_SUB";

	public final static String MSG_INVALID_ARGS_UMINUS = "MSG_INVALID_ARGS_UMINUS";

	public final static String MSG_INVALID_ARGS_UPLUS = "MSG_INVALID_ARGS_UPLUS";

	public final static String MSG_UNDEFINED_VAR = "MSG_UNDEFINED_VAR";

	public final static String MSG_ILLEGAL_DEREFERENCE = "MSG_ILLEGAL_DEREFERENCE";

	public final static String MSG_INVALID_LOOP_TEST = "MSG_INVALID_LOOP_TEST";

	public final static String MSG_LOOP_ITERATION_LIMIT = "MSG_LOOP_ITERATION_LIMIT";

	public final static String MSG_NUMBER_OUTSIDE_RANGE = "MSG_NUMBER_OUTSIDE_RANGE";

	public final static String MSG_INVALID_RANGE_CHECK = "MSG_INVALID_RANGE_CHECK";

	public final static String MSG_INVALID_HASH_KEY = "MSG_INVALID_HASH_KEY";

	public final static String MSG_INVALID_REPLACEMENT = "MSG_INVALID_REPLACEMENT";

	public final static String MSG_HASH_SIZE_OUTSIDE_RANGE = "MSG_HASH_SIZE_OUTSIDE_RANGE";

	public final static String MSG_INVALID_LIST_INDEX = "MSG_INVALID_LIST_INDEX";

	public final static String MSG_NONEXISTANT_LIST_ELEMENT = "MSG_NONEXISTANT_LIST_ELEMENT";

	public final static String MSG_LIST_SIZE_OUTSIDE_RANGE = "MSG_LIST_SIZE_OUTSIDE_RANGE";

	public final static String MSG_STRING_SIZE_OUTSIDE_RANGE = "MSG_STRING_SIZE_OUTSIDE_RANGE";

	public final static String MSG_INVALID_NO_ARGS_BASE64_DECODE = "MSG_INVALID_NO_ARGS_BASE64_DECODE";

	public final static String MSG_INVALID_ARGS_BASE64_DECODE = "MSG_INVALID_ARGS_BASE64_DECODE";

	public final static String MSG_INVALID_NO_ARGS_BASE64_ENCODE = "MSG_INVALID_NO_ARGS_BASE64_ENCODE";

	public final static String MSG_INVALID_ARGS_BASE64_ENCODE = "MSG_INVALID_ARGS_BASE64_ENCODE";

	public final static String MSG_INVALID_NO_ARGS_CLONE = "MSG_INVALID_NO_ARGS_CLONE";

	public final static String MSG_INVALID_NO_ARGS_CREATE = "MSG_INVALID_NO_ARGS_CREATE";

	public final static String MSG_INVALID_FIRST_ARG_CREATE = "MSG_INVALID_FIRST_ARG_CREATE";

	public final static String MSG_INVALID_KEY_CREATE_STRINGS = "MSG_INVALID_KEY_CREATE_STRINGS";

	public final static String MSG_INVALID_KEY_CREATE = "MSG_INVALID_KEY_CREATE";

	public final static String MSG_INVALID_KEY_HASH = "MSG_INVALID_KEY_HASH";

	public final static String MSG_NONEXISTANT_TYPE = "MSG_NONEXISTANT_TYPE";

	public final static String MSG_NONEXISTANT_REFERENCED_TYPE = "MSG_NONEXISTANT_REFERENCED_TYPE";

	public final static String MSG_MISMATCHED_TYPES = "MSG_MISMATCHED_TYPES";

	public final static String MSG_CONFLICTING_TYPES = "MSG_CONFLICTING_TYPES";

	public final static String MSG_INVALID_URI = "MSG_INVALID_URI";

	public final static String MSG_NONEXISTANT_ELEMENT = "MSG_NONEXISTANT_ELEMENT";

	public final static String MSG_USER_VALIDATION_FAILED = "MSG_USER_VALIDATION_FAILED";

	public final static String MSG_INVALID_LINK_PATH = "MSG_INVALID_LINK_PATH";

	public final static String MSG_NONEXISTANT_LINK_ELEMENT = "MSG_NONEXISTANT_LINK_ELEMENT";

	public final static String MSG_PATH_EVAL_ERROR = "MSG_PATH_EVAL_ERROR";

	public final static String MSG_LINK_ELEMENT_FAILED_VALIDATION = "MSG_LINK_ELEMENT_FAILED_VALIDATION";

	public final static String MSG_NONRECORD_TYPE_REF = "MSG_NONRECORD_TYPE_REF";

	public final static String MSG_UNEXPECTED_FIELDS = "MSG_UNEXPECTED_FIELDS";

	public final static String MSG_MISSING_FIELD = "MSG_MISSING_FIELD";

	public final static String MSG_CANNOT_INCLUDE_TYPE = "MSG_CANNOT_INCLUDE_TYPE";

	public final static String MSG_PATH_INVALID_AUTHORITY = "MSG_PATH_INVALID_AUTHORITY";

	public final static String MSG_PATH_INVALID_BRACES = "MSG_PATH_INVALID_BRACES";

	public final static String MSG_PATH_MISSING_TERM = "MSG_PATH_MISSING_TERM";

	public final static String MSG_PATH_INVALID_FIRST_TERM = "MSG_PATH_INVALID_FIRST_TERM";

	public final static String MSG_EXTERNAL_PATH_NOT_ALLOWED = "MSG_EXTERNAL_PATH_NOT_ALLOWED";

	public final static String MSG_RELATIVE_PATH_NOT_ALLOWED = "MSG_RELATIVE_PATH_NOT_ALLOWED";

	public final static String MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH = "MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH";

	public final static String MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST = "MSG_ILLEGAL_WRITE_TO_PROTECTED_LIST";

	public final static String MSG_CANNOT_ADD_CHILD = "MSG_CANNOT_ADD_CHILD";

	public final static String MSG_INVALID_PATH_INDEX = "MSG_INVALID_PATH_INDEX";

	public final static String MSG_INVALID_KEY_OR_INDEX = "MSG_INVALID_KEY_OR_INDEX";

	public final static String MSG_NO_VALUE_FOR_PATH = "MSG_NO_VALUE_FOR_PATH";

	public final static String MSG_INVALID_PATH_DEREFERENCE = "MSG_INVALID_PATH_DEREFERENCE";

	public final static String MSG_CANNOT_MODIFY_GLOBAL_VARIABLE_FROM_DML = "MSG_CANNOT_MODIFY_GLOBAL_VARIABLE_FROM_DML";

	public final static String MSG_VALUE_AT_PATH_UNDEFINED = "MSG_VALUE_AT_PATH_UNDEFINED";

	public final static String MSG_VALIDATION_FAILED_BECAUSE_OF_EXCEPTION = "MSG_VALIDATION_FAILED_BECAUSE_OF_EXCEPTION";

	public final static String MSG_INVALID_VALIDATION_FUNCTION_RETURN_TYPE = "MSG_INVALID_VALIDATION_FUNCTION_RETURN_TYPE";

	public final static String MSG_INVALID_TERM = "MSG_INVALID_TERM";

	public final static String MSG_INVALID_LIST_TERM = "MSG_INVALID_LIST_TERM";

	public final static String MSG_ONE_STRING_ARG_REQ = "MSG_ONE_STRING_ARG_REQ";

	public final static String MSG_FIRST_STRING_ARG_REQ = "MSG_FIRST_STRING_ARG_REQ";

	public final static String MSG_SECOND_STRING_ARG_REQ = "MSG_SECOND_STRING_ARG_REQ";

	public final static String MSG_ONE_ARG_REQ = "MSG_ONE_ARG_REQ";

	public final static String MSG_ONE_OR_TWO_ARGS_REQ = "MSG_ONE_OR_TWO_ARGS_REQ";

	public final static String MSG_ONE_OR_MORE_ARG_REQ = "MSG_ONE_OR_MORE_ARG_REQ";

	public final static String MSG_TWO_ARGS_REQ = "MSG_TWO_ARGS_REQ";

	public final static String MSG_3_ARGS_REQ = "MSG_3_ARGS_REQ";

	public final static String MSG_RESTRICTED_CONTEXT = "MSG_RESTRICTED_CONTEXT";

	public final static String MSG_EVEN_NUMBER_OF_ARGS = "MSG_EVEN_NUMBER_OF_ARGS";

	public final static String MSG_2_OR_3_ARGS = "MSG_2_OR_3_ARGS";

	public final static String MSG_3_OR_4_ARGS = "MSG_3_OR_4_ARGS";

	public final static String MSG_NO_ASSIGNMENT_TO_EXTERNAL_PATH = "MSG_NO_ASSIGNMENT_TO_EXTERNAL_PATH";

	public final static String MSG_DML_MUST_BE_STRING_NULL_OR_UNDEF = "MSG_DML_MUST_BE_STRING_NULL_OR_UNDEF";

	public final static String MSG_ABSOLUTE_PATH_ONLY_FOR_BIND = "MSG_ABSOLUTE_PATH_ONLY_FOR_BIND";

	public final static String MSG_INVALID_IDENTIFIER = "MSG_INVALID_IDENTIFIER";

	public final static String MSG_AUTO_VAR_CANNOT_BE_SET = "MSG_AUTO_VAR_CANNOT_BE_SET";

	public final static String MSG_FIELD_MUST_BE_VALID_PATH = "MSG_FIELD_MUST_BE_VALID_PATH";

	public final static String MSG_FIELD_MUST_BE_VALID_TERM = "MSG_FIELD_MUST_BE_VALID_TERM";

	public final static String MSG_FIELD_MUST_BE_VALID_KEY = "MSG_FIELD_MUST_BE_VALID_KEY";

	public final static String MSG_FIRST_ARG_VARIABLE_REF = "MSG_FIRST_ARG_VARIABLE_REF";

	public final static String MSG_FIRST_ARG_LIST_OR_VARIABLE_REF = "MSG_FIRST_ARG_LIST_OR_VARIABLE_REF";

	public final static String MSG_DEF_VALUE_NOT_CONSTANT = "MSG_DEF_VALUE_NOT_CONSTANT";

	public final static String MSG_DEF_VALUE_CANNOT_BE_UNDEF = "MSG_DEF_VALUE_CANNOT_BE_UNDEF";

	public final static String MSG_VARIABLE_REF_OR_UNDEF = "MSG_VARIABLE_REF_OR_UNDEF";

	public final static String MSG_INVALID_TPL_NAME = "MSG_INVALID_TPL_NAME";

	public final static String MSG_MISNAMED_TPL = "MSG_MISNAMED_TPL";

	public final static String MSG_INVALID_STMT_IN_DECL_TPL = "MSG_INVALID_STMT_IN_DECL_TPL";

	public final static String MSG_ONLY_REL_ASSIGNMENT_ALLOWED = "MSG_ONLY_REL_ASSIGNMENT_ALLOWED";

	public final static String MSG_ONLY_ABS_ASSIGNMENT_ALLOWED = "MSG_ONLY_ABS_ASSIGNMENT_ALLOWED";

	public final static String MSG_MULTIPLY_DEFINED_TYPE = "MSG_MULTIPLY_DEFINED_TYPE";

	public final static String MSG_MULTIPLY_DEFINED_FUNCTION = "MSG_MULTIPLY_DEFINED_FUNCTION";

	public final static String MSG_PATH_OR_TPL_NAME_REQ = "MSG_PATH_OR_TPL_NAME_REQ";

	public final static String MSG_ONE_STRING_OR_IDENTIFIER_REQ = "MSG_ONE_STRING_OR_IDENTIFIER_REQ";

	public final static String MSG_INVALID_ARG_IN_CONSTRUCTOR = "MSG_INVALID_ARG_IN_CONSTRUCTOR";

	public final static String MSG_NULL_RESULT_FROM_OPERATION = "MSG_NULL_RESULT_FROM_OPERATION";

	public final static String MSG_CLONE_NOT_SUPPORTED = "MSG_CLONE_NOT_SUPPORTED";

	public final static String MSG_INVALID_STATEMENT_IN_STRUCT_TPL = "MSG_INVALID_STATEMENT_IN_STRUCT_TPL";

	public final static String MSG_ALL_STRING_ARGS_REQ = "MSG_ALL_STRING_ARGS_REQ";

	public final static String MSG_INVALID_REGEXP = "MSG_INVALID_REGEXP";

	public final static String MSG_INVALID_REGEXP_FLAG = "MSG_INVALID_REGEXP_FLAG";

	public final static String MSG_INVALID_CALL_TO_STATIC_MATCHER = "MSG_INVALID_CALL_TO_STATIC_MATCHER";

	public final static String MSG_OBJECT_DEPENDENCY_RAISED_EXCEPTION = "MSG_OBJECT_DEPENDENCY_RAISED_EXCEPTION";

	public final static String MSG_INTERRUPTED_THREAD = "MSG_INTERRUPTED_THREAD";

	public final static String MSG_CANCELLED_THREAD = "MSG_CANCELLED_THREAD";

	public final static String MSG_CIRCULAR_OBJECT_DEPENDENCY = "MSG_CIRCULAR_OBJECT_DEPENDENCY";

	public final static String MSG_INVALID_FIRST_ARG_DEPRECATED = "MSG_INVALID_FIRST_ARG_DEPRECATED";

	public final static String MSG_INVALID_SECOND_ARG_DEPRECATED = "MSG_INVALID_SECOND_ARG_DEPRECATED";

	public final static String MSG_CANNOT_CREATE_OUTPUT_DIRECTORY = "MSG_CANNOT_CREATE_OUTPUT_DIRECTORY";

	public final static String MSG_CANNOT_LOCATE_TEMPLATE = "MSG_CANNOT_LOCATE_TEMPLATE";

	public final static String MSG_CANNOT_LOCATE_OBJECT_TEMPLATE = "MSG_CANNOT_LOCATE_OBJECT_TEMPLATE";

	public final static String MSG_INDEX_EXCEEDS_MAXIMUM = "MSG_INDEX_EXCEEDS_MAXIMUM";

	public final static String MSG_INDEX_CANNOT_BE_NEGATIVE = "MSG_INDEX_CANNOT_BE_NEGATIVE";

	public final static String MSG_KEY_CANNOT_BE_EMPTY_STRING = "MSG_KEY_CANNOT_BE_EMPTY_STRING";

	public final static String MSG_KEY_CANNOT_BEGIN_WITH_DIGIT = "MSG_KEY_CANNOT_BEGIN_WITH_DIGIT";

	public final static String MSG_INVALID_ELEMENT_FOR_INDEX = "MSG_INVALID_ELEMENT_FOR_INDEX";

	public final static String MSG_INVALID_KEY = "MSG_INVALID_KEY";

	public final static String MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS = "MSG_NON_ABSOLUTE_PATH_IN_INCLUDE_DIRS";

	public final static String MSG_NON_DIRECTORY_IN_INCLUDE_DIRS = "MSG_NON_DIRECTORY_IN_INCLUDE_DIRS";

	public final static String MSG_MIN_RANGE_VALUE_CANNOT_BE_NEGATIVE = "MSG_MIN_RANGE_VALUE_CANNOT_BE_NEGATIVE";

	public final static String MSG_MIN_MUST_BE_LESS_OR_EQUAL_TO_MAX = "MSG_MIN_MUST_BE_LESS_OR_EQUAL_TO_MAX";

	public final static String MSG_MIN_RANGE_VALUE_IS_NOT_VALID_LONG = "MSG_MIN_RANGE_VALUE_IS_NOT_VALID_LONG";

	public final static String MSG_MAX_RANGE_VALUE_IS_NOT_VALID_LONG = "MSG_MAX_RANGE_VALUE_IS_NOT_VALID_LONG";

	public final static String MSG_CANNOT_MODIFY_FINAL_GLOBAL_VARIABLE = "MSG_CANNOT_MODIFY_FINAL_GLOBAL_VARIABLE";

	public final static String MSG_ATTEMPT_TO_REPLACE_EXISTING_NODE = "MSG_ATTEMPT_TO_REPLACE_EXISTING_NODE";

	public final static String MSG_UNKNOWN_PROPERTY_TYPE = "MSG_UNKNOWN_PROPERTY_TYPE";

	public final static String MSG_INVALID_OPERATION_IN_ASSIGN = "MSG_INVALID_OPERATION_IN_ASSIGN";

	public final static String MSG_INVALID_EXECUTE_METHOD_CALLED = "MSG_INVALID_EXECUTE_METHOD_CALLED";

	public final static String MSG_DUPLICATE_FORMATTER_KEY = "MSG_DUPLICATE_FORMATTER_KEY";

	public final static String MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT = "MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT";

	public final static String MSG_MISSING_SAX_TRANSFORMER = "MSG_MISSING_SAX_TRANSFORMER";

	public final static String MSG_TEMPLATE_CONTAINS_NON_STATIC_STATEMENTS = "MSG_TEMPLATE_CONTAINS_NON_STATIC_STATEMENTS";

	public final static String MSG_CANNOT_CREATE_EMPTY_TEMPLATE = "MSG_CANNOT_CREATE_EMPTY_TEMPLATE";

	public final static String MSG_INVALID_ATTEMPT_TO_SET_RELATIVE_PATH = "MSG_INVALID_ATTEMPT_TO_SET_RELATIVE_PATH";

	public final static String MSG_INVALID_ATTEMPT_TO_SET_EXTERNAL_PATH = "MSG_INVALID_ATTEMPT_TO_SET_EXTERNAL_PATH";

	public final static String MSG_INVALID_EMPTY_RELATIVE_PATH = "MSG_INVALID_EMPTY_RELATIVE_PATH";

	public final static String MSG_FINAL_FOR_NON_EXISTANT_VARIABLE = "MSG_FINAL_FOR_NON_EXISTANT_VARIABLE";

	public final static String MSG_INVALID_PATH_ASSIGNMENT = "MSG_INVALID_PATH_ASSIGNMENT";

	public final static String MSG_REACHED_IMPOSSIBLE_BRANCH = "MSG_REACHED_IMPOSSIBLE_BRANCH";

	public final static String MSG_CANNOT_CREATE_FUNCTION_TABLE = "MSG_CANNOT_CREATE_FUNCTION_TABLE";

	public final static String MSG_ASSIGNMENT_HAS_NON_VARIABLE_CHILD = "MSG_ASSIGNMENT_HAS_NON_VARIABLE_CHILD";

	public final static String MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED = "MSG_UNEXPECTED_EXCEPTION_ENCOUNTERED";

	public final static String MSG_NULL_ELEMENT_IN_CONFIGURATION = "MSG_NULL_ELEMENT_IN_CONFIGURATION";

	public final static String MSG_VALUE_CANNOT_BE_NULL = "MSG_VALUE_CANNOT_BE_NULL";

	public final static String MSG_DUPLICATE_KEY = "MSG_DUPLICATE_KEY";

	public final static String MSG_INVALID_KEY_TYPE = "MSG_INVALID_KEY_TYPE";

	public final static String MSG_REFERENCED_VARIABLE_NOT_LIST = "MSG_REFERENCED_VARIABLE_NOT_LIST";

	public final static String MSG_CANNOT_MODIFY_SELF = "MSG_CANNOT_MODIFY_SELF";

	public final static String MSG_SELF_IS_UNDEFINED = "MSG_SELF_IS_UNDEFINED";

	public final static String MSG_INVALID_IN_COMPILE_TIME_CONTEXT = "MSG_INVALID_IN_COMPILE_TIME_CONTEXT";

	public final static String MSG_INVALID_DIGEST_ALGORITHM = "MSG_INVALID_DIGEST_ALGORITHM";

	public final static String MSG_RELATIVE_FILE_REQ = "MSG_RELATIVE_FILE_REQ";

	public final static String MSG_NONEXISTANT_FILE = "MSG_NONEXISTANT_FILE";

	public final static String MSG_DIR_NOT_ALLOWED = "MSG_DIR_NOT_ALLOWED";

	public final static String MSG_ABSOLUTE_PATH_REQ = "MSG_ABSOLUTE_PATH_REQ";

	public final static String MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL = "MSG_SRC_FILE_NAME_OR_TYPE_IS_NULL";

	public final static String MSG_ABSENT_FILE_MUST_HAVE_NULL_PATH = "MSG_ABSENT_FILE_MUST_HAVE_NULL_PATH";

	public final static String MSG_INVALID_ANNOTATION_NAME_OR_KEY = "MSG_INVALID_ANNOTATION_NAME_OR_KEY";

	public final static String MSG_INVALID_ANNOTATION_NULL_VALUE = "MSG_INVALID_ANNOTATION_NULL_VALUE";

	public final static String MSG_INVALID_ANNOTATION_SYNTAX = "MSG_INVALID_ANNOTATION_SYNTAX";

	public final static String MSG_ERROR_WHILE_WRITING_OUTPUT = "MSG_ERROR_WHILE_WRITING_OUTPUT";

	public final static String MSG_INVALID_SELF_REF_IN_INCLUDE = "MSG_INVALID_SELF_REF_IN_INCLUDE";

	// Name of the resource bundle to use for compiler messages.
	private final static String bundleName = "org.quattor.pan.Messages";

	// Create the resource bundle that contains all of the compiler messages.
	private final static ResourceBundle bundle;
	static {
		try {
			bundle = ResourceBundle.getBundle(bundleName);
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			throw new CompilerError(mre.getLocalizedMessage());
		}
	}

	/**
	 * Class contains only static utilities, so ensure that no instances of this
	 * class are created.
	 */
	private MessageUtils() {
	}

	/**
	 * Look up a localized message in the message bundle and return a
	 * MessageFormat for it.
	 * 
	 * @param msgKey
	 *            key that identifies which message to retrieve
	 * 
	 * @return MessageFormat corresponding to the given key
	 */
	public static String getMessageString(String msgKey) {

		assert (msgKey != null);

		try {

			return bundle.getString(msgKey);

		} catch (MissingResourceException mre) {
			throw new CompilerError(mre.getLocalizedMessage());
		} catch (ClassCastException cce) {
			throw new CompilerError(
					"bundle contains non-string object for key '" + msgKey
							+ "'");
		}
	}

	/**
	 * Format a message corresponding to the given key and using the given
	 * arguments.
	 * 
	 * @param msgKey
	 *            key that identifies which message to retrieve
	 * @param args
	 *            arguments to use to complete the message
	 * 
	 * @return localized String corresponding to the given key and arguments
	 */
	public static String format(String msgKey, Object... args) {

		try {

			return MessageFormat.format(getMessageString(msgKey), args);

		} catch (IllegalArgumentException iae) {
			throw new CompilerError(
					"bundle contains invalid message format for key '" + msgKey
							+ "'\n" + "error: " + iae.getMessage());

		}

	}

}