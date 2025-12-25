(ns effects-promise
  (:require [effects :as e]))

(defn then [fx f]
  (e/then fx
          (fn [result]
            (if (some? result.then)
              (fn [w]
                [(.then result
                        (fn [response]
                          (first ((f response) w))))
                 nil])
              (f response)))))

(defn pure [x]
  (fn [_]
    [(Promise.resolve x) nil]))

(defn batch [effects]
  (if (= (count effects) 0)
    (pure [])
    (then
     (first effects)
     (fn [result]
       (then
        (batch (rest effects))
        (fn [results]
          (pure (concat [result] results))))))))

(defn first_some [effects]
  (if (= (count effects) 0)
    (pure nil)
    (then
     (first effects)
     (fn [result]
       (if (some? result)
         (pure result)
         (first_some (rest effects)))))))
