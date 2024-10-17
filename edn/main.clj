(defn to_string [x]
  (cond
    (quote? x) (.-value x)
    (number? x) (str x)
    (string? x) (str "\"" x "\"")

    (list? x)
    (str "("
         (reduce x
                 (fn [a n]
                   (if (= "" a)
                     (to_string n)
                     (str a " " (to_string n))))
                 "")
         ")")

    (seq? x)
    (str "["
         (reduce x
                 (fn [a n]
                   (if (= "" a)
                     (to_string n)
                     (str a " " (to_string n))))
                 "")
         "]")

    :else (str "{"
               (reduce
                x
                (fn [a [k n]]
                  (if (= "" a)
                    (str (to_string k) " " (to_string n))
                    (str a " " (to_string k) " " (to_string n))))
                "")
               "}")))
