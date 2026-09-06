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
