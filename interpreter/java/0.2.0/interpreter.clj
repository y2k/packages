(ns interpreter)

;; Version: 0.5.0

(defn- handle_children [list_to_tree ^int i1 nodes]
  (let [[n1 ^int i2] (list_to_tree i1 nodes)]
    (if (= n1 nil)
      [[] i2]
      (let [[n2 i3] (handle_children list_to_tree i2 nodes)]
        [(concat [n1] n2) i3]))))

(defn- list_to_tree [^int i nodes]
  (let [hd (get nodes i)]
    (cond
      (= hd "(") (handle_children list_to_tree (+ i 1) nodes)
      (= hd ")") [nil (+ i 1)]
      :else [hd (+ i 1)])))

;;
;;
;;

(defn resolve_value [engine ctx name]
  (let [value (get ctx name)]
    (if (some? value)
      value
      ;; (get (deref (:ns engine)) name)
      ((:resolve_name engine) engine name))))

(defn- zipmap_merge [keys values dic]
  (if (empty? keys)
    dic
    (zipmap_merge
     (rest keys)
     (rest values)
     (assoc dic (first keys) (first values)))))

(defn- is_string_node [s]
  ;; (eprintln s) ;; FIXME:
  (string/starts-with? s "\""))

(defn- is_number_node [s]
  (re-find (re-pattern "^\\d+$") s))

;; engine * local_scope * lines -> value * local_scope
(defn- eval [engine ctx sexp]
  ;; (eprintln "SEXP: " sexp)
  (if (vector? sexp)
    (case (first sexp)
      "fn*" [(fn [arg_values]
               (let [arg_names (get sexp 1)
                     ctx2 (zipmap_merge arg_names arg_values ctx)]
                 (first
                  (eval engine ctx2 (get sexp 2)))))
             ctx]
      "if*" [(let [[cond _] (eval engine ctx (get sexp 1))]
               (first
                (if cond
                  (eval engine ctx (get sexp 2))
                  (eval engine ctx (get sexp 3)))))
             ctx]
      "do*" (reduce
             (fn [[_ ctx2] n]
               (eval engine ctx2 n))
             nil
             (rest sexp))
      "let*" (let [name (get sexp 1)
                   ctx2 (assoc ctx name (eval engine ctx (get sexp 2)))]
               [nil ctx2])
      (let [f (resolve_value engine ctx (first sexp))]
         ;; (eprintln "F: " f)
        [(f (map
             (fn [n] (first (eval engine ctx n)))
             (rest sexp)))
         ctx]))
    (cond
      (is_string_node sexp) (let [^int len (count sexp)]
                              [(subs sexp 1 (- len 1)) ctx])
      (is_number_node sexp) [(parse-int sexp) ctx]
      :else [(resolve_value engine ctx sexp) ctx])))

(defn- read_all_lines [^String path]
  ;; (string/split (slurp path) "\n")
  (java.nio.file.Files.readAllLines (java.nio.file.Path.of path)))

(defn- load_code [engine name]
  (let [path (str (:code_dir engine) "/" name ".bin")]
    (->>
     (read_all_lines path)
     (list_to_tree 0)
     (first)
     (eval engine (:ctx engine))
     (first))))

(defn- resolve_name [engine name]
  (let [ns (deref (:ns engine))
        value (get ns name)]
    (if (some? value)
      value
      (let [value2 (load_code engine name)]
        (if (some? value2)
          (do
            (reset! (:ns engine) (assoc ns name value2))
            value2)
          (FIXME "Could not find " name " in " (:code_dir engine)))))))

(defn engine_call [engine name args]
  (let [fun (resolve_name engine name)]
    (fun args)))

;;
;;
;;

;; { :code_dir "" } -> engine
(defn engine_create [opts]
  {:code_dir (:code_dir opts)
   :ns (atom {})
   :resolve_name resolve_name
   :ctx {"true" true
         "false" false
         "vector" (fn [xs] xs)
         "hash-map" (fn [key_values]
                      (hash-map-from key_values))
         "str" (fn [[x]] (str x))
         "+" (fn [[^int a ^int b]] (+ a b))}})
