(ns random)

;; Version: 0.2.0

(defn next [^double prev]
  (let [a 1664525.0
        c 1013904223.0
        m 2147483647.0 ;; 2^31 - 1
        next (mod (+ (* a m prev) c) m)]
    (/ next m)))

(defn seq [seed ^int count]
  (if (= 0 count) [seed []]
      (let [r (next seed)
            [next_seed result] (seq r (- count 1))]
        [next_seed (conj result r)])))
