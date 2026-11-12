"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

// Exercise the real event handlers without a browser dependency.
function viewer(catalog = []) {
  const elements = new Map();
  let document;
  class Element {
    constructor() {
      this.children = []; this.listeners = {}; this.attributes = {};
      this.value = ""; this.hidden = true; this.selectedIndex = -1;
      this.classList = { toggle() {} };
    }
    get options() { return this.children; }
    get lastChild() { return this.children.at(-1); }
    append(...children) { this.children.push(...children); }
    replaceChildren(...children) { this.children = children; this.selectedIndex = -1; }
    setAttribute(name, value) { this.attributes[name] = value; }
    addEventListener(name, handler) { (this.listeners[name] ||= []).push(handler); }
    focus() { document.activeElement = this; }
    contains(element) { return element === this || this.children.includes(element); }
    fire(name, properties = {}) {
      return Promise.all((this.listeners[name] || []).map(handler => handler({ target: this, preventDefault() {}, ...properties })));
    }
  }
  document = {
    getElementById(id) { if (!elements.has(id)) elements.set(id, new Element()); return elements.get(id); },
    createElement() { const element = new Element(); element.style = {}; return element; },
    createDocumentFragment() { return new Element(); },
    addEventListener() {},
  };
  const pending = [];
  const script = fs.readFileSync(path.join(__dirname, "..", "depviewer.html"), "utf8").match(/<script>([\s\S]*?)<\/script>/)[1];
  vm.runInNewContext(script, {
    document, window: { location: { protocol: catalog.length ? "http:" : "file:" } },
    fetch: route => route === "catalog"
      ? Promise.resolve({ ok: true, json: async () => ({ traces: catalog }) })
      : new Promise(resolve => pending.push(resolve)),
    File: class { constructor(chunks, name) { this.name = name; this.size = 0; this.arrayBuffer = async () => Buffer.concat(chunks); } },
    TextDecoder, Uint8Array, Blob, DecompressionStream, URL,
  });
  const element = id => document.getElementById(id);
  const local = async (name, profiles) => {
    const bytes = Buffer.from(JSON.stringify({ format: "depx", version: 1, roots: [], dependencies: [], profiles: profiles.map(name => ({ name, sites: [], occurrences: [] })) }));
    element("file-input").files = [{ name, size: bytes.length, arrayBuffer: async () => bytes }];
    await element("file-input").fire("change");
  };
  return { element, local, pending, document };
}

for (const [traceName, label] of [["namespace/web", "namespace/web"], ["web", "web"], ["release", "release · web"]]) {
test(`remote picker click installs ${traceName} without invalidating its generation`, async () => {
  const { element, pending } = viewer([{ id: "0", name: `${traceName}.depx` }]);
  await new Promise(resolve => setImmediate(resolve));
  await element("profile-picker-toggle").fire("click");
  element("profile-options").selectedIndex = 0;
  await element("profile-options").fire("click");
  assert.equal(pending.length, 1);
  const bytes = Buffer.from(JSON.stringify({ format: "depx", version: 1, roots: [], dependencies: [], profiles: [{ name: "web", sites: [], occurrences: [] }] }));
  pending[0]({ ok: true, headers: { get: () => null }, body: new ReadableStream({ start(controller) { controller.enqueue(bytes); controller.close(); } }) });
  await new Promise(resolve => setImmediate(resolve));
  assert.match(element("status").textContent, /Loaded profile “web”/);
  assert.equal(element("profile-current").textContent, "web");
  await element("profile-picker-toggle").fire("click");
  assert.deepEqual(element("profile-options").options.map(option => option.textContent), [label]);
});
}

test("keyboard Enter selects the first filtered profile and returns focus to the picker", async () => {
  const { element, local, document } = viewer();
  await local("manual.depx", ["first", "second"]);
  await element("profile-picker-toggle").fire("click");
  assert.equal(document.activeElement, element("profile-filter"));
  element("profile-filter").value = "second";
  await element("profile-filter").fire("input");
  await element("profile-filter").fire("keydown", { key: "ArrowDown" });
  await element("profile-options").fire("keydown", { key: "Enter" });
  assert.equal(element("profile-current").textContent, "second");
  assert.equal(element("profile-picker-popup").hidden, true);
  assert.equal(document.activeElement, element("profile-picker-toggle"));
});

test("clicking the first filtered profile loads it without a separate action", async () => {
  const { element, local } = viewer();
  await local("manual.depx", ["first", "second"]);
  await element("profile-picker-toggle").fire("click");
  element("profile-options").selectedIndex = 0;
  await element("profile-options").fire("click");
  assert.equal(element("profile-picker-popup").hidden, true);
  assert.equal(element("profile-current").textContent, "first");
});

test("failed manual replacement keeps the displayed profile and its picker entries", async () => {
  const { element, local } = viewer();
  await local("manual.depx", ["first", "second"]);
  element("file-input").files = [{ name: "bad.depx", size: 1, arrayBuffer: async () => Buffer.from("{") }];
  await element("file-input").fire("change");
  assert.equal(element("profile-current").textContent, "first");
  assert.match(element("status").textContent, /Invalid depx JSON/);
  await element("profile-picker-toggle").fire("click");
  assert.deepEqual(element("profile-options").options.map(option => option.textContent), ["first", "second"]);
});
