"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

function viewer() {
  const elements = new Map(); let document;
  class Element {
    constructor(tag = "") { this.tag = tag; this.children = []; this.listeners = {}; this.attributes = {}; this.style = {}; this.dataset = {}; this.value = ""; this.hidden = true; this.selectedIndex = -1; this.classList = { toggle() {} }; }
    get options() { return this.children; }
    get lastChild() { return this.children.at(-1); }
    get textContent() { return this.text || this.children.map(child => child.textContent).join(""); }
    set textContent(value) { this.text = String(value); this.children = []; }
    append(...children) { for (const child of children) child.tag === "fragment" ? this.children.push(...child.children) : this.children.push(child); }
    replaceChildren(...children) { this.text = ""; this.children = children; this.selectedIndex = -1; }
    setAttribute(name, value) { this.attributes[name] = value; }
    addEventListener(name, handler) { (this.listeners[name] ||= []).push(handler); }
    focus() { document.activeElement = this; }
    scrollIntoView() {}
    contains(element) { return element === this || this.children.includes(element); }
    fire(name, properties = {}) { return Promise.all((this.listeners[name] || []).map(handler => handler({ target: this, currentTarget: this, preventDefault() {}, stopPropagation() {}, ...properties }))); }
  }
  document = { getElementById(id) { if (!elements.has(id)) elements.set(id, new Element()); return elements.get(id); }, createElement: tag => new Element(tag), createDocumentFragment: () => new Element("fragment"), addEventListener() {} };
  const html = fs.readFileSync(path.join(__dirname, "..", "depviewer.html"), "utf8");
  vm.runInNewContext(html.match(/<script>([\s\S]*?)<\/script>/)[1], { document, window: { location: { protocol: "file:" } }, TextDecoder, Uint8Array, Blob, DecompressionStream, URL });
  const element = id => document.getElementById(id);
  const load = async profiles => { const bytes = Buffer.from(JSON.stringify({ format: "depx", version: 1, roots: [], dependencies: [], profiles })); element("file-input").files = [{ name: "trace.depx", size: bytes.length, arrayBuffer: async () => bytes }]; await element("file-input").fire("change"); };
  return { element, load, document, html };
}

function profile(events = []) {
  return { name: "p", sites: [], occurrences: [], global_trace: { version: 1, object_glob: "*", variable_glob: "*", selected: true, complete: true, limits: { max_events: 10000, max_bytes: 8388608 }, dropped_events: 0, events } };
}
function event(sequence, variable = "<img>") { return { format: "pan-global-trace", version: 1, sequence, object: "p", object_file: null, template: "<source>", template_file: null, location: null, occurrence: null, action: "read", variable, value: { type: "string", value: "<script>" } }; }

test("variables expose literal names, counts, root details and safe values", async () => {
  const { element, load, html } = viewer();
  await load([profile([event(1)])]);
  assert.match(element("variable-capture-status").textContent, /Showing recorded/);
  assert.match(element("variable-options").options[0].textContent, /<img> · 0 set · 1 use · 0 skipped/);
  element("variable-options").selectedIndex = 0; await element("variable-options").fire("change");
  const button = element("variable-events").children[0].children[0]; await button.fire("click");
  assert.match(element("variable-details").textContent, /Profile root/);
  assert.match(element("variable-details").textContent, /<script>/);
  assert.doesNotMatch(html, /innerHTML\s*=/);
});

test("variable searches distinguish no match and retain incomplete metadata", async () => {
  const { element, load } = viewer(); const p = profile([event(1)]); p.global_trace.complete = false; p.global_trace.dropped_events = 7;
  await load([p]); element("variable-filter").value = "missing"; await element("variable-filter").fire("input");
  assert.match(element("variable-search-status").textContent, /No variables match/);
  assert.match(element("variable-capture-status").textContent, /Capture incomplete: 7/);
  assert.match(element("variable-capture-status").textContent, /8388608/);
});

