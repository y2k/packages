# Make JS Libraries Implementation Plan

**Status:** Completed. Current implementation and final checks verified on 2026-09-06; the historical red-phase run noted below was not preserved.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить в `make/0.6.0` явный `:libs` DSL, который компилирует выбранные библиотеки и копирует JS runtime во все настроенные каталоги сборки.

**Architecture:** Существующие пары `:path`/`:build-dir` остаются без изменений. GNU Make вычисляет уникальные output roots через `$(sort ...)`; для каждой библиотеки генерируются правила по всем этим roots, а один multi-target rule копирует runtime в каждый root.

**Tech Stack:** Clojure dialect, `ly2k --target eval`, GNU Make, JavaScript ESM.

## Global Constraints

- Первый вариант поддерживает только JS target.
- DSL сохраняет существующий формат `:dirs [{:path ... :build-dir ...}]`.
- `:libs` имеет форму `{package-name version}` и по умолчанию пуст.
- Каждая библиотека компилируется в каждый уникальный `:build-dir`.
- Вложенный `<library>/test/` не компилируется; локальный `:dirs` с `:path "test"` компилируется.
- Поддерживаются только пакеты, чей путь `.clj` соответствует namespace.
- Транзитивные зависимости перечисляются вызывающим проектом явно.
- Runtime всегда берётся из `prelude/1.0.0/js/language_runtime.js`.
- `.DELETE_ON_ERROR` удаляет незавершённый output после ошибки `ly2k`.
- Не добавлять resolver, dependency graph, symlinks, Java support, `javac` или npm packaging.
- Не создавать git-коммит без отдельного явного запроса пользователя.

---

### Task 1: Compile libraries into every JS output tree

**Files:**
- Modify: `make/0.6.0/test/make_test.clj:6-49`
- Modify: `make/0.6.0/make.clj:3-75`

**Interfaces:**
- Consumes: `(make/makefile {:target "js" :dirs [{:path string :build-dir string}] :libs {keyword string}})` and `LY2K_PACKAGES_DIR` in generated GNU Make.
- Produces: a Makefile where local sources retain their configured outputs, package sources compile into every unique output root, and `<build-dir>/language_runtime.js` is copied for every root.

- [x] **Step 1: Replace the golden assertion with the new public contract**

Keep the namespace and dependency declaration, then replace the assertion in
`make/0.6.0/test/make_test.clj` with:

