(ns interpreter (:import [java.util.function Function]))

;; Recursive Descent Parser

(defn- parse [tokens ^int index]
  (case (get tokens index)
    "(" (parse_list tokens (+ index 1))
    ")" (FIXME index tokens)
    [(parse_atom (as (get tokens index) String)) (+ index 1)]))

(defn- parse_list [tokens ^int index]
  (let [token (get tokens index)]
    (if (= token ")")
      [[] (+ index 1)]
      (let [[first ^int next_index] (parse tokens index)
            [rest ^int final_index] (parse_list tokens next_index)]
        [(concat [first] rest) final_index]))))

(defn- parse_atom [^String token]
  (cond
    (= token "true") true
    (= token "false") false
    (= token "nil") null
    ;; (.startsWith token "\"") (.substring token 1 (- (.length token) 1))
    :else token))

;; Scope

(defn make_env [scope]
  {:scope
   (merge
    {:+ (function (fn [[a b]]
                    (let [aa (as (if (is a String) (Integer/parseInt (as a String)) a) int)
                          bb (as (if (is b String) (Integer/parseInt (as b String)) b) int)]
                      (+ aa bb))))
     :get (function (fn [[xs i]] (get xs i)))
     :vector (function (fn [xs] xs))
     :atom (function (fn [[x]] (atom x)))
     :deref (function (fn [[x]] (deref x)))
     :reset! (function (fn [[a x]] (reset! a x) x))
     :str (function (fn [xs] (str (into-array2 (.-class Object) xs))))
     :hash-map (function (fn [xs] (hash-map (into-array2 (.-class Object) xs))))}
    scope)})

(defn- resolve_value [env ^String name]
  ;; (println "RESOLVE:" name env)
  (cond
    (.matches name "-?\\d+(\\.\\d+)?") (Integer/parseInt name)
    (.startsWith name "\"") (.substring name 1 (- (.length name) 1))
    (.startsWith name ":") (.substring name 1 (.length name))
    :else (let [r (get (:scope env) name)]
            (if (= null r)
              (FIXME name " | " env)
              r))))

(defn- register_value [env name value]
  ;; (println "REGISTER:" name value)
  (assoc env :scope (assoc (:scope env) name value)))

(defn- scope_contains [env name]
  ;; (println "SCOPE_CONTAINS:" name env)
  (not= null (get (:scope env) name)))

;; Eval

(defn- merge_args_with_values [env args_names args]
  ;; (println "MERGE:" args_names args)
  (if (empty? args_names)
    env
    (merge_args_with_values
     (register_value env (first args_names) (first args))
     (rest args_names)
     (rest args))))

(defn eval [env lexemes]
  (let [sexp (first (parse lexemes 0))]
    (rec_eval env sexp)))

(defn eval_do_body [env sexps]
  (let [node (first sexps)
        tail (rest sexps)
        [r env2] (rec_eval env node)]
    (if (empty? tail)
      [r env2]
      (eval_do_body env2 tail))))

(defn eval_arg [env xs]
  (if (empty? xs)
    []
    (let [arg (first xs)]
      (concat [(first (rec_eval env arg))] (eval_arg env (rest xs))))))

(defn- rec_eval [env sexp]
  ;; (println "EVAL:" sexp env)
  (cond
    (= sexp null) [null env]
    (is sexp String) [(resolve_value env (as sexp String)) env]
    (vector? sexp) (let [^String name (first sexp)]
                     (case name
                       "do" (eval_do_body env (rest sexp))
                       "def" (let [dname (second sexp)]
                               (if (= 3 (count sexp))
                                 [true (register_value env dname (first (rec_eval env (get sexp 2))))]
                                 [(scope_contains env dname) env]))
                       "let*" (let [dname (second sexp)]
                                [null (register_value env dname (first (rec_eval env (get sexp 2))))])
                       "if" (let [[cond env2] (rec_eval env (second sexp))]
                              ;; (println "IF:" cond sexp)
                              (if (as cond boolean)
                                (rec_eval env2 (get sexp 2))
                                (rec_eval env2 (get sexp 3))))
                       "fn*" [(function (fn [args]
                                          (let [args_names (second sexp)]
                                            ;; (println "FN*" args_names args env)
                                            (first
                                             (eval_do_body
                                              (merge_args_with_values env args_names args)
                                              (rest (rest sexp)))))))
                              env]
                       (let [f (as (resolve_value env name) Function)]
                         [(.apply f (eval_arg env (rest sexp))) env])))
    :else (FIXME sexp)))
