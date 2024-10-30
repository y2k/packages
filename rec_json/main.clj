(defn parse [x]
  (cond
    (= null x) x
    (Array.isArray x) (.map x parse)
    (= (type x) "object") (and x (-> (Object.entries x)
                                     (.reduce (fn [a x] (assoc a (get x 0) (parse (get x 1)))) {})))
    (= (type x) "string") (if (.startsWith x "{") (parse (JSON.parse x)) x)
    :else x))

(defn stringify [x replacer space]
  (->
   (parse x)
   (JSON.stringify replacer space)))
