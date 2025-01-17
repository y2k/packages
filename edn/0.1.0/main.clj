(def- symbol_regex (RegExp. "^[a-zA-Z_][a-zA-Z0-9_]*$"))

(defn to_string [x]
  (cond
    (nil? x) "nil"
    (quote? x) (.-value x)
    (number? x) (str x)
    (boolean? x) (if x "true" "false")
    (string? x) (if (.test symbol_regex x)
                  (str ":" x)
                  (str "\"" x "\""))

    (list? x)
    (str "("
         (reduce (fn [a n]
                   (if (= "" a)
                     (to_string n)
                     (str a " " (to_string n))))
                 ""
                 x)
         ")")

    (seq? x)
    (str "["
         (reduce (fn [a n]
                   (if (= "" a)
                     (to_string n)
                     (str a " " (to_string n))))
                 ""
                 x)
         "]")

    :else (str "{"
               (reduce (fn [a [k n]]
                         (if (= "" a)
                           (str (to_string k) " " (to_string n))
                           (str a " " (to_string k) " " (to_string n))))
                       ""
                       x)
               "}")))