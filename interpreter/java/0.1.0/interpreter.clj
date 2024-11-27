(ns interpreter (:import [java.util.function Function]))

(defn- ^Function function [^Function f] f)

(defn make_env []
  {:scope
   {:+ (function (fn [[^int a ^int b]] (+ a b)))}})

(defn eval [env node]
  (cond
    (list? node)
    (let [name (first node)
          args (rest node)
          ^Function f (get (:scope env) name)]
      (.apply f args))

    (vector? node) (FIXME)

    (map? node) (FIXME)

    :else node))