test("capture states and variable and event pagination remain distinct and bounded", async () => {
  const { element, load, document } = viewer();
  const old = profile(); delete old.global_trace; await load([old]);
  assert.match(element("variable-capture-status").textContent, /not captured/);
  const filtered = profile(); filtered.global_trace.selected = false; await load([filtered]);
  assert.match(element("variable-capture-status").textContent, /excluded/);
  await load([profile()]); assert.match(element("variable-capture-status").textContent, /No matching variable activity/);
  const incomplete = profile(); incomplete.global_trace.complete = false; incomplete.global_trace.dropped_events = 1;
  await load([incomplete]); assert.match(element("variable-capture-status").textContent, /Capture incomplete/);
  const events = Array.from({ length: 51 }, (_, i) => event(i + 1, "a"));
  for (let i = 0; i < 10; i++) events.push(event(52 + i, `var${i}`));
  events[0] = { ...event(1, "a"), action: "assign", conditional: false, final: true, old: { type: "absent" }, new: { type: "null" } }; delete events[0].value;
  events[1] = { ...event(2, "a"), action: "skip_default", conditional: true, final: false, existing: { type: "string", value: "short", truncated: true, reason: "string", size: 100 } }; delete events[1].value;
  await load([profile(events)]);
  assert.equal(element("variable-options").children.length, 10);
  assert.match(element("variable-options").children[0].textContent, /1 set · 49 use · 1 skipped/);
  element("variable-options").selectedIndex = 0; await element("variable-options").fire("change");
  assert.equal(element("variable-events").children.length, 50);
  const first = element("variable-events").children[0].children[0]; await first.fire("click");
  assert.match(element("variable-details").textContent, /absent/); assert.match(element("variable-details").textContent, /null/);
  assert.equal(document.activeElement.attributes["aria-pressed"], "true");
  await element("variable-events").children[1].children[0].fire("click"); assert.match(element("variable-details").textContent, /truncated: string, size 100/);
  await element("variable-event-next-page").fire("click"); assert.equal(element("variable-events").children.length, 1);
  assert.match(element("variable-event-page-label").textContent, /Events 51–51 of 51/);
  assert.equal(document.activeElement, element("variable-event-previous-page"));
  await element("variable-next-page").fire("click"); assert.equal(element("variable-options").children.length, 1);
  assert.match(element("variable-options").children[0].textContent, /var9/);
});

test("event navigation reveals the exact repeated off-page occurrence and resets on document change", async () => {
  const { element, load, document } = viewer(); const p = profile([event(1, "v")]);
  p.sites = [{ caller: null, target: { name: "same", source: null }, location: null }];
  p.occurrences = Array.from({ length: 505 }, (_, i) => ({ site: 0, parent: i === 0 ? null : 0, status: "executed" }));
  p.global_trace.events[0].occurrence = 504;
  await load([p]); element("variable-options").selectedIndex = 0; await element("variable-options").fire("change");
  await element("variable-events").children[0].children[0].fire("click");
  element("search-input").value = "not found"; await element("search-input").fire("input");
  const details = element("variable-details"); const button = details.children.find(child => child.id === "variable-show-occurrence"); await button.fire("click");
  assert.equal(document.activeElement.dataset.index, "504"); assert.equal(document.activeElement.attributes["aria-selected"], "true");
  assert.match(element("status").textContent, /search cleared/i); assert.match(element("variable-details").textContent, /<source>/);
  await load([profile([event(1, "other")])]); assert.equal(element("variable-filter").value, "");
  assert.match(element("variable-details").textContent, /Select an event/);
});

test("profile switch resets variable selection while failed replacement preserves it", async () => {
  const { element, load } = viewer(); const first = profile([event(1, "v")]);
  const second = profile([event(1, "v")]); second.name = "second"; second.global_trace.events[0].object = "second";
  await load([first, second]); element("variable-filter").value = "v"; await element("variable-filter").fire("input");
  element("variable-options").selectedIndex = 0; await element("variable-options").fire("change"); await element("variable-events").children[0].children[0].fire("click");
  element("file-input").files = [{ name: "bad.depx", size: 1, arrayBuffer: async () => Buffer.from("{") }]; await element("file-input").fire("change");
  assert.equal(element("variable-filter").value, "v"); assert.match(element("variable-details").textContent, /Profile root/);
  await element("profile-picker-toggle").fire("click"); element("profile-options").selectedIndex = 1; await element("profile-options").fire("click");
  assert.equal(element("profile-current").textContent, "second"); assert.equal(element("variable-filter").value, ""); assert.match(element("variable-details").textContent, /Select an event/);
});
