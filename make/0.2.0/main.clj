(defn- default [] "# THIS FILE IS GENERATED\n\n")

(defn- build-files [{target :target root-ns :root-ns rules :rules}]
  (let [res (map
             (fn [r]
               (str (:target r) ": " (:src r) "\n"
                    "\t@ mkdir -p $$(dirname " (:target r) ")\n"
                    "\t@ clj2js compile"
                    (if (some? (:ns r))
                      (str " -root_ns " (:ns r))
                      (if (some? root-ns)
                        (str " -root_ns " root-ns)
                        ""))
                    " -target " target
                    " -src " (:src r) " > " (:target r)
                    " || (rm " (:target r) " && exit 1)"))
             rules)]
    (reduce (fn [a x] (str a "\n\n" x)) "" res)))

(defn module-files [files] files)

(defn module [{lang :lang root-ns :root-ns src-dir :src-dir target-dir :target-dir items :items}]
  {:target lang
   :root-ns root-ns
   :rules (map
           (fn [x]
             {:src (str src-dir "/" x ".clj")
              :target (str target-dir "/" x "." lang)})
           items)})

(defn vendor [params]
  {:target (:lang params)
   :rules (->>
           (:items params)
           (map
            (fn [x]
              {:src (str "vendor/" (:name x) "/" (:version x) "/"
                         (if (= (:lang params) :java)
                           (:name x)
                           "main")
                         ".clj")
               :ns (if (= (:lang params) :java) (:name x) nil)
               :target (str (:target-dir params) "/" (:name x) "/"
                            (if (= (:lang params) :java)
                              (:name x)
                              "main")
                            "." (:lang params))})))})

(defn- make_all_target [xs]
  (reduce
   (fn [acc x]
     (str acc
          (reduce
           (fn [ac2 r] (str ac2 " " (:target r)))
           ""
           (:rules x))))
   ".PHONY: all\nall:"
   xs))

(defn generate [xs]
  (reduce
   (fn [acc x]
     (str acc (build-files x)))
   (str
    (default)
    (make_all_target xs))
   xs))
