.. _global_variable_tracing_contract:

Global-variable tracing: proposed contract
==========================================

.. warning::

   This page specifies expected behavior for a future tracing facility.
   It does not document an implemented feature. The examples are acceptance
   scenarios, not captured compiler output; option names are proposed.
   Traced values may contain secrets. Treat trace output as sensitive data.

Scope and selection
-------------------

Tracing concerns global variables: assignments, overwrites, skipped conditional
assignments (``?=``), and reads. Reads include those in DML blocks, function
arguments, function bodies, and expressions assigned to configuration paths.
Local DML variables are excluded, even when their names match the variable glob.
When a local variable shadows a global variable, reading the local variable
must not produce a global-variable read record.

The proposed ``--trace-object-glob`` selects object template names, not source
filenames. The proposed ``--trace-variable-glob`` selects global-variable names
and defaults to ``*``. Equivalent settings must be available on the compiler's
Ant task, including when that task is invoked from Maven. Both entry points
must use shared compiler-option validation and selection semantics. Exact Ant
attribute names and glob syntax remain to be finalized.

Tracing is disabled unless object selection is explicitly enabled. An
unselected object emits no records. Disabled tracing must be bypassed early,
without allocating events, formatting messages, rendering values, or retaining
trace history. Normal runs must preserve compiler optimizations and results.

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

Records go to stderr. Each record is a complete, non-interleaved line, including
when objects are compiled concurrently. Embedded newlines in names and values
must be escaped. Values are rendered only for selected events and must describe
the value at the time of the event, not a later mutation.

The examples below use a compact illustrative format. Exact field ordering,
quoting, and value serialization remain to be finalized. Tests should assert
the fields and behavior, not the illustrative spacing or column boundaries.

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

Additional acceptance checks
----------------------------

Future automated tests must also cover:

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
  definition's template. Resolve and verify this attribution during
  implementation rather than assuming the current executing template is
  always the definition's template.
* Local/global name shadowing and absence of records for compiler bookkeeping.
* Multiple objects compiled concurrently: records are not interleaved and
  selection or object identity cannot leak between build contexts.
* Equivalent CLI and Ant settings, including invalid-setting diagnostics.
* Disabled tracing and nonmatching selections: no value rendering or event
  allocation, with unchanged compiler output and exception behavior.
* Values containing newlines and mutable collections: single-line records
  that reflect the value when read or assigned, without altering evaluation.

These scenarios belong in compiler regression tests under ``panc/src/test``
or ``panc/tests`` when the feature is implemented. This page is the readable
acceptance contract until those executable tests exist; it is not evidence
that the scenarios currently pass.
