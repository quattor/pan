
License
=======

Licensed under the Apache License, Version 2.0 (the "License"). You may
obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0. Unless required by
applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for
the specific language governing permissions and limitations under the
License.

Support
=======

The v10 series is in active development; the v9 series is frozen.  Both of
these series are supported.

**The old v8 series of releases is no longer supported.**  Migration from v8
to a more recent release is very strongly recommended.

Upcoming Changes
================

The following upcoming and potentially breaking changes are scheduled for
the next major pan compiler release (v11):

- Support for the file extension '.tpl' is deprecated and will be removed
  in the v11 series of the compiler.  Rename your pan source files from
  '*.tpl' to '*.pan'.

- The path '/panc' will be reserved by the compiler.  Values under this path
  will be used to transmit information about the pan compiler version and
  compilation flags to downstream tools.  Avoid putting configuration
  information in the '/panc' part of the configuration tree.

Change Log
==========

Version 10.4
------------

-  (GitHub Issue #118) Built-in join function.

-  (GitHub Issue #121) Implement choice type.

-  (GitHub Issue #122) Formatting.

-  (GitHub Issue #123) Implement built-in validate function to check type.

-  (GitHub Issue #124) Working initial-data command line option.

-  (GitHub Issue #125) Standalone pan tests.

-  (GitHub Issue #126) Support booleans with (in)equality operator.


Version 10.3
------------

-  (GitHub Issue #105) Resource protection should be deep, not shallow.

-  (GitHub Issue #107) Fix error message for undefined values.


Version 10.2
------------

-  (GitHub Issue #80) enable automated build of documentation on the
   http://quattor-pan.readthedocs.org/.

-  (GitHub Issue #79, #81) move documentation sources from DocBook to
   restructured text.

-  (GitHub Issue #76) remove 'object' template reference in output stats

-  (GitHub Issue #71, #72, #73, #74, #75) add options for displaying the
   pan compiler version and update documentation

-  (GitHub Issue #70) create a null formatter mainly for performance testing


Version 10.1
------------

-  (GitHub Issue #68) revert a couple of UTF-8 read/write changes to
   conserve backward compatibility

-  (GitHub Issue #67) update source and bytecode to java 1.6

-  (GitHub Issue #13) change nlist references to dict

-  (GitHub Issue #48) allow variable substitution for bind/valid paths

-  (GitHub Issue #34) add the file\_exists function

-  (GitHub Issue #63) allow user to specify number of threads for
   processing (nthread option)

-  (GitHub Issue #61) fix processing of include path CLI argument

-  (GitHub Issue #59) add substitute function to replace named values in
   string template

-  (GitHub Issue #54) convert source files to UTF-8

-  (GitHub Issue #49) add warning in docs that all pan source files must
   be UTF-8 (also for file\_contents function)

-  (GitHub Issue #47) fix compiler hang when using escape sequence in
   path literal

-  (GitHub Issue #43) fix compiler crash when SELF is used as a function

-  (GitHub Issue #41) RPM package should not own /usr/bin and /usr/lib

-  (GitHub Issue #40) ensure line number and file name are correct for
   traceback function

-  (GitHub Issue #38) add ip address and netmask functions

-  (GitHub Issue #37) ensure line numbers appear in error message for
   bad default values

-  (GitHub Issue #36) allow to\_long to treat values like "08" and "09"

-  (GitHub Issue #31) fix options processing for CLI (bad processing
   causes failure)

-  (GitHub Issue #29) update links in documentation to GitHub from
   SourceForge

-  (GitHub Issue #15, #24) add OBJECT to debug and error output

-  (GitHub Issue #31) panc command line fails

Version 10.0
------------

-  (GitHub Issue #27) Remove session directory functionality

-  (GitHub Issue #5) Remove deprecated options from panc ant task

-  (GitHub Issue #4) Remove panc-old script

-  (GitHub Issue #2) Remove deprecation level attribute in favor of
   warnings attribute in pan-syntax-check mojo

-  (GitHub Issue #26) Restore backward compatibility for gzip output
   flag

Version 9.3
-----------

No additional changes besides those in RC1 and RC2.

Version 9.3-RC2
---------------

-  (SF Bug #3585672) Permit both lower and upper case strings for
   warnings flag in ant and maven tasks.

-  (SF Bug #3585346) Misleading deprecation message for debug element

Version 9.3-RC1
---------------

-  (SF Bug #3582159) Uncaught exception when creating XML transformation

-  (SF RFE #3581805) Remove support for XMLDB format.

-  (SF RFE #3581801) Change dependency file extension from \*.xml.dep to
   \*.dep.

-  (SF RFE #3535682) Allow multiple output formats to be generated from
   CLI.

-  (SF Bug #3535413) Check timestamps of all requested output file
   formats.

-  (SF Bug #3529737) Non-object templates can be accessed via value().

-  (SF Bug #3579769) Tests failed because of change in TreeSet contract
   in Java 1.7.

-  (SF Bug #3579770) Shell scripts use bash syntax. Explicitly use bash
   in she-bang lines.

-  (SF Bug #3581163) Invalid replacement string in replace() raises
   uncaught exception.

-  (SF RFE #3489988) Allow negative values in range expressions.

-  The include syntax without required braces is now allowed.

-  The ``panc`` command no longer includes the possibility to process
   annotations. This functionality is now in a separate command
   ``panc-annotations``.

-  The ``panc`` command now uses a streamlined set of options that are
   not compatible with the previous one. The previous one can be invoked
   with the ``panc-old`` command.

Version 9.2
-----------

-  (SF RFE #3489506) Provide a pan maven archetype. A rudimentary
   implementation is available which uses the panc maven plugin.

-  (SF RFE #3489504) Provide a maven build mojo. A rudimentary
   implementation is available in the panc maven plugin.

-  (SF RFE #3489048) Switch unit tests to use the pan XML format instead
   of xmldb.

-  (SF RFE #3489084) Remove support for panx extension. This has been
   removed as an XML input format is no longer in the roadmap.

-  (SF RFE #3477756) Provide JSON output option. Initial JSON formatter
   is available; detailed serialization may change based on feedback.
   The pan compiler now includes the GSON library (Apache 2 license) to
   handle the JSON serialization.

-  (SF RFE #3477753) Deprecate xmldb format. Use the standard pan XML
   format instead of the xmldb format.

-  (SF Bug #3488948) Annotation information in pan book is inaccurate.
   The description has been correct and expanded somewhat.

Version 9.1
-----------

-  (SF Bug #3485801) pan does not build on Windows; full build and unit
   tests now run correctly on windows

-  (SF Bug #3485492) ``file_contents`` does not work correctly on
   Windows; problems with file name handling have been resolved

-  (SF Bug #3483938) Fix the README file to contain information on
   changes up through the production 9.0 release.

Version 9.0
-----------

Production release contains the same features as RC3. All version
numbers will be considered production releases unless marked explicitly
as alpha, beta, or release candidates.

Version 9.0.0-RC3
-----------------

-  (SF RFE #3422390) The root element used as the starting point for all
   machine profiles can be specified from the command line and ant task.
   This allows the injection of data into all of the profiles without
   having to include explicitly a template in all machine profiles. This
   will be useful for injecting build metadata into the profiles. Note
   that the injected data must still follow the global schema (if
   defined), otherwise builds will fail with validation errors.

Version 9.0.0-RC2
-----------------

The documentation has been significantly reorganized with all of the
documentation apart from this README combined into a single "pan book".

Version 9.0.0-RC1
-----------------

This release contains the following changes:

-  (SF Bug #3171788) Improve error message for format() function when
   there is a mismatch between given format string and arguments.

-  (SF RFE #3386906) Support for \\b (backspace) and \\f (form feed)
   escape sequences in double-quoted strings.

-  (SF Bug #3186921) Dependency calculation in ant task does not work
   correctly for namespaced object templates.


