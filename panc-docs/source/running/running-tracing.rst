.. _global_variable_tracing_contract:

Global-variable tracing
=======================

.. warning::

   Traced values may contain secrets. Treat trace output as sensitive data.
   Snapshots are bounded and may be truncated. A trace is diagnostic output,
   not a lossless dump of the configuration or a guarantee of complete capture.

Scope and selection
-------------------

Tracing concerns global variables: assignments, overwrites, skipped conditional
assignments (``?=``), and reads. Reads include those in DML blocks, function
arguments, function bodies, and expressions assigned to configuration paths.
Local DML variables are excluded, even when their names match the variable glob.
When a local variable shadows a global variable, reading the local variable
must not produce a global-variable read record.

``--trace-object-glob`` selects object template names, not source filenames.
``--trace-variable-glob`` selects global-variable names and defaults to ``*``.
The compiler's Ant task exposes equivalent ``traceObjectGlob`` and
``traceVariableGlob`` attributes, including when invoked from Maven. Both entry
points use shared compiler-option validation and selection semantics.

Globs are case-sensitive and match the entire logical name. ``*`` matches zero
or more characters, including ``/``; ``?`` matches one character. All other
characters are literal, including brackets and backslashes. Empty globs are
invalid. A variable glob alone does not enable tracing.

For example, add these options to an existing compiler invocation::

   --trace-object-glob 'hosts/*' --trace-variable-glob 'NETWORK_*'

On an existing Ant compiler task, use::

   traceObjectGlob="hosts/*" traceVariableGlob="NETWORK_*"

Omit the variable selection to trace all globals in the selected objects.
Quote CLI globs so the shell does not expand them.

For output formats other than DEPX, redirect stderr on the existing command to
capture the stream, for example::

   panc --trace-object-glob 'hosts/*' [existing compiler arguments] 2>trace.log

Replace the bracketed placeholder with your normal compiler arguments.
The file also contains ordinary stderr diagnostics; a parser must select only
lines with the ``PAN_GLOBAL_TRACE`` prefix followed by a space.

Tracing is disabled unless object selection is explicitly enabled. An
unselected object emits no records. Disabled tracing must be bypassed early,
without allocating events, formatting messages, rendering values, or retaining
trace history. Normal runs must preserve compiler optimizations and results.

Compiler-managed trace files
----------------------------

Add ``--trace-file`` to write trace records to ``trace.log`` in the compiler's
output directory instead of stderr. On an Ant ``panc`` task, use
``traceFile="true"``. Object selection is still required; requesting file output
alone does not enable tracing. Ordinary compiler diagnostics remain separate.

The output directory must exist. Each invocation replaces its previous trace
file after acquiring an exclusive lock. A concurrent invocation targeting the
same file fails without truncating it. Requested file-output failures fail the
compilation, unlike the best-effort stderr stream. A failed compilation may
leave a partial trace. The file uses the same prefixed JSON lines as stderr.
An enabled invocation that recompiles no objects produces an empty trace file.

One Ant ``panc`` task shares its file across compilation batches. For a build
with multiple ``panc`` tasks or nested builds, use an outer session to avoid
replacing the file at each task::

   <taskdef resource="org/quattor/ant/panc-ant.xml" loaderref="pan.trace.loader">
     <classpath><pathelement location="/path/to/panc.jar"/></classpath>
   </taskdef>
   <panc-trace-session outputDir="build/xml" referenceId="pan.trace.session">
     <panc outputDir="build/xml" traceObjectGlob="hosts/*"
           traceVariableGlob="NETWORK_*" traceSessionRef="pan.trace.session"
           checkDependencies="false">
       <path><pathelement location="templates"/></path>
       <fileset dir="templates" includes="hosts/*.pan"/>
     </panc>
   </panc-trace-session>

Replace the example paths and source selection with those of your build.
``traceSessionRef`` selects the shared file without needing ``traceFile`` on
each compiler task. The session opens the file once, closes it after its nested
tasks finish or fail, and removes its reference. Its ``referenceId`` must not
already exist. Disabled compiler tracing does not resolve ``traceSessionRef``;
an explicitly invoked session still opens its file, so use the session only in
the debug build path.

At **every** nested ``subant`` link, explicitly forward both references::

   <reference refid="pan.trace.loader"/>
   <reference refid="pan.trace.session"/>

Child task definitions must use the same ``loaderref="pan.trace.loader"``.
Property inheritance alone does not forward references. An incompatible task
classloader or missing session reference causes an error rather than silently
starting a separate log.

