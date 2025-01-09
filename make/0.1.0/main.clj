(defn- default [] "# THIS FILE IS GENERATED\n\n")

(defn- build-files [{target :target root-ns :root-ns rules :rules}]
  (let [res (map
             (fn [r]
               (str (:target r) ": " (:src r) "\n"
                    "\t@ mkdir -p $$(dirname " (:target r) ")\n"
                    "\t@ clj2js compile"
                    (if (some? root-ns)
                      (str " -root_ns " root-ns)
                      "")
                    " -target " target
                    " -src " (:src r) " > " (:target r)))
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
              {:src (str "vendor/" (:name x) "/" (:version x) "/main.clj")
               :target (str (:target-dir params) "/" (:name x) "/main.js")})))})

(defn- make_all_target [xs]
  (reduce
   (fn [acc x]
     (str acc
          (reduce
           (fn [ac2 r] (str ac2 " " (:target r)))
           ""
           (:rules x))))
   "all:"
   xs))

(defn generate [xs]
  (reduce
   (fn [acc x]
     (str acc (build-files x)))
   (str
    (default)
    (make_all_target xs))
   xs))