```clojure
;; ponytail: one golden assertion covers the public generator contract.
(assert
 (= "# Файл сгенерирован из build.clj. Не редактируйте его вручную.
.DEFAULT_GOAL := all
.DELETE_ON_ERROR:

# Основные настройки компиляции.
LY2K ?= ly2k
LY2K_TARGET ?= js
OUT_EXT ?= js
DIRS := src src test
BUILD_DIRS := $(sort build/src build/test build/test)
RUNTIME_SOURCE := $(LY2K_PACKAGES_DIR)/prelude/1.0.0/js/language_runtime.js
RUNTIME_FILES := $(addsuffix /language_runtime.js,$(BUILD_DIRS))

ifndef LY2K_PACKAGES_DIR
$(error LY2K_PACKAGES_DIR is required)
endif

ifeq ($(wildcard $(LY2K_PACKAGES_DIR)/xml/0.4.0),)
$(error Library not found: xml 0.4.0)
endif

ifeq ($(wildcard $(RUNTIME_SOURCE)),)
$(error JS runtime not found: $(RUNTIME_SOURCE))
endif

# Рекурсивный поиск .clj-файлов в настроенных папках.
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# Путь результата строится от уникальной папки сборки с сохранением структуры исходной папки.
out_file = $(1)/$(patsubst $(2)/%.clj,%.$(OUT_EXT),$(3))

OUT_FILES :=

OUT_FILES += $(foreach file,$(call rwildcard,src/,*.clj),$(call out_file,build/src,src,$(file)))
OUT_FILES += $(foreach file,$(call rwildcard,src/,*.clj),$(call out_file,build/test,src,$(file)))
OUT_FILES += $(foreach file,$(call rwildcard,test/,*.clj),$(call out_file,build/test,test,$(file)))
OUT_FILES += $(foreach build_dir,$(BUILD_DIRS),$(foreach file,$(filter-out $(LY2K_PACKAGES_DIR)/xml/0.4.0/test/%,$(call rwildcard,$(LY2K_PACKAGES_DIR)/xml/0.4.0/,*.clj)),$(call out_file,$(build_dir),$(LY2K_PACKAGES_DIR)/xml/0.4.0,$(file))))

# Шаблон правила компиляции одного исходного файла.
define compile_rule
$(call out_file,$(1),$(2),$(3)): $(3)
	@mkdir -p $$(dir $$@)
	@$$(LY2K) --target $$(LY2K_TARGET) < $$< > $$@
endef

# Создаём отдельное правило для каждого найденного .clj-файла.

$(foreach file,$(call rwildcard,src/,*.clj),$(eval $(call compile_rule,build/src,src,$(file))))
$(foreach file,$(call rwildcard,src/,*.clj),$(eval $(call compile_rule,build/test,src,$(file))))
$(foreach file,$(call rwildcard,test/,*.clj),$(eval $(call compile_rule,build/test,test,$(file))))
$(foreach build_dir,$(BUILD_DIRS),$(foreach file,$(filter-out $(LY2K_PACKAGES_DIR)/xml/0.4.0/test/%,$(call rwildcard,$(LY2K_PACKAGES_DIR)/xml/0.4.0/,*.clj)),$(eval $(call compile_rule,$(build_dir),$(LY2K_PACKAGES_DIR)/xml/0.4.0,$(file)))))

$(RUNTIME_FILES): $(RUNTIME_SOURCE)
	@mkdir -p $(dir $@)
	@cp $< $@

# Пользовательские цели.
.PHONY: all build clean
all: build

build: $(OUT_FILES) $(RUNTIME_FILES)

clean:
	@rm -rf $(BUILD_DIRS)
"
    (make/makefile
     {:target "js"
      :dirs [{:path "src" :build-dir "build/src"}
             {:path "src" :build-dir "build/test"}
             {:path "test" :build-dir "build/test"}]
      :libs {:xml "0.4.0"}})))
```

- [ ] **Step 2: Run the golden test and confirm the old generator fails** *(Retrospectively unverified: the test and generator changes were committed together.)*

Run from `/Users/igor/Projects/packages`:

```bash
ly2k --target eval < make/0.6.0/test/make_test.clj
```

Expected: non-zero exit with `assertion failed`, because the existing generator
does not understand `:libs`, `BUILD_DIRS`, or runtime rules.

- [x] **Step 3: Implement the minimal generator**

Replace `make/0.6.0/make.clj` with:

