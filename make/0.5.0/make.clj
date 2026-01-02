(ns make)

;; Version: 0.5.0

;; Deprecated
(defn- make_deps [config]
  (if (some? (:deps config))
    (let [dir (or (:out-dir config) "src/vendor")]
      (reduce
       (fn [a [name ver]]
         (str
          a
          "\t@ ln -s $(LY2K_PACKAGES_DIR)/" name "/" ver " " dir "/" name "\n"))
       (str ".PHONY: restore\nrestore:\n\t@ rm -rf " dir "\n\t@ mkdir -p " dir "\n")
       (:deps config)))
    ""))

(defn- generate [rules]
  (let [items
        (map
         (fn [cfg]
           (let [id (str "_" (gensym))]
             {:id id
              :content
              (case (:target cfg)
                "dep"
                (str
                 "# [DEPENDENCY: " (:name cfg) ":" (:version cfg) "]\n\n"
                 "SRC_DIR" id " := $(LY2K_PACKAGES_DIR)/" (:name cfg) "/" (:version cfg) "\n"
                 "OUT_DIR" id " := " (:out-dir cfg) "\n"
                 "EXT_IN" id " := clj\n"
                 "EXT_OUT" id " := " (:compile_target cfg) "\n"
                 "SRC_FILES" id " := $(shell find -L $(SRC_DIR" id ") -type f -name '*.$(EXT_IN" id ")')\n"
                 "OUT_FILES" id " := $(patsubst $(SRC_DIR" id ")/%.$(EXT_IN" id "),$(OUT_DIR" id ")/%.$(EXT_OUT" id "),$(SRC_FILES" id "))\n\n"
                 id ": $(OUT_FILES" id ")\n"
                 "$(OUT_DIR" id ")/%.$(EXT_OUT" id "): $(SRC_DIR" id ")/%.$(EXT_IN" id ")\n"
                 "\t@ mkdir -p $(dir $@)\n"
                 "\tly2k -log " (or (:log cfg) false)
                 (if (= (:compile_target cfg) "js") (str " -prelude_path ./prelude.js") "")
                 " -target " (:compile_target cfg)
                 " -src $< -namespace " (:namespace cfg)
                 " > $@\n\n")

                "eval"
                (str
                 "# [FILE: " (:src cfg) "]\n"
                 "\n" id ": " (:out cfg) "\n\n"
                 (:out cfg) ": " (:src cfg) "\n"
                 "\t@ mkdir -p $(dir " (:out cfg) ")\n"
                 "\tly2k -log " (or (:log cfg) false) " -target eval -src $< > $@\n\n")

                (str
                 "# [FILE: " (:root cfg) "]\n\n"
                 "SRC_DIR" id " := " (:root cfg) "\n"
                 "OUT_DIR" id " := " (:out-dir cfg) "\n"
                 "EXT_IN" id " := clj\n"
                 "EXT_OUT" id " := " (:target cfg) "\n"
                 "SRC_FILES" id " := $(shell find -L $(SRC_DIR" id ") -type f -name '*.$(EXT_IN" id ")')\n"
                 "OUT_FILES" id " := $(patsubst $(SRC_DIR" id ")/%.$(EXT_IN" id "),$(OUT_DIR" id ")/%.$(EXT_OUT" id "),$(SRC_FILES" id "))\n\n"
                 id ": $(OUT_FILES" id ")\n"
                 "$(OUT_DIR" id ")/%.$(EXT_OUT" id "): $(SRC_DIR" id ")/%.$(EXT_IN" id ")\n"
                 "\t@ mkdir -p $(dir $@)\n"
                 "\tly2k"
                 " -log " (or (:log cfg) false)
                 (if (= (:target cfg) "js") (str " -prelude_path $$PWD/" (:prelude-path cfg)) "")
                 " -target " (:target cfg)
                 " -src $<"
                 " -namespace " (:namespace cfg)
                 " > $@\n\n"))}))
         rules)]
    (reduce
     (fn [a x] (str a (:content x)))
     (str
      "\n\n"
      (reduce (fn [a x] (str a " " (:id x))) "all:" items)
      "\n\n")
     items)))

(defn build [config]
  (str
   "# GENERATED FILE - DO NOT EDIT"
   (generate (:rules config))
  ;;  (make_deps config)
   ))
