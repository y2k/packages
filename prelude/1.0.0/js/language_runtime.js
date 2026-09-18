class Atom {
  constructor(value) {
    this.value = value;
  }
}

export function atom(value) {
  if (arguments.length !== 1) throw new Error("atom expects one value");
  return new Atom(value);
}

export function deref(reference) {
  if (arguments.length !== 1 || !(reference instanceof Atom))
    throw new Error("deref expects one atom");
  return reference.value;
}

export function reset_BANG_(reference, value) {
  if (arguments.length !== 2 || !(reference instanceof Atom))
    throw new Error("reset! expects an atom and a value");
  reference.value = value;
  return value;
}

export function swap_BANG_(reference, fn) {
  if (arguments.length !== 2 || !(reference instanceof Atom) || typeof fn !== "function")
    throw new Error("swap! expects an atom and a function");
  // ponytail: sequential updates; synchronization needs a separate concurrency contract.
  const value = fn(reference.value);
  reference.value = value;
  return value;
}

function valueText(value) {
  if (value === null || value === undefined) return "nil";
  if (value instanceof Atom) return "#<atom>";
  if (Array.isArray(value)) return `(${value.map(valueText).join(" ")})`;
  if (typeof value === "object") {
    return `{${Object.entries(value)
      .map(([key, value]) => `${key} ${valueText(value)}`)
      .join(" ")}}`;
  }
  if (typeof value === "function") return "#<function>";
  return String(value);
}

function strText(value) {
  return valueText(value);
}

export function list(...items) {
  return items;
}

export function vector_QMARK_(value) {
  return Array.isArray(value);
}

export function _EQ_(left, right) {
  return Object.is(left, right);
}

export function not_EQ_(left, right) {
  return !_EQ_(left, right);
}

export function _GT_(left, right) {
  return left > right;
}

export function _LT_(left, right) {
  return left < right;
}

export function _GT__EQ_(left, right) {
  return left >= right;
}

export function _LT__EQ_(left, right) {
  return left <= right;
}

export function concat(...collections) {
  const result = [];
  for (const collection of collections) {
    if (!Array.isArray(collection)) throw new Error("concat expects lists");
    result.push(...collection);
  }
  return result;
}

export function hash_map(...items) {
  if (items.length % 2 !== 0) {
    throw new Error("hash-map arguments must be key/value pairs");
  }

  const result = Object.create(null);
  for (let i = 0; i < items.length; i += 2) {
    result[valueText(items[i])] = items[i + 1];
  }
  return result;
}

export function get(collection, key) {
  if (collection === null) return null;
  if (Array.isArray(collection)) return Number.isInteger(key) && key >= 0 ? collection[key] ?? null : null;
  if (collection !== null && typeof collection === "object") return collection[valueText(key)] ?? null;
  throw new Error("get expects a hash-map/list and a key/index");
}

export function get_in(collection, keys) {
  if (!Array.isArray(keys)) throw new Error("get-in expects a collection and a vector path");
  return keys.reduce((value, key) => get(value, key), collection);
}

export function truthy(value) {
  return (
    value !== false &&
    value !== null &&
    value !== undefined &&
    value !== "false" &&
    value !== "nil"
  );
}

export function not(value) {
  return !truthy(value);
}

export function print_result(value) {
  console.log(valueText(value));
}

export function println(...items) {
  console.log(items.map(strText).join(" "));
}

export function eprintln(...items) {
  console.error(items.map(strText).join(" "));
}

export function str(...items) {
  return items.map(strText).join("");
}

export function _PLUS_(...items) {
  const result = items.reduce((sum, item) => sum + item, 0);
  return result === 0 ? 0 : result;
}

export function _MINUS_(...items) {
  if (items.length === 0) throw new Error("- expects at least one number");
  const result = items.slice(1).reduce((result, item) => result - item, items[0]);
  return result === 0 ? 0 : result;
}

export function _STAR_(...items) {
  const result = items.reduce((result, item) => result * item, 1);
  return result === 0 ? 0 : result;
}

export function _SLASH_(...items) {
  if (items.length === 0) throw new Error("/ expects at least one number");
  return items.slice(1).reduce((result, item) => Math.trunc(result / item), items[0]);
}

export function count(collection) {
  if (Array.isArray(collection)) return collection.length;
  if (collection !== null && typeof collection === "object") {
    return Object.keys(collection).length;
  }
  throw new Error("count expects one collection");
}

export function map(fn, list) {
  if (!Array.isArray(list)) throw new Error("map expects a function and a list");
  return list.map((item) => fn(item));
}

export function drop(count, list) {
  return list.slice(Math.max(0, count));
}

export function reduce(fn, init, list) {
  const hasInit = arguments.length === 3;
  if (!hasInit) list = init;
  if (!Array.isArray(list)) {
    if (list !== null && typeof list === "object") list = Object.entries(list);
    else throw new Error("reduce expects a list or hash-map");
  }
  if (!hasInit && list.length === 0) throw new Error("reduce expects a non-empty list");
  return hasInit
    ? list.reduce((acc, item) => fn(acc, item), init)
    : list.reduce((acc, item) => fn(acc, item));
}
