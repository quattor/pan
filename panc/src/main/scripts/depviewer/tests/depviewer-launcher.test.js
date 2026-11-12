const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs/promises');
const path = require('node:path');
const { spawn } = require('node:child_process');
const { once } = require('node:events');
const http = require('node:http');
const { gzipSync } = require('node:zlib');
const { promisify } = require('node:util');
const { execFile } = require('node:child_process');

const scripts = process.env.DEPVIEWER_ASSET_DIRECTORY || path.resolve(__dirname, '..');

async function fixture(t, options = {}) {
  const directory = await fs.mkdtemp('/tmp/opencode/depviewer-launcher-');
  const cleanup = [];
  t.after(async () => {
    for (const close of cleanup) await close();
    await fs.rm(directory, { recursive: true, force: true, maxRetries: 5, retryDelay: 100 });
  });
  const assets = path.join(directory, 'relocated scripts');
  await fs.mkdir(assets);
  for (const name of ['depviewer', 'depviewer.py']) {
    await fs.copyFile(path.join(scripts, name), path.join(assets, name));
  }
  if (!options.missingPage) {
    await fs.copyFile(path.join(scripts, 'depviewer.html'), path.join(assets, 'depviewer.html'));
  }
  const trace = path.join(directory, options.name || 'spaced ü #.depx.gz');
  const json = Buffer.from('{"hello":"world"}');
  const bytes = trace.endsWith('.gz') ? gzipSync(json) : json;
  if (!options.missingTrace) await fs.writeFile(trace, bytes);
  if (options.prepare) await options.prepare(directory, trace);
  const capture = path.join(directory, 'browser-url');
  await fs.writeFile(path.join(directory, 'xdg-open'), '#!/bin/sh\nprintf "%s" "$1" > "$CAPTURE"\nexit ' + (options.openerFailure ? '7' : '0') + '\n', { mode: 0o755 });
  const child = spawn('bash', [path.join(assets, 'depviewer'), ...(options.args || [options.absoluteTrace ? trace : path.basename(trace)])], {
    cwd: directory,
    env: { ...process.env, PATH: directory + ':' + process.env.PATH, CAPTURE: capture },
    stdio: ['ignore', 'pipe', 'pipe'],
  });
  const exited = once(child, 'exit');
  let logs = '';
  child.stdout.on('data', data => { logs += data; });
  child.stderr.on('data', data => { logs += data; });
  t.after(async () => {
    if (child.exitCode === null && child.signalCode === null) child.kill('SIGINT');
    await exited;
  });
  return { directory, trace, bytes, capture, child, exited, cleanup, logs: () => logs };
}

async function browserUrl(f) {
  for (let attempt = 0; attempt < 100; attempt++) {
    try {
      const value = await fs.readFile(f.capture, 'utf8');
      if (value) return new URL(value);
    } catch (error) {
      if (error.code !== 'ENOENT') throw error;
    }
    if (f.child.exitCode !== null) throw new Error(f.logs());
    await new Promise(resolve => setTimeout(resolve, 20));
  }
  throw new Error('Browser was not launched: ' + f.logs());
}

function request(url, headers = {}, method = 'GET') {
  return new Promise((resolve, reject) => {
    const req = http.request(url, { headers, method }, response => {
      const chunks = [];
      response.on('data', chunk => chunks.push(chunk));
      response.on('end', () => resolve({ status: response.statusCode, headers: response.headers, bytes: Buffer.concat(chunks) }));
    });
    req.on('error', reject);
    req.end();
  });
}

