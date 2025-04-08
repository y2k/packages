(defn- default [] "# THIS FILE IS GENERATED\n\n")

(defn- build-files [{target :target root-ns :root-ns rules :rules}]
  (let [res (map
             (fn [r]
               (str (:target r) ": " (:src r) "\n"
                    "\t@ mkdir -p $$(dirname " (:target r) ")\n"
                    "\t@ OCAMLRUNPARAM=b clj2js compile"
                    (if (some? (:ns r))
                      (str " -root_ns " (:ns r))
                      (if (some? root-ns)
                        (str " -root_ns " root-ns)
                        ""))
                    (if (some? (:no_lint r))
                      " -no_lint true"
                      "")
                    " -target " target
                    " -src " (:src r) " > " (:target r)

                    " || (rm " (:target r) " && exit 1)"))
             rules)]
    (reduce (fn [a x] (str a "\n\n" x)) "" res)))

(defn module-files [files] files)

(defn module [{lang :lang
               root-ns :root-ns
               src-dir :src-dir
               target-dir :target-dir
               items :items
               no_lint :no_lint}]
  {:target lang
   :root-ns root-ns
   :rules (map
           (fn [x]
             {:src (str src-dir "/" x ".clj")
              :no_lint no_lint
              :target (str target-dir "/" x "." lang)})
           items)})

(defn vendor [params]
  {:target (:lang params)
   :rules (->>
           (:items params)
           (map
            (fn [{name :name version :version}]
              (if (= (:lang params) :java)
                {:src (str "vendor/" name "/" version "/" name ".clj")
                 :ns name
                 :target (str (:target-dir params) "/" name "/" name "." (:lang params))}
                {:src (str "vendor/" name "/" version "/main.clj")
                 :target (str (:target-dir params) "/" name "/main." (:lang params))}))))})

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
