;; Version: 0.3.0

(defn generate [rules]
  (let [items
        (map
         (fn [cfg]
           (let [id (str "_" (gensym))]
             {:id id
              :content
              (if (= (:target cfg) "eval")
                (str
                 "\n" id ": " (:out cfg) "\n\n"
                 (:out cfg) ": " (:src cfg) "\n"
                 "\t@ mkdir -p $(dir " (:out cfg) ")\n"
                 "\tly2k -log " (or (:log cfg) false) " -target eval -src $< > $@\n\n")
                (str
                 "SRC_DIR" id " := " (:root cfg) "\n"
                 "OUT_DIR" id " := " (:out-dir cfg) "\n"
                 "EXT_IN" id " := clj\n"
                 "EXT_OUT" id " := " (:target cfg) "\n"
                 "SRC_FILES" id " := $(shell find -L $(SRC_DIR" id ") -type f -name '*.$(EXT_IN" id ")')\n"
                 "OUT_FILES" id " := $(patsubst $(SRC_DIR" id ")/%.$(EXT_IN" id "),$(OUT_DIR" id ")/%.$(EXT_OUT" id "),$(SRC_FILES" id "))\n\n"
                 id ": $(OUT_FILES" id ")\n\n"
                 "$(OUT_DIR" id ")/%.$(EXT_OUT" id "): $(SRC_DIR" id ")/%.$(EXT_IN" id ")\n"
                 "\t@ mkdir -p $(dir $@)\n"
                 "\tly2k -log " (or (:log cfg) false) " -target " (:target cfg) " -src $< -root " (:root cfg) " -namespace " (:namespace cfg) " > $@\n\n"))}))
         rules)]
    (reduce
     (fn [a x] (str a (:content x)))
     (str
      "# GENERATED FILE - DO NOT EDIT\n\n"
      (reduce (fn [a x] (str a " " (:id x))) "all:" items)
      "\n")
     items)))

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

(defn build [config]
  (str
   (generate (:compile config))
   (make_deps config)))
