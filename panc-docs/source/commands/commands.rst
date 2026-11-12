
panc
====

Name
----

panc -- compile pan language templates

Synopsis
--------

panc
[``--no-debug`` \| ``--debug``]
[``--debug-ns-include regex``]
[``--debug-ns-exclude regex``]
[``--initial-data dict-dml``]
[``--include-path path``]
[``--output-dir dir``]
[``--formats formats``]
[``--java-opts java-options``]
[``--max-iteration limit``]
[``--max-recursion limit``]
[``--nthread number``]
[``--no-disable-escaping`` \| ``--disable-escaping``]
[``--logging string``]
[``--log-file file``]
[``--warnings flag``]
[``-v`` \| ``--no-verbose`` \| ``--verbose``]
[``-h`` \| ``--no-help`` \| ``--help``]
[``--version``]
[template ...]

Description
-----------

The ``panc`` command will compile a collection of pan language templates
into a set of machine configuration files. This command, with its
reorganized and simplified options, replaces the older ``panc`` command.

``--no-debug, --debug``
    Enable or disable all debugging. By default, debugging is turned
    off.

``--debug-ns-include=``
    Define a pattern to selectively enable the pan ``debug`` and
    ``traceback`` functions. Those functions will be enabled for
    templates where the template name matches one of the include regular
    expressions *and* does not match an exclude regular expression. This
    option may appear multiple times.

``--debug-ns-exclude=``
    Define a pattern to selectively disable the pan ``debug`` and
    ``traceback`` functions. Those functions will be disabled for
    templates where the template name matches one of the exclude regular
    expressions. This option may appear multiple times. Exclusion takes
    precedence over inclusion.

``--initial-data=``
    A DML expression that evaluates to an dict. This value will be used
    as the starting dict for all object templates. This is a convenient
    mechanism for injecting build numbers and other metadata in the
    profiles.

``--include-path=``
    Defines the source directories to search when looking for templates.
    The value must be a list of absolute directories delimited by the
    platform's path separator. If this is not specified, the current
    working directory is used.

``--output-dir=``
    Set where the machine configuration files will be written. If this
    option is not specified, then the current working directory is used
    by default.

``--formats=``
    A comma separated list of desired output formats. Allowed values are
    "pan", "pan.gz", "xml", "xml.gz", "json", "json.gz", "txt", "dep", "dep.gz",
    "depx", "depx.gz" and "dot".
    The default value is "pan,dep". See
    :ref:`depx-output` for the extended dependency trace.

``--java-opts=``
    List of options to use when starting the java virtual machine. These
    are passed directly to the ``java`` command and must be valid.
    Multiple options can be specified by separating them with a space.
    When using multiple options, the full value must be enclosed in
    quotes.

``--max-iteration=``
    Set the limit on the maximum number of permitted loop iterations.
    This is used to avoid infinite loops. The default value is 5000.

``--max-recursion=``
    Set the limit on the maximum number of permitted recursions. The
    default value is 10.

``--nthread=``
    The number of threads to use for profile processing. The default
    value of zero will use a number equal to the number of CPU cores on
    the machine.

``--no-disable-escaping, --disable-escaping``
    Enable or disable the escaping of path elements. The default value
    is to enable the escaping of path elements.

``--logging=``
    Enable compiler logging; possible values are "all", "none",
    "include", "call", "task", and "memory". A log file must be
    specified with the ``--log-file`` option to capture the logging
    information.

``--log-file=``
    Set the name of the file to use to store logging information.

``--warnings=``
    Possible values are "on", "off", and "fatal". The last value will
    turn all warnings into fatal errors.

``-v, --no-verbose, --verbose``
    At the end of a compilation, print run statistics including the
    numbers of files processed, total time, and memory used. The default
    is not to print these values.

``-h, --no-help, --help``
    Print a short summary of command usage if requested. No other
    processing is done if this option is given.

``--version``
    Prints pan compiler version.

The ``panc`` command is just a wrapper script around the ``java``
command to simplify setting various options. The typical case is that
the command is invoked without options and just a list of object
templates as the arguments. Larger sets of templates will need to set
the memory option for the Java Virtual Machine; this should be done
through the ``--java-opts`` option.


.. _depx-output:

Extended dependency traces (depx)
=================================

Select ``depx`` to write a ``.depx`` file containing the runtime include
trace and the complete file dependency set. For example::

    panc --formats pan,depx profiles/example.pan

Select ``depx.gz`` for a gzip-compressed ``.depx.gz`` file with the same
UTF-8 content, streamed directly without an intermediate uncompressed file::

    panc --formats pan,depx.gz profiles/example.pan

The existing ``dep`` output and default formats are unchanged.
Collection is enabled only when a selected
formatter requests it. Within each profile, metadata shares existing template
and source-location references, while occurrence indexes, parents and statuses
use primitive arrays. Storage grows with encountered occurrences, not merely
with the number of distinct templates.

