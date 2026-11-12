"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const { spawnSync } = require("node:child_process");
const test = require("node:test");
const zlib = require("node:zlib");

const script = path.join(__dirname, "..", "..", "depx2text.py");
const run = (args = [], input) => spawnSync("python3", [script, ...args], { input, encoding: "utf8" });
const fixture = () => ({
  format: "depx", version: 1, roots: ["/root", "/other"],
  profiles: [{ name: 'nested/é"\n', sites: [
    { caller: null, target: { name: 'root', source: { name: 'root', type: 'PAN', location: { root: 0, path: 'a%?# 雪' } } }, location: null },
    { caller: { name: 'root', source: null }, target: { name: 'leaf', source: { name: 'leaf', type: 'PAN', location: null } }, location: { begin_line: 2, begin_column: 3, end_line: 4, end_column: 5 } }
  ], occurrences: [
    { site: 0, parent: null, status: 'executed' },
    { site: 1, parent: 0, status: 'skipped_unique' },
    { site: 1, parent: 0, status: 'skipped_declaration' },
    { site: 1, parent: 0, status: 'create' }
  ] }], dependencies: [
    { name: 'payload', type: 'TEXT', location: { root: 1, path: 'a%?# 雪' } },
    { name: 'missing', type: 'PAN', location: null },
    { name: 'external', type: 'TEXT', location: { uri: 'file:/external/a%25' } }
  ]
});

test("renders escaped names, sources, nested occurrences and every status from stdin", () => {
  const result = run([], JSON.stringify(fixture()));
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stdout, 'depx 2\nroot 0 "/root"\nroot 1 "/other"\nprofile "nested/é\\"\\n"\n' +
    '  site 0 caller=null target="root" caller-path=null target-path="root PAN //0//a%25%3F%23%20%E9%9B%AA" location=null\n' +
    '  site 1 caller="root" target="leaf" caller-path=null target-path="leaf (PAN)" location=2:3-4:5\n' +
    '  occurrences\n    include 0 site=0 caller=null target="root" location=null status=executed\n' +
    '      include 1 site=1 caller="root" target="leaf" location=2:3-4:5 status=skipped-unique\n' +
    '      include 2 site=1 caller="root" target="leaf" location=2:3-4:5 status=skipped-declaration\n' +
    '      create 3 site=1 caller="root" target="leaf" location=2:3-4:5 status=create\n' +
    'dependencies\n  file "payload TEXT //1//a%25%3F%23%20%E9%9B%AA"\n  file "missing (PAN)"\n  file "external TEXT file:/external/a%25"\n');
});

test("distinguishes literal backslash sequences from controls in roots and source names", () => {
  const document = {
    format: "depx", version: 1, roots: ['C:\\base\\folder\b\f'],
    profiles: [{ name: 'profile\\b\\f\b\f', sites: [], occurrences: [] }],
    dependencies: [{ name: 'source\\b\\f\b\f', type: 'PAN', location: null }]
  };
  const result = run([], JSON.stringify(document));
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stdout, [
    'depx 2',
    String.raw`root 0 "C:\\base\\folder\u0008\u000c"`,
    String.raw`profile "profile\\b\\f\u0008\u000c"`,
    '  occurrences',
    'dependencies',
    String.raw`  file "source\\b\\f\u0008\u000c (PAN)"`,
    ''
  ].join('\n'));
});

test("reads and writes plain and gzip files and supports in-place conversion", (t) => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'depx2text-'));
  t.after(() => fs.rmSync(dir, { recursive: true, force: true }));
  const plain = path.join(dir, 'trace.depx');
  const compressed = `${plain}.gz`;
  const output = path.join(dir, 'output.gz');
  const json = JSON.stringify(fixture());
  fs.writeFileSync(plain, json);
  fs.writeFileSync(compressed, zlib.gzipSync(json));
  const expected = run(['-'], json).stdout;
  assert.equal(run([compressed, '-o', output]).status, 0);
  assert.equal(zlib.gunzipSync(fs.readFileSync(output)).toString(), expected);
  assert.equal(run([plain, '-o', plain]).status, 0);
  assert.equal(fs.readFileSync(plain, 'utf8'), expected);
  assert.equal(run([compressed, '-o', compressed]).status, 0);
  assert.equal(zlib.gunzipSync(fs.readFileSync(compressed)).toString(), expected);
});

test("rejects malformed schema, references and gzip without touching output or input", (t) => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'depx2text-'));
  t.after(() => fs.rmSync(dir, { recursive: true, force: true }));
  const input = path.join(dir, 'invalid.depx');
  const output = path.join(dir, 'output');
  const cases = [
    '{', JSON.stringify({ ...fixture(), version: 2 }),
    ...[v => { v.profiles[0].occurrences[0].site = 9; }, v => { v.profiles[0].occurrences[1].parent = 3; },
      v => { v.dependencies[0].location.root = 3; }, v => { v.dependencies[0].location.path = '../escape'; },
      v => { delete v.profiles[0].sites[0].target.source.type; }].map(change => { const v = fixture(); change(v); return JSON.stringify(v); })
  ];
  for (const invalid of cases) {
    fs.writeFileSync(input, invalid); fs.writeFileSync(output, 'preserved');
    for (const destination of [output, input, path.join(dir, 'new')]) {
      const result = run([input, '-o', destination]);
      assert.notEqual(result.status, 0); assert.doesNotMatch(result.stderr, /Traceback/);
      assert.equal(fs.readFileSync(input, 'utf8'), invalid);
      assert.equal(fs.readFileSync(output, 'utf8'), 'preserved');
      if (destination.endsWith('/new')) assert.equal(fs.existsSync(destination), false);
    }
  }
  const gzip = `${input}.gz`; fs.writeFileSync(gzip, 'not gzip');
  const result = run([gzip, '-o', output]);
  assert.notEqual(result.status, 0); assert.doesNotMatch(result.stderr, /Traceback/);
  assert.equal(fs.readFileSync(output, 'utf8'), 'preserved');
});

test("converts compiler-generated nested profile traces in plain and gzip form", (t) => {
  const input = process.env.DEPX_NESTED_INTEGRATION_FILE;
  if (!input) return t.skip("set DEPX_NESTED_INTEGRATION_FILE to a compiler-generated nested trace");
  const plain = run([input]);
  const compressed = run([`${input}.gz`]);
  assert.equal(plain.status, 0, plain.stderr);
  assert.equal(compressed.status, 0, compressed.stderr);
  assert.equal(compressed.stdout, plain.stdout);
  assert.match(plain.stdout, /^profile "nested\/trace_context"$/m);
  assert.match(plain.stdout, /^    (include|create) /m);
  assert.match(plain.stdout, /^dependencies$/m);
});
