(ns xml)

(defn- attrs_to_string [attrs]
  (reduce
   (fn [acc [k v]]
     (str acc " " k "='" v "'"))
   ""
   attrs))

(defn to-string [node]
  (if (vector? node)
    (let [tag (get node 0)]
      (if (= 1 (count node))
        (str "<" tag "></" tag ">")
        (if (= 2 (count node))
          (str "<" tag " " (attrs_to_string (get node 1)) ">"
               "</" tag ">")
          (str "<" tag " " (attrs_to_string (get node 1)) ">"
               (reduce
                (fn [a x] (str a (to-string x)))
                ""
                (drop 2 node))
               "</" tag ">"))))
    (str node)))
