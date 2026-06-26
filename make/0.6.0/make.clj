(ns make)

(defn join-texts [texts]
  (reduce (fn [acc text] (str acc text)) texts))

(defn join-texts-with-space [texts]
  (reduce (fn [acc text] (str acc " " text)) texts))

(defn join-dir-paths [dirs]
  (join-texts-with-space (map (fn [dir] (get dir :path)) dirs)))

(defn out-files [dirs]
  (join-texts
   (map
    (fn [dir]
      (str "
OUT_FILES += $(foreach file,$(call rwildcard," (get dir :path) "/,*.clj),$(call out_file," (get dir :build-dir) "," (get dir :path) ",$(file)))"))
    dirs)))

(defn compile-rules [dirs]
  (join-texts
   (map
    (fn [dir]
      (str "
$(foreach file,$(call rwildcard," (get dir :path) "/,*.clj),$(eval $(call compile_rule," (get dir :build-dir) "," (get dir :path) ",$(file))))"))
    dirs)))

(defn clean-dirs [dirs]
  (join-texts-with-space
   (map (fn [dir] (get dir :build-dir)) dirs)))

(defn makefile [config]
  (let [target (get config :target)
        dirs (get config :dirs)
        dirs-text (join-dir-paths dirs)
        out-files-text (out-files dirs)
        compile-rules-text (compile-rules dirs)
        clean-dirs-text (clean-dirs dirs)]
    (str "# Файл сгенерирован из build.clj. Не редактируйте его вручную.
.DEFAULT_GOAL := all

# Основные настройки компиляции.
LY2K ?= ly2k
LY2K_TARGET ?= " target "
OUT_EXT ?= " target "
DIRS := " dirs-text "

# Рекурсивный поиск .clj-файлов в настроенных папках.
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# Путь результата строится от уникальной папки сборки с сохранением структуры исходной папки.
out_file = $(1)/$(patsubst $(2)/%.clj,%.$(OUT_EXT),$(3))

OUT_FILES :=
" out-files-text "

# Шаблон правила компиляции одного исходного файла.
define compile_rule
$(call out_file,$(1),$(2),$(3)): $(3)
	@mkdir -p $$(dir $$@)
	@$$(LY2K) --target $$(LY2K_TARGET) < $$< > $$@
endef

# Создаём отдельное правило для каждого найденного .clj-файла.
" compile-rules-text "

# Пользовательские цели.
.PHONY: all build clean
all: build

build: $(OUT_FILES)

clean:
	@rm -rf " clean-dirs-text "
")))
