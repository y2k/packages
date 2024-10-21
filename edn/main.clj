(def- symbol_regex (RegExp. "^[a-zA-Z_][a-zA-Z0-9_]*$"))

(defn to_string [x]
  (cond
    (nil? x) "null"
    (quote? x) (.-value x)
    (number? x) (str x)
    (string? x) (if (.test symbol_regex x)
                  (str ":" x)
                  (str "\"" x "\""))

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