Only successfully resolved, type-valid named includes encountered during
evaluation are recorded, including computed names and validation-time execution.
Untaken branches and includes evaluating to ``null`` or ``undef`` produce no
occurrence. Repeated ordinary includes retain their own occurrences and children.
Later references to already loaded unique or declaration templates are marked
``skipped_unique`` or ``skipped_declaration`` and have no synthetic descendants.
An include inside a skipped template is not encountered again.

Structure template invocations are labeled ``create``, not ``include``. The
caller identifies the executing template, including a function's defining
template when applicable; this is not a general function-call trace. Reachable
external object profiles have separate sections, not fabricated include edges.
The final dependency section preserves the same complete file set as ``dep``,
including dependencies that are not represented by include occurrences.

Version 1 JSON format
----------------------

Both compiler formats contain identical UTF-8 JSON after decompression. The
top-level object contains
``format: "depx"``, ``version: 1``, an ordered ``roots`` array of absolute
include directories, a ``profiles`` array sorted by name, and a separate
``dependencies`` array. For example::

    {
      "format": "depx", "version": 1, "roots": ["/templates"],
      "profiles": [{
        "name": "example",
        "sites": [{
          "caller": {"name": "example", "source": {
            "name": "example", "type": "PAN",
            "location": {"root": 0, "path": ""}}},
          "target": {"name": "common", "source": {
            "name": "common", "type": "PAN",
            "location": {"root": 0, "path": ""}}},
          "location": {"begin_line": 2, "begin_column": 1,
                       "end_line": 2, "end_column": 17}
        }],
        "occurrences": [{"site": 0, "parent": null, "status": "executed"}]
      }],
      "dependencies": [
        {"name": "common", "type": "PAN", "location": {"root": 0, "path": ""}},
        {"name": "example", "type": "PAN", "location": {"root": 0, "path": ""}}
      ]
    }

Site and occurrence identifiers are their array indexes, local to each profile.
Each occurrence references a shared ``site`` and has a ``parent`` that is either
``null`` or an earlier occurrence index. The flat array preserves encounter
order and nesting without deeply nested JSON. Status is ``executed``,
``skipped_unique``, ``skipped_declaration``, or ``create``. Suppressed occurrences
have no children. Caller, target, source, or statement location can be ``null``
when the corresponding metadata is absent.

A source has separate ``name``, ``type``, and ``location`` fields. The location
is still a load directory, not an inferred source filename. In-root locations
use ``{"root": INDEX, "path": RELATIVE}``, where ``RELATIVE`` is a literal
slash-separated filesystem path; an empty path denotes the root itself. JSON
relative paths are not URI-escaped. Percent signs,
spaces and Unicode characters retain their literal meaning. The first matching
include root wins, observing directory boundaries. Other locations use
``{"uri": ABSOLUTE_FILE_URI}``; absent locations use ``null``. Dependencies
include files that do not correspond to an include occurrence.

Quick text inspection
---------------------

Use the dependency-free ``depx2text.py`` converter to inspect a trace as text.
It requires Python 3.7 or newer and uses only the Python standard library. From
a checkout it is ``panc/src/main/scripts/depx2text.py``; compiler bundles install
it as ``bin/depx2text.py``. For example::

    depx2text.py profile.depx.gz
    depx2text.py profile.depx.gz -o profile.txt
    depx2text.py -o profile.txt.gz < profile.depx

The optional input defaults to standard input; ``-`` also selects standard
input. Output defaults to standard output; ``-o PATH`` or ``--output PATH``
selects a file, and ``-o -`` selects standard output. Input and output filenames
ending in ``.gz`` are decompressed and compressed respectively. Standard input
and output are uncompressed. The converter validates the JSON before opening an
output file and reports invalid input without a traceback.

The resulting UTF-8 text uses LF line endings and starts with ``depx 2``.
Profile and dependency ordering follows the input; site and occurrence indexes
are local to each profile and assigned in encounter order. For example::

    depx 2
    root 0 "/templates"
    profile "example"
      site 0 caller="example" target="common" caller-path="example PAN //" target-path="common PAN //" location=2:1-2:17
      occurrences
        include 0 site=0 caller="example" target="common" location=2:1-2:17 status=executed
    dependencies
      file "common PAN //"
      file "example PAN //"

The ordered ``root`` table records the absolute include directories. With one
directory, ``//`` denotes that root; with several, ``//0//``, ``//1//`` and so
on denote directories in configured include-search order. Locations beneath a
root carry a URI-escaped relative suffix, for example
``//site/configuration/`` or ``//1//site/configuration/``. Decode the suffix
once and join it to the recorded root to resolve the directory. Locations
outside every root remain absolute file URIs.

