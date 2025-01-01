(defn default []
    "#!/bin/bash\nset -e\nset -u\nset -o pipefail\nexport OCAMLRUNPARAM=b\n")

  (defn build [target root_ns rules]
    (let [res (map
               (fn [r]
                 (str "clj2js compile"
                      " -root_ns " root_ns
                      " -target " target
                      " -src " (:src r) " > " (:target r)))
               rules)]
      (reduce (fn [a x] (str a "\n" x)) "" res)))
