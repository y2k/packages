(defn default []
  "#!/bin/bash\nset -e\nset -u\nset -o pipefail\nexport OCAMLRUNPARAM=b\n")

(defn build-files [{target :target root-ns :root-ns rules :rules}]
  (let [res (map
             (fn [r]
               (str "clj2js compile"
                    (if (some? root-ns)
                      (str " -root_ns " root-ns)
                      "")
                    " -target " target
                    " -src " (:src r) " > " (:target r)))
             rules)]
    (reduce (fn [a x] (str a "\n" x)) "" res)))

(defn build-java-package [{root-ns :root-ns target-dir :target-dir items :items}]
  (build-files
   {:target "java"
    :root-ns root-ns
    :rules (map
            (fn [x]
              {:src (str root-ns "/" x ".clj")
               :target (str target-dir "/" root-ns "/" x ".java")})
            items)}))
