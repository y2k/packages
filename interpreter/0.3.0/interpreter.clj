(ns _ (:import [java.util.function Function]))

;; Recursive Descent Parser

(declare parse)

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
    (= token "nil") nil
    :else token))

(defn- parse [tokens ^int index]
  (case (get tokens index)
    "(" (parse_list tokens (+ index 1))
    ")" (FIXME index tokens)
    [(parse_atom (as (get tokens index) String)) (+ index 1)]))

;; Scope

(def- gensym_atom (atom 0))

(defn make_env [scope]
  {:ns {}
   :scope
   (merge
    {:gensym (fn [args]
               (let [id (swap! gensym_atom (fn [x] (+ (as x int) 1)))]
                 (str "G__" id)))
     :+ (fn [[a b]]
          (let [aa (as (if (is a String) (Integer/parseInt (as a String)) a) int)
                bb (as (if (is b String) (Integer/parseInt (as b String)) b) int)]
            (+ aa bb)))
     := (fn [[a b]] (= a b))
     :get (fn [[xs i]] (get xs i))
     :vector (fn [xs] xs)
     :atom (fn [[x]] (atom x))
     :deref (fn [[x]] (deref x))
     :reset! (fn [[a x]] (reset! a x) x)
     :println (fn [xs] (println (into-array2 (.-class Object) xs)))
     :str (fn [xs] (str (into-array2 (.-class Object) xs)))
     :hash-map (fn [xs] (hash-map (into-array2 (.-class Object) xs)))
     :reduce (fn [[f def_ xs]] (reduce (fn [acc x] (f [acc x])) def_ xs))
     :empty? (fn [[x]] (empty? x))}
    scope)})

(defn- resolve_value [env ^String name]
  ;; (println "RESOLVE:" name env)
  (cond
    (.matches name "-?\\d+(\\.\\d+)?") (Integer/parseInt name)
    (.startsWith name "\"") (unescape (.substring name 1 (- (.length name) 1)))
    (.startsWith name ":") (.substring name 1 (.length name))
    :else (if (contains? (:scope env) name)
            (get (:scope env) name)
            (if (contains? (:ns env) name)
              (deref (get (:ns env) name))
              (FIXME "Can't resolve '" name "' | SCOPE: " (map (fn [[k]] k) (:scope env)))))))

(defn- register_value [env name value]
  ;; (println "REGISTER:" name value)
  (assoc env :scope (assoc (:scope env) name value)))

;; Namespace definitions

(defn- register_def [env name value]
  ;; (println "REGISTER:" name value)
  (assoc env :ns (assoc (:ns env) name value)))

(defn- scope_contains [env name]
  ;; (println "SCOPE_CONTAINS:" name env)
  (not= nil (get (:ns env) name)))

;; Eval

(defn- merge_args_with_values [env args_names args]
  ;; (println "MERGE:" args_names args)
  (if (empty? args_names)
    env
    (merge_args_with_values
     (register_value env (first args_names) (first args))
     (rest args_names)
     (rest args))))

(declare rec_eval)

(defn eval [env lexemes]
  (let [[sexp] (parse lexemes 0)]
    (rec_eval env sexp)))

(defn- eval_do_body [env sexps]
  (let [[node] sexps
        tail (rest sexps)
        [r env2] (rec_eval env node)]
    (if (empty? tail)
      [r env2]
      (eval_do_body env2 tail))))

(defn- eval_arg [env xs]
  ;; (println "EVAL_ARG:" xs)
  (if (empty? xs)
    []
    (let [arg (first xs)
          x (first (rec_eval env arg))]
      ;; (println "arg:" arg)
      (concat [x] (eval_arg env (rest xs))))))

(defn- rec_eval [env sexp]
  ;; (println "EVAL:" sexp env)
  (cond
    (= sexp nil) [nil env]
    (is sexp String) [(resolve_value env (as sexp String)) env]
    (vector? sexp) (let [^String name (first sexp)]
                     (case name
                       "do*" (eval_do_body env (rest sexp))
                       "def*" (let [[_ dname] sexp]
                                (if (= 3 (count sexp))
                                  (let [value_atom (atom nil)
                                        env2 (register_def env dname value_atom)
                                        [value] (rec_eval env2 (get sexp 2))]
                                    (reset! value_atom value)
                                    [true env2])
                                  [(scope_contains env dname) env]))
                       "let*" (let [[_ dname] sexp]
                                [nil (register_value env dname (first (rec_eval env (get sexp 2))))])
                       "if*" (let [[cond env2] (rec_eval env (second sexp))]
                              ;; (println "IF:" cond sexp)
                               (if (as cond boolean)
                                 (rec_eval env2 (get sexp 2))
                                 (rec_eval env2 (get sexp 3))))
                       "fn*" [(fn [args]
                                (let [[_ args_names body] sexp]
                                  ;; (println "FN*" args_names args env)
                                  (first
                                   (rec_eval
                                    (merge_args_with_values env args_names args)
                                    body))))
                              env]
                       (let [f (as (resolve_value env name) Function)]
                         [(.apply f (eval_arg env (rest sexp))) env])))
    :else (FIXME sexp)))
