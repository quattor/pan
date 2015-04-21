
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
    "pan", "pan.gz", "xml", "xml.gz", "json", "json.gz", "txt", "dep"
    and "dot". The default is value is "pan,dep".

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

