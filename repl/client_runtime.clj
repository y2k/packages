(defn- call_fun_by_name [module name args]
  ((get module name) (spread args)))

(defn route [get_module_by_name request]
  (.then
   (.json request)
   (fn [config]
     (let [result (call_fun_by_name (get_module_by_name (:module config)) (:name config) (:args config))]
       (println result)
       (Response. result)))))
