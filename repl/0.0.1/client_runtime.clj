(defn- call_fun_by_name [module name args]
  ((get module name) (spread args)))

(defn route [modules request]
  (.then
   (.json request)
   (fn [config]
     (let [result (call_fun_by_name (get modules (:module config)) (:name config) (:args config))]
       (println result)
       (Response. result)))))