Dependency checking may skip up-to-date profiles, which then emit no records.
For a complete debug recompilation, use ``checkDependencies="false"`` instead
of deleting existing build outputs. Record sequence numbers remain local to
each object's build context, including when an object is compiled again in a
later task; the shared log does not provide a global execution sequence.

Record identity and location
----------------------------

Each record must identify:

* ``object``: the logical name of the object template being compiled.
* ``object_file``: the source filename of that object template.
* ``template``: the logical name of the template containing the operation,
  such as ``config/network``, as used in an include, not an absolute file path.
* ``location``: the source line, or source line range, of the operation.
  Line-and-column ranges are preferred when available.
* The action, global-variable name, and applicable values.

A read inside a DML block should use its own operation's source range when
available. A range covering the enclosing DML block is an acceptable fallback.
For a read in a function body, report the template containing that body and the
read's location (or enclosing body range), rather than attributing the read to
the caller's template. The object identity always remains the object currently
being compiled.

With DEPX output, records are captured in the result by default. With other
output formats, records go to stderr by default. An explicitly requested trace
file receives the diagnostic stream in either case. Each streamed record is
a complete, non-interleaved line, including when objects are compiled
concurrently. Embedded newlines in names and values
must be escaped. Values are rendered only for selected events and must describe
the value at the time of the event, not a later mutation.

The examples below use a compact illustrative format, not literal compiler
output. The actual stream uses the versioned JSON format described next.
Consumers should parse fields rather than relying on JSON field order.

Stream format and typed snapshots
---------------------------------

Each trace line starts with ``PAN_GLOBAL_TRACE`` followed by a space
and one JSON object.
For example (shown on one line, with illustrative filenames)::

   PAN_GLOBAL_TRACE {"format":"pan-global-trace","version":1,"sequence":1,"object":"hosts/node01","object_file":"hosts/node01.pan","template":"config/network","template_file":"config/network.pan","location":{"begin_line":2,"begin_column":1,"end_line":2,"end_column":21},"action":"assign","variable":"PORT","conditional":false,"final":false,"old":{"type":"absent"},"new":{"type":"long","value":8080}}

``sequence`` increases within each object's build context. There is no global
execution order across objects. ``location`` is a line-and-column range or
JSON ``null`` when unavailable; filenames can also be JSON ``null`` when no
source file is available, and otherwise reflect the compiler's source paths.

Actions are ``read``, ``assign``, ``overwrite``, and ``skip_default``. Reads have
a typed ``value`` and, for indexed reads, an ``access_path`` array of typed keys
or indices. Successful writes have ``old`` and ``new`` snapshots plus
``conditional`` and ``final`` flags. Skipped defaults have an ``existing``
snapshot instead of ``old`` and ``new``; the RHS is not evaluated.
``assign`` versus ``overwrite`` follows the compiler's binding semantics, not
merely whether an earlier assignment appeared in the source.

Snapshots use a ``type`` tag. ``absent``, ``null``, and ``undef`` have no
``value`` field. Strings, longs, booleans, and finite doubles have a JSON
``value`` of the corresponding type. Non-finite doubles retain ``type=double``
but use a string value such as ``NaN`` or ``Infinity``. Lists have an array of
typed snapshots; dictionaries have an array of entries with typed ``key`` and
``value`` fields. This preserves dictionary keys without relying on JSON
object-key encoding. Unsupported values carry ``type=unsupported`` and a
``class`` name rather than arbitrary object text.

Snapshots limit nesting to 16 levels, value traversal to 1024 nodes, and each
string or dictionary key to 4096 Unicode code points. Truncated snapshots carry
``truncated=true``, a ``reason`` (``depth``, ``nodes``, ``string``, or ``cycle``),
and ``size`` for the affected string or resource. Do not interpret a truncated
snapshot as the complete original value. JSON escaping preserves embedded
newlines, quotes, and backslashes while keeping each record on a single line.
Streaming alone does not retain an event history. Stderr output failures must not
turn a successful compilation into a failure; missing records therefore remain
possible and must not be treated as proof that an operation did not occur.

Acceptance scenario: assignment, overwrite, and skipped default
---------------------------------------------------------------

Object source ``hosts/node01.pan``::

   object template hosts/node01;
   include 'config/network';

Included source ``config/network.pan`` (line numbers start at 1)::

   template config/network;
   variable PORT = 8080;
   variable PORT = PORT + 1;
   variable PORT ?= error('the skipped RHS must not execute');
   '/port' = PORT;

Expected records, in execution order::

   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/network location=2 ASSIGN global=PORT value=8080
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/network location=3 READ global=PORT value=8080
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/network location=3 OVERWRITE global=PORT old=8080 new=8081
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/network location=4 SKIP_DEFAULT global=PORT existing=8081
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/network location=5 READ global=PORT value=8081

