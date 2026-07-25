function valueText(value) {
  if (value === null || value === undefined) return "nil";
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
  if (Array.isArray(collection)) return Number.isInteger(key) && key >= 0 ? collection[key] ?? null : null;
  if (collection !== null && typeof collection === "object") return collection[valueText(key)] ?? null;
  throw new Error("get expects a hash-map/list and a key/index");
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
  return items.reduce((sum, item) => sum + item, 0);
}

export function _MINUS_(...items) {
  if (items.length === 0) throw new Error("- expects at least one number");
  return items.slice(1).reduce((result, item) => result - item, items[0]);
}

export function _STAR_(...items) {
  return items.reduce((result, item) => result * item, 1);
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

export function reduce(fn, init, list) {
  const hasInit = arguments.length === 3;
  if (!hasInit) list = init;
  if (!Array.isArray(list)) throw new Error("reduce expects a function and a list");
  if (!hasInit && list.length === 0) throw new Error("reduce expects a non-empty list");
  return hasInit
    ? list.reduce((acc, item) => fn(acc, item), init)
    : list.reduce((acc, item) => fn(acc, item));
}
