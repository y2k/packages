(ns android-build)

(defn dependencies [items]
  (str "dependencies {
"
       (reduce (fn [result dependency]
                 (str result "    implementation('" dependency "')
"))
               ""
               items)
       "}
"))