The profile contains ``/port = 8081``. The skipped RHS is not evaluated and
produces neither an error nor RHS read records. The internal check deciding
whether to skip is not a user read. Likewise, retrieving the old value for an
overwrite record must not itself emit a read record.

Acceptance scenario: global reads inside DML and function arguments
-------------------------------------------------------------------

Source ``example.pan``::

   object template example;
   variable X = 2 + 3;
   variable Y = {
       local_value = X + 10;
       local_value;
   };
   '/result' = Y;
   '/label' = format('%d', X);

Expected records::

   TRACE object=example object_file=example.pan template=example location=2 ASSIGN global=X value=5
   TRACE object=example object_file=example.pan template=example location=4 READ global=X value=5
   TRACE object=example object_file=example.pan template=example location=3-6 ASSIGN global=Y value=15
   TRACE object=example object_file=example.pan template=example location=7 READ global=Y value=15
   TRACE object=example object_file=example.pan template=example location=8 READ global=X value=5

For the read of ``X`` on line 4, ``location=3-6`` is an acceptable fallback
if only the enclosing block's range is available. No assignment or read
records are emitted for ``local_value``. The output profile contains
``/result = 15`` and ``/label = '5'``.

The compiler may precompute ``2 + 3`` into ``5``. The global assignment to
``X`` must still be traced. Expressions reading object-level globals remain
runtime operations; tracing does not need to disable constant folding to
capture the global reads in this example. Intermediate arithmetic operations
are outside the tracing contract.

Acceptance scenario: distinct value representations
---------------------------------------------------

Trace rendering must preserve value types. Pan ``null`` and ``undef`` must
not be confused with an empty string or strings containing those words.
For example, an included template may contain::

   template config/value_types;
   variable V_NULL = null;
   variable V_UNDEF = undef;
   variable V_EMPTY = '';
   variable V_NULL_TEXT = 'null';
   variable V_UNDEF_TEXT = 'undef';

For a selected object named ``hosts/node01``, the assignment records must
distinguish all five values. One acceptable illustrative representation is::

   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/value_types location=2 ASSIGN global=V_NULL type=null value=null
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/value_types location=3 ASSIGN global=V_UNDEF type=undef value=undef
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/value_types location=4 ASSIGN global=V_EMPTY type=string value=""
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/value_types location=5 ASSIGN global=V_NULL_TEXT type=string value="null"
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=config/value_types location=6 ASSIGN global=V_UNDEF_TEXT type=string value="undef"

The typed JSON encoding preserves this distinction for
reads, assignments, skipped-default existing values, and both old and new
values in overwrite records. String quoting and escaping must be unambiguous.
A string containing a quote or a literal backslash must not lose those
characters; an actual newline and the two characters backslash and ``n``
must have different encodings. The same type distinction applies recursively
inside lists and dictionaries.

An undefined global (no binding exists) is not automatically the same as a
global bound to Pan ``undef`` or ``null``. If absence is represented in a
record, use a separate explicit marker, not the string ``"undef"`` or an
empty value field. Tests must preserve the compiler's existing assignment,
lookup, and ``?=`` semantics for these values rather than changing them to
simplify tracing. In particular, verify whether a ``null`` assignment retains
or removes a binding before asserting a subsequent read or default outcome.

Acceptance scenario: dynamic include and conditional reads
----------------------------------------------------------

Regression tests cover global reads that drive a dynamic include and
conditional execution. The following records are illustrative, not live
compiler output::

   TRACE object=hosts/node01 object_file=hosts/node01.pan template=hosts/node01 location=2 READ global=INCLUDE_TARGET type=string value="config/network"
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=hosts/node01 location=3 READ global=ENABLED type=boolean value=true
   TRACE object=hosts/node01 object_file=hosts/node01.pan template=hosts/node01 location=3 READ global=ENABLED type=boolean value=false

The true and false records represent separate test outcomes. The tests must
verify:

* For ``include(VARIABLE)``, evaluating a selected global include target emits
  a read with the actual template-name string, attributed to the including
  template and the include expression's source location. Subsequent reads and
  assignments in the included template retain the object identity but use the
  included logical template name and their own locations.
* For ``if(VARIABLE)``, the global read reports the boolean value at the
  condition expression's source location. Test both outcomes and execute and
  trace only the chosen branch.
* In a DML block, a global condition read is traced, but a local condition
  variable is not.
* Variable-glob exclusions suppress these reads without affecting include
  resolution or branch selection.
* Skipped branches produce no reads, assignments, or side effects from that
  branch. Internal include and default bookkeeping must not be mislabeled as
  extra global reads.

