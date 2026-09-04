# Package Eval Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить падающие assertion-тесты для версий пакетов и проверить их на переносимом `xml/0.4.0`.

**Architecture:** Evaluator получает минимальный `assert`, а существующая eval-семантика `reduce` по map переносится в JS и Java runtime. XML-тест живёт во вложенном `test/`, подключает пакет через `deps` и запускается напрямую через eval CLI.

**Tech Stack:** OCaml, Alcotest, Clojure dialect, JavaScript runtime, Java runtime, `ly2k`.

## Global Constraints

- Тест каждой версии хранится в `<package>/<version>/test/<package>_test.clj`.
- Тест запускается через `ly2k --target eval`; отдельный runner не добавляется.
- `reduce` по map передаёт callback двухэлементный список `[key value]` на всех targets.
- XML не использует `Object/entries` и не добавляет пробел при пустых attrs.
- Порядок нескольких атрибутов, XML escaping и test discovery не входят в scope.
- Не создавать git-коммиты без отдельного запроса пользователя.

---

### Task 1: Eval `assert`

**Files:**
- Modify: `../language/test/eval_ns_test.ml`
- Modify: `../language/backend_eval/eval_stdlib.ml`

**Interfaces:**
- Consumes: существующие `Backend_eval.Eval.truthy`-совместимые значения `false`, `nil` и truthy values.
- Produces: eval-функция `(assert condition)`, возвращающая `true` либо бросающая `Eval_error "assertion failed"`.

- [ ] **Step 1: Add failing evaluator tests**

Добавить в `../language/test/eval_ns_test.ml`:

```ocaml
let assert_returns_true () = Alcotest.(check string) "result" "true" (last_symbol {|(assert "ok")|})

let assert_rejects_false () =
  Alcotest.check_raises "assertion failed" (Backend_eval.Eval.Eval_error "assertion failed") (fun () ->
      ignore (eval {|(assert false)|}))

let assert_rejects_nil () =
  Alcotest.check_raises "assertion failed" (Backend_eval.Eval.Eval_error "assertion failed") (fun () ->
      ignore (eval {|(assert nil)|}))
```

Зарегистрировать три case в группе `stdlib`:

```ocaml
Alcotest.test_case "assert returns true" `Quick assert_returns_true;
Alcotest.test_case "assert rejects false" `Quick assert_rejects_false;
Alcotest.test_case "assert rejects nil" `Quick assert_rejects_nil;
```

- [ ] **Step 2: Verify the tests fail**

Run from `../language`:

```sh
dune exec ./test/eval_ns_test.exe
```

Expected: FAIL с `symbol not found: assert`.

- [ ] **Step 3: Implement the minimal evaluator function**

Добавить в `../language/backend_eval/eval_stdlib.ml`:

```ocaml
let assert_ _ = function
  | [ Symbol "false" ] | [ Symbol "nil" ] -> raise (Eval_error "assertion failed")
  | [ _ ] -> Symbol "true"
  | _ -> raise (Eval_error "assert expects one value")
```

Добавить binding в `env`:

```ocaml
("assert", Closure (Native assert_));
```

- [ ] **Step 4: Format and verify**

Run from `../language`:

```sh
ocamlformat -i backend_eval/eval_stdlib.ml test/eval_ns_test.ml
dune exec ./test/eval_ns_test.exe
```

Expected: suite `eval ns` passes.

### Task 2: Cross-target `reduce` over maps

**Files:**
- Create: `../language/test/samples/map_reduce.clj`
- Modify: `prelude/1.0.0/js/language_runtime.js`
- Modify: `prelude/1.0.0/java/language_runtime.java`

**Interfaces:**
- Consumes: `(reduce fn init collection)` and map values produced by `hash-map`.
- Produces: JS and Java parity with eval, where each map entry is passed as `[key value]`.

- [ ] **Step 1: Add a failing cross-target sample**

Create `../language/test/samples/map_reduce.clj`:

```clojure
;; a1b2

(defn test []
  (reduce
   (fn [acc [k v]] (str acc k v))
   ""
   {:a 1 :b 2}))
