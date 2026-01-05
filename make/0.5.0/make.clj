(ns make)

;; Version: 0.5.0

(defn- make-var [name id value]
  (str name id " := " value "\n"))

(defn- make-pattern-rule-vars [id src-dir out-dir ext-out]
  (str (make-var "SRC_DIR" id src-dir)
       (make-var "OUT_DIR" id out-dir)
       (make-var "EXT_IN" id "clj")
       (make-var "EXT_OUT" id ext-out)
       (make-var "SRC_FILES" id (str "$(shell find -L $(SRC_DIR" id ") -type f -name '*.$(EXT_IN" id ")')"))
       (make-var "OUT_FILES" id (str "$(patsubst $(SRC_DIR" id ")/%.$(EXT_IN" id "),$(OUT_DIR" id ")/%.$(EXT_OUT" id "),$(SRC_FILES" id "))"))))

(defn- make-pattern-rule [id log-enabled? target namespace prelude-opt]
  (str id ": $(OUT_FILES" id ")\n"
       "$(OUT_DIR" id ")/%.$(EXT_OUT" id "): $(SRC_DIR" id ")/%.$(EXT_IN" id ")\n"
       "\t@ mkdir -p $(dir $@)\n"
       "\tly2k -log " log-enabled?
       prelude-opt
       " -target " target
       " -src $< -namespace " namespace
       " > $@\n\n"))

(defn- generate-dep-rule [id cfg]
  (let [src-dir (str "$(LY2K_PACKAGES_DIR)/" (:name cfg) "/" (:version cfg))
        target (:compile_target cfg)
        prelude-opt (if (= target "js") " -prelude_path ./prelude.js" "")]
    (str "# [DEPENDENCY: " (:name cfg) ":" (:version cfg) "]\n\n"
         (make-pattern-rule-vars id src-dir (:out-dir cfg) target)
         "\n"
         (make-pattern-rule id (or (:log cfg) false) target (:namespace cfg) prelude-opt))))

(defn- generate-eval-rule [id cfg]
  (let [src (:src cfg)
        out (:out cfg)]
    (str "# [FILE: " src "]\n\n"
         id ": " out "\n\n"
         out ": " src "\n"
         "\t@ mkdir -p $(dir " out ")\n"
         "\tly2k -log " (or (:log cfg) false) " -target eval -src $< > $@\n\n")))

(defn- generate-file-rule [id cfg]
  (let [target (:target cfg)
        prelude-opt (if (= target "js") (str " -prelude_path $$PWD/" (:prelude-path cfg)) "")]
    (str "# [FILE: " (:root cfg) "]\n\n"
         (make-pattern-rule-vars id (:root cfg) (:out-dir cfg) target)
         "\n"
         (make-pattern-rule id (or (:log cfg) false) target (:namespace cfg) prelude-opt))))

(defn- generate-rule [cfg]
  (let [id (str "_" (gensym))]
    {:id id
     :content (case (:target cfg)
                "dep"  (generate-dep-rule id cfg)
                "eval" (generate-eval-rule id cfg)
                (generate-file-rule id cfg))}))

(defn- generate [rules]
  (let [items (map generate-rule rules)
        ids (reduce (fn [a x] (str a " " (:id x))) "" items)
        content (reduce (fn [a x] (str a (:content x))) "" items)]
    (str "\n\nall:" ids "\n\n" content)))

(defn build [config]
  (str "# GENERATED FILE - DO NOT EDIT"
       (generate (:rules config))))