Direct DEPX capture and variable overview
-----------------------------------------

Select ``depx`` or ``depx.gz`` output together with the trace globs to capture
global-variable events directly in successful dependency results. Selecting
DEPX alone does not enable tracing. No implicit stderr trace stream is produced
when DEPX capture is enabled. Add ``--trace-file`` or the Ant file/session
settings described above if you also need a diagnostic log.

DEPX is written only after successful validation. A failed compilation does not
produce a partial DEPX trace; an explicitly requested diagnostic log can retain
events recorded before the failure. There is no separate stdout destination or
log-to-DEPX merger.

The outer DEPX schema remains version 1. Each profile can contain an optional
``global_trace`` section with its own ``version: 1``, the effective
``object_glob`` and ``variable_glob`` (``*`` when omitted), ``selected``, ``complete``,
``limits``, ``dropped_events``, and ordered ``events``. Configured but excluded
profiles have ``selected: false`` and an empty event list. An absent section
means activity was not captured or is unavailable, not that no variable was used.

Each event retains the stream's action and typed payload and adds ``occurrence``:
the exact zero-based include/create occurrence active when the event happened,
or JSON ``null`` for the profile root. Source template and location identify the
operation's definition, independently of execution context. A function can
therefore report its definition source while referring to a caller's occurrence.
Repeated includes must not be correlated by filename alone. Sequences remain
profile-local; they do not establish an order across profiles.

Capture retains a prefix of at most 10,000 events or 8,388,608 serialized-event
UTF-8 bytes per profile, whichever limit is reached first. Byte accounting
excludes stream prefixes and enclosing DEPX punctuation. Once saturated, capture
omits later matching events, counts them in ``dropped_events``, marks
``complete: false``, and emits one warning for the affected profile. An explicitly
requested diagnostic stream continues independently of this cap.
Completeness concerns only events matching the recorded filters; individual
snapshots can still be truncated. These limits are not a strict heap bound:
parallel profiles and dependency results can retain multiple captures.

Open the resulting file with ``depviewer path/to/profile.depx.gz``. In the
**Variables** section, search for a global variable and select it to see set,
use, and skipped-default counts for retained events. Select an event to inspect
typed values, indexed access paths, source location, and execution context.
**Show occurrence N** navigates to the exact occurrence in the existing include
tree; root events have no occurrence link. Source paths remain text.

The viewer distinguishes unavailable capture, excluded profiles, no matching
activity, and a search with no results. Capture filters and incomplete-capture
warnings remain visible; value-level truncation is shown separately in details.
Old DEPX files remain usable without a variable history. Trace data can contain
secrets even in compressed files; treat it as sensitive configuration data.

Additional acceptance checks
----------------------------

The regression coverage includes the following requirements:

* Distinct representations for null, undef, empty strings, and strings
  containing those words, including type-changing overwrites and nested values.
* A matching object glob and a nonmatching object glob, using template names
  that differ from their physical source filenames.
* The default variable glob selecting all globals, and a narrower glob
  excluding otherwise executed assignments and reads.
* A successful ``?=`` on an undefined global, distinguished from a skipped
  default and from an overwrite of an existing global.
* Indexed global reads: identify the global and the accessed index/key path,
  and report the value actually read, without treating local indexed reads
  as global reads.
* A global read inside a function defined in another included template:
  preserve object identity and attribute the source location to the function
  definition's template, rather than assuming the current executing template
  is always the definition's template.
* Local/global name shadowing and absence of records for compiler bookkeeping.
* Multiple objects compiled concurrently: records are not interleaved and
  selection or object identity cannot leak between build contexts.
* Equivalent CLI and Ant settings, including invalid-setting diagnostics.
* Disabled tracing and nonmatching selections: no value rendering or event
  allocation, with unchanged compiler output and exception behavior.
* Values containing newlines and mutable collections: single-line records
  that reflect the value when read or assigned, without altering evaluation.

Executable tests are under ``panc/src/test``. Compiler-level fixtures cover
assignments and reads, filtering, source attribution, both conditional outcomes,
dynamic includes, shadowing, nested mutation snapshots, and multiple objects
using a worker pool. Structural tests check that disabled and nonmatching
selections avoid tracing and value rendering. Serializer tests cover typed
values, escaping, cycles, and truncation.

The worker-pool test does not force simultaneous object execution. Truncation
is tested at the serializer level, not through a compiled fixture. Namespaced
logical object names and physical paths are tested, but differing object/file
basenames are not. Disabled-path checks are structural rather than a complete
allocation or timing benchmark. These limitations remain distinct from the
requirements above.