Source values use ``NAME TYPE LOCATION`` descriptors, where the location is the
load directory, not the full filename. For example, ``common PAN //`` identifies
the Pan source named ``common`` in ``/templates``. A source without a location
uses ``NAME (TYPE)``; absent metadata is the bare token ``null``.

Each ``site`` record contains caller and target names, source descriptors and
statement location in ``beginLine:beginColumn-endLine:endColumn`` form.
Descriptors occur in site metadata rather than on every occurrence line. After
``occurrences``, each record begins with ``include`` or ``create``, its
occurrence index and ``site=<index>``. Caller, target, location and status follow.
Root occurrences have four leading spaces; each enclosing occurrence adds two
spaces. Status is ``executed``, ``skipped-unique``, ``skipped-declaration`` or
``create``. Indentation expresses occurrence nesting, not a deduplicated graph.

The ``dependencies`` section follows all profiles, with two-space-indented
``file`` records. Names and paths are double-quoted strings. Backslash, double
quote, newline, carriage return and tab are escaped as ``\\\\``, ``\\\"``,
``\\n``, ``\\r`` and ``\\t`` respectively; other ISO control characters use
lowercase four-digit ``\\uXXXX`` escapes. Other characters are preserved. A
quoted ``"null"`` is a string, distinct from missing metadata.


panc-annotations
================

Name
----

panc-annotations -- process annotations in pan configuration files

Synopsis
--------

panc-annotations
[``--base-dir base-directory``]
[``--output-dir dir``]
[``--java-opts jvm-options``]
[``-v`` \| ``--no-verbose`` \| ``--verbose``]
[``-h`` \| ``--no-help`` \| ``--help``]
[``--version``]
[template ...]

Description
-----------

The ``panc-annotations`` command will process the annotations contains
within pan configuration files within the given base directory.

``--base-dir=``
    Defines a base directory containing all pan configuration files to
    process. The default is value is the current working directory.

``--output-dir=``
    Set where the annotation files will be written. If this option is
    not specified, then the current working directory is used by
    default.

``--java-opts=``
    List of options to use when starting the java virtual machine. These
    are passed directly to the ``java`` command and must be valid.
    Multiple options can be specified by separating them with a space.
    When using multiple options, the full value must be enclosed in
    quotes.

``-v, --no-verbose, --verbose``
    At the end of a compilation, print run statistics including the
    numbers of files processed, total time, and memory used. The default
    is not to print these values.

``-h, --no-help, --help``
    Print a short summary of command usage if requested. No other
    processing is done if this option is given.

``--version``
    Prints pan compiler version.

The ``panc-annotations`` command is just a wrapper script around the
``java`` command to simplify setting various options.

panc-build-stats.pl
===================

Name
----

panc-build-stats.pl -- create a report of panc build statistics

Synopsis
--------

panc-build-stats.pl [``--help``] {logfile}

Description
-----------

The ``panc-build-stats.pl`` script will analyze a panc log file and
report build statistics. The script takes the name of the log file as
its only argument. If no argument is given or the ``--help`` option is
used, a short usage message is printed. *The log file must have been
created with "task" logging enabled.*

The script will extract the time required to execute, to set default
values, to validate the configuration, to write the XML file, and to
write a dependency file. It will also report the "build" time which is
the time for executing, setting defaults, and validating an object file.

The analysis is written to the standard output, but may be saved in a
file using standard IO stream redirection. The format of the file is
appropriate for the R statistical analysis package, but should be
trivial to import into excel or any other analysis package.

Example
-------

If the output from the command is written to the file ``build.txt``,
then the following R script will do a simple analysis of the results.
This will provide statistical results on the various build phases and
show histograms of the distributions.

.. code-block:: r

    # R-script for simple analysis of build report
    bstats <- read.table("build.txt")
    attach(bstats)
    summary(bstats)
    hist(build, nclass=20)
    hist(execute, nclass=20)
    hist(execute, nclass=20)
    hist(defaults, nclass=20)
    hist(validation, nclass=20)
    hist(xml, nclass=20)
    hist(dep, nclass=20)
    detach(bstats)

panc-call-tree.pl
=================

Name
----

panc-call-tree.pl -- create a graph of pan call tree

Synopsis
--------

panc-call-tree.pl [``--help``] [``--format=dot\|hg``] {logfile}

Description
-----------

The ``panc-call-tree.pl`` script will analyze a panc log file and create
a graph of the pan call tree. One output file will be created for each
object template. The script takes the name of the log file as its only
argument. If no argument is given or the ``--help`` option is used, a
short usage message is printed. *The log file must have been created
with "call" logging enabled.*

The graphs are written in either "dot" or "hypergraph" format.
`Graphviz <http://www.graphviz.org/>`__ can be used to visualize graphs
written in dot format.
`Hypergraph <http://hypergraph.sourceforge.net/>`__ can be used to
visualize graphs written in hypergraph format. Note that all "includes"
are shown in the graph; in particular unique and declaration templates
will appear in the graph wherever they are referenced.

