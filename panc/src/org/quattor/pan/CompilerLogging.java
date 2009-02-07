package org.quattor.pan;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CompilerLogging {

	// Initialize the top-level logger with default configuration. By default,
	// the logging is off and no logging is sent to parent loggers. Any existing
	// handlers of the top-level logger are removed.
	private static final Logger topLogger = Logger.getLogger("org.quattor.pan");
	static {
		topLogger.setLevel(Level.OFF);
		initializeLogger(topLogger);
	}

	// Save the name of the initialized log file to avoid unnecessary
	// re-initialization.
	private static String initializedLogFile = null;

	/**
	 * This enumeration identifies the types of logging that can be turned or
	 * off. Note that the order of the enumeration is important as they will be
	 * activated in the given order. This allows NONE to turn off all logging no
	 * matter what the order of the values and INCLUDE to defer to CALL or ALL
	 * if specified.
	 * 
	 * @author loomis
	 * 
	 */
	public static enum LoggingType {

		TASK {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan.task");
			}
		},

		INCLUDE {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan.call");
			}

			@Override
			public Level logLevel() {
				return Level.INFO;
			}
		},

		CALL {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan.call");
			}
		},

		MEMORY {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan.memory");
			}
		},

		ALL {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan");
			}

			@Override
			public void activate() {
				logger().setLevel(logLevel());
				TASK.logger().setLevel(logLevel());
				CALL.logger().setLevel(logLevel());
				MEMORY.logger().setLevel(logLevel());
			}
		},

		NONE {
			@Override
			public Logger logger() {
				return Logger.getLogger("org.quattor.pan");
			}

			@Override
			Level logLevel() {
				return Level.OFF;
			}

			@Override
			public void activate() {
				logger().setLevel(logLevel());
				TASK.logger().setLevel(logLevel());
				CALL.logger().setLevel(logLevel());
				MEMORY.logger().setLevel(logLevel());
			}
		};

		abstract public Logger logger();

		Level logLevel() {
			return Level.ALL;
		}

		public void activate() {
			logger().setLevel(logLevel());
		}
	}

	/**
	 * Class consists only of static methods and static values. Ensure that no
	 * instances of this class are created.
	 */
	private CompilerLogging() {
	}

	/**
	 * Enable the given types of logging. Note that NONE will take precedence
	 * over active logging flags and turn all logging off. Illegal logging
	 * values will be silently ignored.
	 * 
	 * @param loggerList
	 *            a comma-separated list of logging types to enable
	 */
	public static void activateLoggers(String loggerList) {

		// Create an empty set. This will contain the list of all of the loggers
		// to activate. (Note that NONE may be a logger; in this case, all
		// logging will be deactivated.)
		EnumSet<LoggingType> flags = EnumSet.noneOf(LoggingType.class);

		// Split on commas and remove white space.
		for (String name : loggerList.split("\\s*,\\s*")) {
			try {
				flags.add(LoggingType.valueOf(name.trim().toUpperCase()));
			} catch (IllegalArgumentException consumed) {
			}
		}

		// Loop over the flags, enabling each one.
		for (LoggingType type : flags) {
			type.activate();
		}

	}

	/**
	 * Remove all handlers associated with the given logger.
	 */
	private static void initializeLogger(Logger logger) {

		// Do NOT send any logging information to parent loggers.
		topLogger.setUseParentHandlers(false);

		// Remove any existing handlers.
		for (Handler handler : topLogger.getHandlers()) {
			try {
				topLogger.removeHandler(handler);
			} catch (SecurityException consumed) {
				System.err
						.println("WARNING: missing 'LoggingPermission(\"control\")' permission");
			}
		}

	}

	/**
	 * Define the file that will contain the logging information. If this is
	 * called with null, then the log file will be removed. The log file and
	 * logging parameters are global to the JVM. Interference is possible
	 * between multiple threads.
	 * 
	 * @param logfile
	 */
	public synchronized static void setLogFile(File logfile) {

		// Add a file logger that will use the compiler's customized
		// formatter. This formatter provides a terse representation of the
		// logging information.
		try {
			if (logfile != null) {
				String absolutePath = logfile.getAbsolutePath();
				if (initializedLogFile == null
						|| (initializedLogFile != null && !initializedLogFile
								.equals(absolutePath))) {

					// Remove any existing handlers.
					initializeLogger(topLogger);

					// Set the new handler.
					FileHandler handler = new FileHandler(absolutePath);
					handler.setFormatter(new LogFormatter());
					topLogger.addHandler(handler);

					// Make sure we save the name of the log file to avoid
					// inappropriate reinitialization.
					initializedLogFile = absolutePath;
				}
			}
		} catch (IOException consumed) {
			StringBuilder sb = new StringBuilder();
			sb.append("WARNING: unable to open logging file handler\n");
			if (logfile != null) {
				sb.append("WARNING: logfile = '" + logfile.getAbsolutePath()
						+ "'");
			}
			sb.append("\nWARNING: message = ");
			sb.append(consumed.getMessage());
			System.err.println(sb.toString());
		}

	}

	/**
	 * Provides a terse representation of the logging information from the
	 * compiler. The output is intended to be extremely easy to parse for later
	 * analysis. Each log message consists of a single line with space-separated
	 * values. The values are: time in milliseconds, thread ID, log message, and
	 * any message parameters.
	 * 
	 * @author loomis
	 * 
	 */
	private static class LogFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {

			StringBuilder sb = new StringBuilder();

			sb.append(record.getMillis());
			sb.append(" ");
			sb.append(record.getThreadID());
			sb.append(" ");
			sb.append(record.getMessage());
			for (Object o : record.getParameters()) {
				if (o != null) {
					sb.append(" ");
					sb.append(o.toString());
				}
			}
			sb.append("\n");

			return sb.toString();
		}

	}

}