```

- [ ] **Step 2: Verify JS and Java fail while eval passes**

Run from `../language`:

```sh
make test_smoke
```

Expected: new sample passes on eval and fails on JS or Java because runtime `reduce` accepts only lists.

- [ ] **Step 3: Extend the JavaScript runtime**

В `prelude/1.0.0/js/language_runtime.js`, после `if (!hasInit) list = init;`, преобразовать map в entries перед существующим list reduction:

```javascript
if (!Array.isArray(list)) {
  if (list !== null && typeof list === "object") list = Object.entries(list);
  else throw new Error("reduce expects a list or hash-map");
}
```

Удалить прежнюю проверку, которая безусловно бросает для non-array.

- [ ] **Step 4: Extend the Java runtime**

Добавить в `prelude/1.0.0/java/language_runtime.java` один общий conversion helper:

```java
static java.util.List<?> reduce_items(Object collection) {
  if (collection instanceof java.util.List<?> items)
    return items;
  if (collection instanceof java.util.Map<?, ?> map) {
    var items = new java.util.ArrayList<java.util.List<Object>>();
    for (var entry : map.entrySet())
      items.add(java.util.Arrays.asList(entry.getKey(), entry.getValue()));
    return items;
  }
  throw new RuntimeException("reduce expects a list or hash-map");
}
```

В обоих overload `reduce` заменить проверки/cast списка на:

```java
var items = reduce_items(collection);
```

- [ ] **Step 5: Verify cross-target parity**

Run from `../language`:

```sh
make test_smoke
```

Expected: sample `map_reduce.clj` and the smoke suite pass on eval, JS and Java.

### Task 3: Local XML eval test and portable serializer

**Files:**
- Create: `xml/0.4.0/test/xml_test.clj`
- Modify: `xml/0.4.0/xml.clj`

**Interfaces:**
- Consumes: eval `(assert condition)`, package loading via `(deps {:xml "0.4.0"})`, and portable map `reduce`.
- Produces: local test command and XML without redundant spaces.

- [ ] **Step 1: Add the failing package test**

Create `xml/0.4.0/test/xml_test.clj`:

```clojure
(ns xml-test
  (:require [xml :as xml]))

(deps {:xml "0.4.0"})

(assert (= "<tag></tag>" (xml/to-string [:tag])))
(assert (= "<tag id='x'></tag>" (xml/to-string [:tag {:id "x"}])))
(assert (= "<tag>text</tag>" (xml/to-string [:tag {} "text"])))
(assert (= "<root><child></child></root>"
           (xml/to-string [:root {} [:child]])))
```

- [ ] **Step 2: Verify the package test fails**

Run from the packages repository root:

```sh
ly2k --target eval < xml/0.4.0/test/xml_test.clj
```

Expected: FAIL on `Object/entries` or the existing redundant XML spaces.

- [ ] **Step 3: Remove JS-specific entries and redundant spaces**

В `xml/0.4.0/xml.clj` изменить `attrs_to_string`, передавая map прямо в portable `reduce`:

```clojure
(defn- attrs_to_string [attrs]
  (reduce
   (fn [acc [k v]]
     (str acc " " k "='" v "'"))
   ""
   attrs))
```

В обеих ветках с attrs убрать строковый пробел после tag:

```clojure
(str "<" tag (attrs_to_string (get node 1)) ">"
     "</" tag ">")
```

```clojure
(str "<" tag (attrs_to_string (get node 1)) ">"
     (reduce
      (fn [a x] (str a (to-string x)))
      ""
      (drop 2 node))
     "</" tag ">")
```

- [ ] **Step 4: Verify the package test**

Run from the packages repository root:

```sh
ly2k --target eval < xml/0.4.0/test/xml_test.clj
```

Expected: output `true`, exit code 0.

- [ ] **Step 5: Run final verification**

Run from `../language`:

```sh
make test
```

Run from the packages repository root:

```sh
ly2k --target eval < xml/0.4.0/test/xml_test.clj
git diff --check
```

Expected: all language tests pass, XML test prints `true`, and `git diff --check` reports no errors.