```clojure
(ns make)

(defn join-texts [texts]
  (reduce (fn [acc text] (str acc text)) texts))

(defn join-texts-with-space [texts]
  (reduce (fn [acc text] (str acc " " text)) texts))

(defn join-dir-paths [dirs]
  (join-texts-with-space (map (fn [dir] (get dir :path)) dirs)))

(defn join-build-dirs [dirs]
  (join-texts-with-space (map (fn [dir] (get dir :build-dir)) dirs)))

(defn library-path [name version]
  (str "$(LY2K_PACKAGES_DIR)/" name "/" version))

(defn library-files [path]
  (str "$(filter-out " path "/test/%,$(call rwildcard," path "/,*.clj))"))

(defn out-files [dirs]
  (join-texts
   (map
    (fn [dir]
      (str "
OUT_FILES += $(foreach file,$(call rwildcard," (get dir :path) "/,*.clj),$(call out_file," (get dir :build-dir) "," (get dir :path) ",$(file)))"))
    dirs)))

(defn library-out-files [libs]
  (reduce
   (fn [acc [name version]]
     (let [path (library-path name version)]
       (str acc "
OUT_FILES += $(foreach build_dir,$(BUILD_DIRS),$(foreach file," (library-files path) ",$(call out_file,$(build_dir)," path ",$(file))))")))
   ""
   libs))

(defn compile-rules [dirs]
  (join-texts
   (map
    (fn [dir]
      (str "
$(foreach file,$(call rwildcard," (get dir :path) "/,*.clj),$(eval $(call compile_rule," (get dir :build-dir) "," (get dir :path) ",$(file))))"))
    dirs)))

(defn library-compile-rules [libs]
  (reduce
   (fn [acc [name version]]
     (let [path (library-path name version)]
       (str acc "
$(foreach build_dir,$(BUILD_DIRS),$(foreach file," (library-files path) ",$(eval $(call compile_rule,$(build_dir)," path ",$(file)))))")))
   ""
   libs))

(defn library-checks [libs]
  (reduce
   (fn [acc [name version]]
     (let [path (library-path name version)]
       (str acc "
ifeq ($(wildcard " path "),)
$(error Library not found: " name " " version ")
endif
")))
   ""
   libs))

(defn makefile [config]
  (let [target (get config :target)
        dirs (get config :dirs)
        libs (or (get config :libs) {})
        dirs-text (join-dir-paths dirs)
        build-dirs-text (join-build-dirs dirs)
        out-files-text (out-files dirs)
        library-out-files-text (library-out-files libs)
        compile-rules-text (compile-rules dirs)
        library-compile-rules-text (library-compile-rules libs)
        library-checks-text (library-checks libs)]
    (str "# Файл сгенерирован из build.clj. Не редактируйте его вручную.
.DEFAULT_GOAL := all
.DELETE_ON_ERROR:

# Основные настройки компиляции.
LY2K ?= ly2k
LY2K_TARGET ?= " target "
OUT_EXT ?= " target "
DIRS := " dirs-text "
BUILD_DIRS := $(sort " build-dirs-text ")
RUNTIME_SOURCE := $(LY2K_PACKAGES_DIR)/prelude/1.0.0/js/language_runtime.js
RUNTIME_FILES := $(addsuffix /language_runtime.js,$(BUILD_DIRS))

ifndef LY2K_PACKAGES_DIR
$(error LY2K_PACKAGES_DIR is required)
endif
" library-checks-text "
ifeq ($(wildcard $(RUNTIME_SOURCE)),)
$(error JS runtime not found: $(RUNTIME_SOURCE))
endif

# Рекурсивный поиск .clj-файлов в настроенных папках.
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# Путь результата строится от уникальной папки сборки с сохранением структуры исходной папки.
out_file = $(1)/$(patsubst $(2)/%.clj,%.$(OUT_EXT),$(3))

OUT_FILES :=
" out-files-text library-out-files-text "

# Шаблон правила компиляции одного исходного файла.
define compile_rule
$(call out_file,$(1),$(2),$(3)): $(3)
	@mkdir -p $$(dir $$@)
	@$$(LY2K) --target $$(LY2K_TARGET) < $$< > $$@
endef

# Создаём отдельное правило для каждого найденного .clj-файла.
" compile-rules-text library-compile-rules-text "

$(RUNTIME_FILES): $(RUNTIME_SOURCE)
	@mkdir -p $(dir $@)
	@cp $< $@

# Пользовательские цели.
.PHONY: all build clean
all: build

build: $(OUT_FILES) $(RUNTIME_FILES)

clean:
	@rm -rf $(BUILD_DIRS)
")))
```

This deliberately uses GNU Make's native `sort`, `foreach`, `addsuffix`, and
multi-target rule instead of adding collection helpers to the Clojure generator.

- [x] **Step 4: Run the golden test and confirm it passes**

Run:

```bash
ly2k --target eval < make/0.6.0/test/make_test.clj
```

Expected: output `true`, exit code 0.

- [x] **Step 5: Verify the real multi-output spectator build**

Run from `/Users/igor/Projects/spectator` with `LY2K_PACKAGES_DIR` pointing at
`/Users/igor/Projects/packages`:

```bash
make clean test
```

Expected: generated source exists in both `.wrangler/bin/src` and
`.wrangler/bin/test`, runtime exists in both directories, and Node tests pass.
The two existing outer Makefile `cp` commands remain harmless and are outside
this change.

- [x] **Step 6: Run repository checks**

Run from `/Users/igor/Projects/packages`:

```bash
git diff --check
git diff -- make/0.6.0/make.clj make/0.6.0/test/make_test.clj docs/superpowers/specs/2026-09-05-make-js-libraries-design.md docs/superpowers/plans/2026-09-05-make-js-libraries.md
```

Expected: `git diff --check` has no output; the diff contains only the approved
DSL implementation, its golden test, and design/plan documents.
