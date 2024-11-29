(ns main (:import [java.util.function Function]))

(defn- ^Function function [^Function f] f)

(defn make_env []
  {:scope
   {:+ (function (fn [[^String a ^String b]] (+ (Integer/parseInt a) (Integer/parseInt b))))}})

(defn- invoke_ [env name args]
  ;; (println "ENV:" env)
  (let [^Function f (get (:scope env) name)]
    (if (= null f)
      (FIXME name)
      (.apply f args))))

(defn- register_value [env name value]
  ;; (println "REGISTER:" name value)
  (assoc env :scope
         (assoc (:scope env) name value)))

(defn- merge_args_with_values [env args_names args]
  ;; (println "MERGE:" args_names args)
  (if (empty? args_names)
    env
    (merge_args_with_values
     (register_value env (first args_names) (first args))
     (rest args_names)
     (rest args))))

(defn- get_function_args_names [lexemes]
  (let [hd (first lexemes)]
    (case hd
      "[" (get_function_args_names (rest lexemes))
      "]" [[] (rest lexemes)]
      (let [[vs lx2] (get_function_args_names (rest lexemes))]
        [(concat [hd] vs) lx2]))))

(defn- parse_all_args [env lx]
  (if (= ")" (first lx))
    [[] (rest lx)]
    (let [[node env2 lx2] (eval env lx)
          [rest_args lx3] (parse_all_args env2 lx2)]
      [(concat [node] rest_args) lx3])))

(defn- resolve_value [env name]
  ;; (println "RESOLVE:" name env)
  (let [r (get (:scope env) name)]
    (if (= null r) name r)))

;; -> [_ Env Lexemes]
(defn eval [env lexemes]
  (let [node (first lexemes)]
    (if (= "(" node)
      (let [[f env2 lexemes2] (eval env (rest lexemes))]
        (case f
          "def" (let [name (first lexemes2)
                      [value env3 lexemes3] (eval env (rest lexemes2))]
                  [null (register_value env3 name value) lexemes3])
          ;; Abstraction
          "fn*" (let [[args_names lexemes3] (get_function_args_names lexemes2)]
                  [(function (fn [args]
                              ;;  (println "ARGS:" args)
                               (let [env3 (merge_args_with_values env2 args_names args)]
                                ;;  (println "CALL:" env2 env3)
                                 (first (eval env3 lexemes3)))))
                   env2
                   lexemes3])
          ;; Application
          (let [[args lexemes3] (parse_all_args env2 lexemes2)]
            [(if (is f Function)
               (.apply (as f Function) args)
               ;; (invoke_ env2 f args)
               (FIXME f args))
             env2 lexemes3])))
      [(resolve_value env node) env (rest lexemes)])))
