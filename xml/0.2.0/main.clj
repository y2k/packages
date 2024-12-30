(defn- attrs_to_string [attrs]
  (reduce
   (fn [acc [k v]]
     (str acc " " k "='" v "'"))
   ""
   attrs))

(defn to_string [node]
  (if (vector? node)
    (let [tag (get node 0)]
      (if (= 1 (count node))
        (str "<" tag "></" tag ">")
        (if (map? (get node 1))
          (str "<" tag " " (attrs_to_string (get node 1)) ">"
               (reduce
                (fn [a x] (str a (to_string x)))
                ""
                (drop 2 node))
               "</" tag ">")
          (str "<" tag ">"
               (reduce
                (fn [a x] (str a (to_string x)))
                ""
                (drop 1 node))
               "</" tag ">"))))
    (str node)))