test('relocated launcher serves only protected page and original gzip bytes', { timeout: 10000 }, async t => {
  const f = await fixture(t);
  const url = await browserUrl(f);
  assert.equal(url.hostname, '127.0.0.1');
  assert.equal(url.hash, '');
  assert.match(url.pathname, /^\/[A-Za-z0-9_-]{32,}\/depviewer\.html$/);
  const page = await request(url);
  assert.equal(page.status, 200);
  assert.match(page.headers['content-type'], /text\/html/);
  const catalog = await request(new URL('catalog', url));
  assert.equal(catalog.status, 200);
  assert.match(catalog.headers['content-type'], /application\/json/);
  assert.equal(catalog.headers['cache-control'], 'no-store');
  assert.deepEqual(JSON.parse(catalog.bytes), { traces: [{ id: '0', name: path.basename(f.trace) }] });
  const traceUrl = new URL('trace/0', url);
  const trace = await request(traceUrl, { Origin: url.origin, 'Sec-Fetch-Site': 'same-origin' });
  assert.equal(trace.status, 200);
  assert.equal(trace.headers['content-type'], 'application/octet-stream');
  assert.equal(trace.headers['content-encoding'], undefined);
  assert.deepEqual(trace.bytes, f.bytes);
  for (const route of ['/', '/wrong/trace', url.pathname.replace('depviewer.html', ''), url.pathname.replace('depviewer.html', 'depviewer.py'), '/%2e%2e/trace', ...['trace', 'trace/1', 'trace/00', 'trace/-1', 'trace/0?path=other', 'trace/%30', 'trace/' + encodeURIComponent(f.trace)].map(route => new URL(route, url).pathname + new URL(route, url).search)]) {
    assert.equal((await request(new URL(route, url))).status, 404);
  }
  for (const headers of [{ Host: 'evil.example' }, { Host: 'localhost:' + url.port }, { Origin: 'https://evil.example' }, { Origin: 'null' }, { 'Sec-Fetch-Site': 'cross-site' }]) {
    assert.equal((await request(traceUrl, headers)).status, 403);
    assert.equal((await request(new URL('catalog', url), headers)).status, 403);
  }
  assert.equal((await request(traceUrl, {}, 'POST')).status, 405);
  const token = url.pathname.split('/')[1];
  f.child.kill('SIGINT');
  assert.equal((await f.exited)[0], 0);
  assert.doesNotMatch(f.logs(), new RegExp(token));
  assert.match(f.logs(), /shutdown/i);
  await assert.rejects(request(url));
});

test('uncompressed JSON is served byte-for-byte with an absolute trace path', { timeout: 10000 }, async t => {
  const f = await fixture(t, { name: 'trace.depx', absoluteTrace: true });
  const url = await browserUrl(f);
  assert.deepEqual((await request(new URL('trace/0', url))).bytes, f.bytes);
});

for (const [name, options, message] of [
  ['missing argument', { args: [] }, /usage:/i],
  ['invalid explicit input among valid inputs', { args: ['trace.depx', 'missing'], name: 'trace.depx' }, /input/i],
  ['missing trace', { missingTrace: true }, /trace/i],
  ['invalid suffix', { name: 'trace.json' }, /\.depx/],
  ['missing page', { missingPage: true }, /viewer/i],
]) {
  test('launcher rejects ' + name, async t => {
    const f = await fixture(t, options);
    assert.notEqual((await f.exited)[0], 0);
    assert.match(f.logs(), message);
    await assert.rejects(fs.readFile(f.capture), { code: 'ENOENT' });
  });
}

test('recursive mixed inputs are sorted, deduplicated and distinguish colliding names without following directory symlinks', { timeout: 10000 }, async t => {
  const f = await fixture(t, {
    name: 'root.depx',
    args: ['tree', 'root.depx', 'tree/a/same.depx', 'alias.depx'],
    prepare: async directory => {
      await fs.mkdir(path.join(directory, 'tree/a'), { recursive: true });
      await fs.mkdir(path.join(directory, 'tree/b'), { recursive: true });
      await fs.mkdir(path.join(directory, 'outside'));
      await fs.writeFile(path.join(directory, 'tree/a/same.depx'), 'a');
      await fs.writeFile(path.join(directory, 'tree/b/same.depx'), 'b');
      await fs.writeFile(path.join(directory, 'tree/ignored.json'), 'ignored');
      await fs.writeFile(path.join(directory, 'outside/hidden.depx'), 'hidden');
      await fs.symlink('../outside', path.join(directory, 'tree/link'));
      await fs.symlink('..', path.join(directory, 'tree/a/cycle'));
      await fs.symlink('tree/a/same.depx', path.join(directory, 'alias.depx'));
      await fs.symlink('missing', path.join(directory, 'tree/broken.depx'));
    },
  });
  const url = await browserUrl(f);
  const catalog = JSON.parse((await request(new URL('catalog', url))).bytes);
  assert.deepEqual(catalog, { traces: [
    { id: '0', name: 'root.depx' },
    { id: '1', name: 'tree/a/same.depx' },
    { id: '2', name: 'tree/b/same.depx' },
  ] });
  assert.equal((await request(new URL('trace/1', url))).bytes.toString(), 'a');
  assert.equal((await request(new URL('trace/2', url))).bytes.toString(), 'b');
  assert.match(f.logs(), /omit/i);
});

test('empty recursive inventory fails before browser launch', async t => {
  const f = await fixture(t, { args: ['empty'], prepare: directory => fs.mkdir(path.join(directory, 'empty')) });
  assert.notEqual((await f.exited)[0], 0);
  assert.match(f.logs(), /no.*trace/i);
  await assert.rejects(fs.readFile(f.capture), { code: 'ENOENT' });
});