panc-compile-stats.pl
=====================

Name
----

panc-compile-stats.pl -- create a report of panc compilation statistics

Synopsis
--------

panc-compile-stats.pl [``--help``] {logfile}

Description
-----------

The ``panc-compile-stats.pl`` script will analyze a panc log file and
report compilation statistics. The script takes the name of the log file
as its only argument. If no argument is given or the ``--help`` option
is used, a short usage message is printed. *The log file must have been
created with "task" logging enabled.*

The script will extract the start time of each compilation and its
duration. This compilation is the time to parse a template file and
create the internal representation of the template. The analysis is
written to the standard output, but may be saved in a file using
standard IO stream redirection. The format of the file is appropriate
for the R statistical analysis package, but should be trivial to import
into excel or any other analysis package.

Example
-------

If the output from the command is written to the file ``compile.txt``,
then the following R script will create a "high-density" plot of the
information. This graph shows a vertical line for each compilation,
where the horizontal location is related to the start time and the
height of the line the duration.

.. code-block:: r

    # R-script for simple analysis of compile report
    cstats <- read.table("compile.txt")
    attach(cstats)
    plot(start/1000, duration, type="h", xlab="time (s)", ylab="duration (ms)")
    detach(cstats)

panc-memory.pl
==============

Name
----

panc-memory.pl -- create a report of panc memory utilization

Synopsis
--------

panc-memory.pl [``--help``] {logfile}

Description
-----------

The ``panc-memory.pl`` script will analyze a panc log file and report on
the memory usage. The script takes the name of the log file as its only
argument. If no argument is given or the ``--help`` option is used, a
short usage message is printed. *The log file must have been created
with "memory" logging enabled.*

The script will extract the heap memory usage of the compiler as a
function of time. The memory use is reported in megabytes and the times
are in milliseconds. Usually one will want to use this information in
conjunction with the thread information to understand the memory use as
it relates to general compiler activity. Note that java uses
sophisticated memory management and garbage collection techniques;
fluctuations in memory usage may not be directly related to the compiler
activity at any instant in time.

Example
-------

If the output from the command is written to the file ``memory.txt``,
then the following R script will create a plot of the memory utilization
as a function of time.

.. code-block:: r

    # R-script for simple analysis of memory report
    mstats <- read.table("memory.txt")
    attach(mstats)
    plot(time/1000, memory, xlab="time (s)", ylab="memory (MB)", type="l")
    detach(mstats)

panc-profiling.pl
=================

Name
----

panc-profiling.pl -- generate profiling information from panc log file

Synopsis
--------

panc-profiling.pl [``--help``] [``--usefunctions``] {logfile}

Description
-----------

The ``panc-profiling.pl`` script will analyze a panc log file and report
profiling information. The script takes the name of the log file as its
first argument. The second argument determines if function call
information will be included (flag=1) or not (flag=0). By default, the
function call information is not included. If no argument is given or
the ``--help`` option is used, a short usage message is printed. *The
log file must have been created with "call" logging enabled.*

Two files are created for each object template: one with 'top-down'
profile information and the other with 'bottom-up' information.

The top-down file contains a text representation of the call tree with
each entry giving the total time spent in that template and any
templates called from that template. At each level, one can use this to
understand the relative time spent in a node and each direct descendant.

The bottom-up file provides how much time is spent directly in each
template (or function), ignoring any time spent in templates called from
it. This allows one to see how much time is spent in each template
regardless of how the template (or function) was called.

All of the timing information is the "wall-clock" time, so other
activity on the machine and the logging itself can influence the output.
Nonetheless, the profiling information should be adequate to understand
inefficient parts of a particular build.

panc-threads.pl
===============

Name
----

panc-threads.pl -- create a report of thread activity

Synopsis
--------

panc-threads.pl [``--help``] {logfile}

Description
-----------

The ``panc-threads.pl`` script will analyze a panc log file and report
on build activity per thread. The script takes the name of the log file
as its only argument. If no argument is given or the ``--help`` option
is used, a short usage message is printed. *The log file must have been
created with "task" logging enabled.*

The script will give the start time of build activity on any particular
thread and the ending time. This can be used to understand the build and
thread activity in a particular compilation. The times are given in
milliseconds relative to the first entry in the log file.

Example
-------

If the output from the command is written to the file ``thread.txt``,
then the following R script will create a plot showing the duration of
the activity on each thread.

.. code-block:: r

    # R-script for simple analysis of thread report
    tstats <- read.table("threads.txt")
    attach(tstats)
    plot(stop/1000,thread, type="n", xlab="time (s)", ylab="thread ID")
    segments(start/1000, thread, stop/1000, thread)
    detach(tstats)
