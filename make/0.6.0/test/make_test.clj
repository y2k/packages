(ns make-test
  (:require [make :as make]))

(deps {:make "0.6.0"})

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
