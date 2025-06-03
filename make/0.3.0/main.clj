(comment
  (defn generate [config]
    (let [files (string/split (string/trim STDIN) "\n")]
      (->>
       config
       (map
        (fn [cfg]
          (->>
           files
           (filter (fn [x]
                     (string/starts-with? x (str "./" (:root cfg)))))
           (map
            (fn [x]
              (let [[name] (re-find "./([0-9A-Za-z/_\.-]+).clj" x)
                    file (str (:out-dir cfg) "/" name "." (:target cfg))]
                (str file ":\n\t@ ly2k -target java -src " x " > " file))))
           (reduce
            (fn [a x] (str a "\n\n" x)) ""))))
       (reduce
        (fn [a x] (str a x))
        "# GENERATED FILE - DO NOT EDIT")))))

(defn generate [config]
  (->>
   config
   (map
    (fn [cfg]
;;
      (str
       ;; (:out-dir cfg) "/%." (:target cfg) ": ./%.clj\n"
       ;; $(OUT_DIR)/%: $(SRC_DIR)/%.clj\n
       "SRC_DIR := " (:root cfg) "\n"
       "OUT_DIR := " (:out-dir cfg) "\n"
       "EXT_IN := clj\n"
       "EXT_OUT := " (:target cfg) "\n"
       "SRC_FILES := $(shell find -L $(SRC_DIR) -type f -name '*.$(EXT_IN)')\n"
       "OUT_FILES := $(patsubst $(SRC_DIR)/%.$(EXT_IN),$(OUT_DIR)/%.$(EXT_OUT),$(SRC_FILES))\n\n"
       "all: $(OUT_FILES)\n\n"
       "$(OUT_DIR)/%.$(EXT_OUT): $(SRC_DIR)/%.$(EXT_IN)\n"
       "\t@ mkdir -p $(dir $@)\n"
       "\t~/Projects/language/_build/default/bin/main.exe -log true -target " (:target cfg) " -src $(realpath $<) -root $(realpath " (:root cfg) ") -namespace " (:namespace cfg) " > $@\n"
      ;;
       )))
   (reduce
    (fn [a x] (str a x))
    "# GENERATED FILE - DO NOT EDIT\n\n")))
