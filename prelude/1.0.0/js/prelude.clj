(ns prelude)

(defn hash_map_from [xs]
  (__compiler_emit "
    const result = {};
    for (let i = 0; i < xs.length; i += 2) {
        result[xs[i]] = xs[i + 1];
    }")
  result)

(defn debug_assert [a b]
  (__compiler_emit "JSON.stringify(a) === JSON.stringify(b)"))

(defn re_find [p i]
  (__compiler_emit "
    const match = p.exec(i);
    const result = match == null ? null : match[0];")
  result)

(defn swap_BANG_ [atom f]
  (__compiler_emit "
    const result = atom[0];
    atom[0] = f(result);")
  result)

(defn _PLUS_ [& xs]
  (__compiler_emit "xs.reduce((a, b) => a + b)"))

(defn _MINUS_ [a b]
  (__compiler_emit "a - b"))

(defn inc [a]
  (__compiler_emit "a + 1"))

(defn update [m k f]
  (__compiler_emit "{ ...m, [k]: f(m[k]) }"))
