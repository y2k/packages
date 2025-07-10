(def- symbol_regex (re-pattern "^[a-zA-Z_][a-zA-Z0-9_]*$"))

(defn to_string [x]
  (cond
    (nil? x) "nil"
    (fn? x) "nil"
    (number? x) (str x)
    (boolean? x) (if x "true" "false")
    (string? x) (if (.test symbol_regex x)
                  (str ":" x)
                  (str "\"" x "\""))
    (vector? x)
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
                         (let [value (to_string n)]
                           (if (= value "nil")
                             a
                             (if (= "" a)
                               (str (to_string k) " " value)
                               (str a " " (to_string k) " " value)))))
                       ""
                       x)
               "}")))
