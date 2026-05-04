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
  if (
    typeof value === "string" &&
    value.length >= 2 &&
    value[0] === '"' &&
    value[value.length - 1] === '"'
  ) {
    return value.slice(1, -1);
  }
  return valueText(value);
}

export function symbol(name) {
  return name;
}

export function list(...items) {
  return items;
}

export function hashMap(...items) {
  if (items.length % 2 !== 0) {
    throw new Error("hash-map arguments must be key/value pairs");
  }

  const result = Object.create(null);
  for (let i = 0; i < items.length; i += 2) {
    result[valueText(items[i])] = items[i + 1];
  }
  return result;
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

export function printResult(value) {
  console.log(valueText(value));
}

export function str(...items) {
  return items.map(strText).join("");
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

export function reduce(fn, list) {
  if (!Array.isArray(list)) throw new Error("reduce expects a function and a list");
  if (list.length === 0) throw new Error("reduce expects a non-empty list");
  return list.reduce((acc, item) => fn(acc, item));
}
