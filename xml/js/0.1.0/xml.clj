(defn to_string [node]
  (let [tag (.at node 0)
        attrs (.at node 1)
        has_attrs (and (> node.length 1) (= (type (.at node 1)) "object") (not (Array.isArray (.at node 1))))]
    (if (= (type node) :string)
      node
      (str "<" tag " "
           (if (not has_attrs) ""
               (->
                (Object.entries attrs)
                (.reduce (fn [a x] (str a " " (.at x 0) "='" (.at x 1) "'")) ""))) ">"
           (->
            (.slice node (if has_attrs 2 1))
            (.map to_string)
            (.reduce (fn [a x] (str a x)) ""))
           "</" tag ">"))))