test('explicit directory symlink fails rather than traversing its target', async t => {
  const f = await fixture(t, { args: ['linked'], prepare: async directory => {
    await fs.mkdir(path.join(directory, 'target'));
    await fs.symlink('target', path.join(directory, 'linked'));
  } });
  assert.notEqual((await f.exited)[0], 0);
  assert.match(f.logs(), /directory symlinks are not followed/i);
});

test('hostile display names remain JSON metadata and never route paths', { timeout: 10000 }, async t => {
  const f = await fixture(t, { name: '<img onerror="alert(1)"> #?.depx' });
  const url = await browserUrl(f);
  assert.deepEqual(JSON.parse((await request(new URL('catalog', url))).bytes), {
    traces: [{ id: '0', name: path.basename(f.trace) }],
  });
  assert.deepEqual((await request(new URL('trace/0', url))).bytes, f.bytes);
});

test('thousands of invalid trace contents require only filename metadata at startup', { timeout: 10000 }, async t => {
  const f = await fixture(t, {
    args: ['many'],
    prepare: async directory => {
      await fs.mkdir(path.join(directory, 'many'));
      await Promise.all(Array.from({ length: 2000 }, (_, index) => fs.writeFile(path.join(directory, 'many', `${String(index).padStart(4, '0')}.depx.gz`), 'not gzip')));
    },
  });
  const url = await browserUrl(f);
  const traces = JSON.parse((await request(new URL('catalog', url))).bytes).traces;
  assert.equal(traces.length, 2000);
  assert.deepEqual(traces[1999], { id: '1999', name: '1999.depx.gz' });
});

test('browser-launch failure closes the listening socket', { timeout: 10000 }, async t => {
  const f = await fixture(t, { openerFailure: true, name: 'trace.depx' });
  const url = await browserUrl(f);
  assert.notEqual((await f.exited)[0], 0);
  assert.match(f.logs(), /xdg-open/);
  assert.doesNotMatch(f.logs(), new RegExp(url.pathname.split('/')[1]));
  await assert.rejects(request(url));
});

