(ns interpreter (:import [java.util.function Function]))

(defn- ^Function function [^Function f] f)

(defn make_env []
  {:scope
   {:+ (function (fn [[a b]]
                   (let [aa (as (if (is a String) (Integer/parseInt (as a String)) a) int)
                         bb (as (if (is b String) (Integer/parseInt (as b String)) b) int)]
                     (+ aa bb))))}})

;; (defn- invoke_ [env name args]
;;   (println "INVOKE:" name args env)
;;   (let [^Function f (get (:scope env) name)]
;;     (if (= null f)
;;       (FIXME name)
;;       (.apply f args))))

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

(defn- eval_do_body [env lexemes]
  ;; (println "EVAL_DO_BODY:" lexemes)
  (let [[r env2 lx2] (eval env lexemes)]
    (if (empty? lx2)
      [r env2 lx2]
      (eval_do_body env2 lx2))))

(defn- get_lambda_body [^int level buffer lx]
  (let [node (first lx)]
    (case node
      "(" (get_lambda_body (+ level 1) (conj buffer node) (rest lx))
      ")" (if (= level 0)
            [buffer lx]
            (get_lambda_body (- level 1) (conj buffer node) (rest lx)))
      (get_lambda_body level (conj buffer node) (rest lx)))))

;; -> [_ Env Lexemes]
(defn eval [env lexemes]
  ;; (println "EVAL:" lexemes "\n" env)
  (let [node (first lexemes)]
    (if (= "(" node)
      (let [[f env2 lexemes2] (eval env (rest lexemes))]
        (case f
          "do" (eval_do_body env2 lexemes2)
          "def" (let [name (first lexemes2)
                      [value env3 lexemes3] (eval env (rest lexemes2))]
                  [null (register_value env3 name value) lexemes3])
          "bind*" (let [name (first lexemes2)
                        [value env3 lexemes3] (eval env (rest lexemes2))]
                    [null (register_value env3 name value) lexemes3])
          ;; Abstraction
          "fn*" (let [[args_names lexemes3] (get_function_args_names lexemes2)
                      [body_lx lx4] (get_lambda_body 0 [] lexemes3)]
                  [(function (fn [args]
                               (let [env3 (merge_args_with_values env2 args_names args)]
                                ;;  (println "CALL LAMBDA:" args env3 body_lx)
                                 (first (eval_do_body env3 body_lx)))))
                   env2
                   lx4])
          ;; Application
          (let [[args lexemes3] (parse_all_args env2 lexemes2)]
            ;; (println "CALL1:" (first (rest lexemes)) args f env2)
            [(if (is f Function)
               (.apply (as f Function) args)
               ;; (invoke_ env2 f args)
               (FIXME f " " args))
             env2
             lexemes3])))
      [(resolve_value env node) env (rest lexemes)])))