test('real Chromium filters, pages and lazily loads traces in the unified profile picker', {
  skip: !process.env.DEPVIEWER_BROWSER,
  timeout: 30000,
}, async t => {
  const traceBytes = profile => JSON.stringify({
    format: 'depx', version: 1, roots: [], dependencies: [],
    profiles: [{ name: profile, sites: [], occurrences: [] }],
  });
  const multiple = await fixture(t, { args: ['many'], prepare: async directory => {
    await fs.mkdir(path.join(directory, 'many'));
    await Promise.all(Array.from({ length: 25 }, (_, index) => fs.writeFile(
      path.join(directory, 'many', `${String(index).padStart(3, '0')}-profile-${String(index).padStart(3, '0')}.depx`), traceBytes(`profile-${String(index).padStart(3, '0')}`),
    )));
  } });
  const url = await browserUrl(multiple);
  const browser = spawn(process.env.DEPVIEWER_BROWSER, [
    '--headless', '--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage', '--no-first-run', '--no-default-browser-check',
    '--remote-debugging-port=0', '--user-data-dir=' + path.join(multiple.directory, 'chromium-interaction-profile'), url.href,
  ], { stdio: ['ignore', 'pipe', 'pipe'] });
  const browserExited = once(browser, 'exit');
  multiple.cleanup.push(async () => {
    if (browser.exitCode !== null || browser.signalCode !== null) return;
    browser.kill('SIGTERM');
    const forceKill = setTimeout(() => browser.kill('SIGKILL'), 2000);
    try { await browserExited; } finally { clearTimeout(forceKill); }
  });
  const browserOutput = await new Promise((resolve, reject) => {
    let output = '';
    const timeout = setTimeout(() => { browser.kill('SIGKILL'); reject(new Error('Chromium did not expose a DevTools endpoint')); }, 10000);
    const inspect = data => {
      output += data;
      const endpoint = output.match(/DevTools listening on (ws:\/\/[^\s]+)/);
      if (endpoint) { clearTimeout(timeout); resolve({ output, endpoint: endpoint[1] }); }
    };
    browser.stdout.on('data', inspect);
    browser.stderr.on('data', inspect);
    browser.once('error', error => { clearTimeout(timeout); reject(error); });
  });
  const debuggerHttp = browserOutput.endpoint.replace(/^ws:/, 'http:').replace(/\/devtools\/browser\/.*/, '');
  const targets = await (await fetch(`${debuggerHttp}/json`)).json();
  const pageTarget = targets.find(target => target.type === 'page');
  assert.ok(pageTarget, 'Chromium must expose a page target');
  const devtools = new WebSocket(pageTarget.webSocketDebuggerUrl);
  multiple.cleanup.unshift(() => devtools.close());
  await new Promise((resolve, reject) => { devtools.addEventListener('open', resolve, { once: true }); devtools.addEventListener('error', reject, { once: true }); });
  let commandId = 0;
  const pending = new Map();
  devtools.addEventListener('message', event => {
    const message = JSON.parse(event.data);
    if (!pending.has(message.id)) return;
    const { resolve, reject } = pending.get(message.id);
    pending.delete(message.id);
    if (message.error) reject(new Error(message.error.message));
    else resolve(message.result);
  });
  const command = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++commandId;
    pending.set(id, { resolve, reject });
    devtools.send(JSON.stringify({ id, method, params }));
  });
  await command('Runtime.enable');
  await command('Page.enable');
  const evaluate = async expression => {
    const response = await command('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
    if (response.exceptionDetails) throw new Error(response.exceptionDetails.text);
    return response.result.value;
  };
  for (let attempt = 0; attempt < 100; attempt++) {
    if (await evaluate('Boolean(document.querySelector("#profile-picker-toggle") && !document.querySelector("#profile-control").hidden)')) break;
    await new Promise(resolve => setTimeout(resolve, 20));
  }
  assert.equal(await evaluate('document.querySelector("#profile-picker-popup").hidden'), true, 'catalog choices stay hidden until picker is opened');
  assert.doesNotMatch(await evaluate('document.querySelector("#status").textContent'), /Loaded profile/, 'catalog discovery does not load a trace');
  await evaluate('document.querySelector("#profile-picker-toggle").click()');
  assert.equal(await evaluate('document.activeElement.id'), 'profile-filter', 'opening the picker focuses its filter');
  assert.equal(await evaluate('document.querySelectorAll("#profile-options option").length'), 10, 'opening shows ten choices');
  assert.equal(await evaluate('document.querySelectorAll("#trace-picker, #trace-filter, #trace-load").length'), 0, 'there is no separate trace-loading interface');
  assert.equal(await evaluate('document.querySelector("#profile-options option").textContent'), '000-profile-000', 'filename suffix is removed and relative namespaces are retained');
  await evaluate('document.querySelector("#profile-next-page").click()');
  assert.equal(await evaluate('document.querySelector("#profile-options option").textContent'), '010-profile-010', 'next page exposes further matches');
  assert.equal(await evaluate('document.querySelectorAll("#profile-options option").length'), 10);
  await evaluate('document.querySelector("#profile-filter").value = "profile-024"; document.querySelector("#profile-filter").dispatchEvent(new Event("input", { bubbles: true }))');
  await new Promise(resolve => setTimeout(resolve, 100));
  assert.equal(await evaluate('document.querySelectorAll("#profile-options option").length'), 1, 'filtering is live and bounded');
  assert.equal(await evaluate('document.querySelector("#profile-options option").textContent'), '024-profile-024');
  assert.equal(await evaluate('document.querySelector("#profile-options").getAttribute("aria-label")'), 'Matching profiles and traces, 1 results');
  await evaluate('document.querySelector("#profile-options").selectedIndex = 0; document.querySelector("#profile-options").click()');
  for (let attempt = 0; attempt < 100; attempt++) {
    if ((await evaluate('document.querySelector("#status").textContent')).includes('Loaded profile')) break;
    await new Promise(resolve => setTimeout(resolve, 20));
  }
  assert.match(await evaluate('document.querySelector("#status").textContent'), /Loaded profile “profile-024”/);
  assert.equal(await evaluate('document.querySelector("#profile-picker-popup").hidden'), true, 'successful selection closes the picker');
  assert.equal(await evaluate('Boolean(document.querySelector("#tree").textContent)'), true, 'loaded profile is rendered');
  assert.equal(await evaluate('Boolean(document.querySelector("#trace-picker") || document.querySelector("#trace-load"))'), false, 'no separate trace-loading section remains');
  assert.equal(await evaluate('document.querySelector("#profile-picker-toggle").textContent'), 'profile-024', 'selected profile name becomes the picker label');
  assert.equal(await evaluate('document.querySelectorAll("#profile-options option").length'), 1, 'embedded profiles in the selected trace join the same picker');
});
