(defn fetch [url props]
  (fn [env]
    ((:fetch:fetch env) {:url url :props props})))

(defn attach_effect_handler [env]
  (assoc env :fetch:fetch
         (fn [{url :url props :props}]
           [(.then (globalThis.fetch url props)
                   (fn [response]
                     (case (:decoder props)
                       :json (.json response)
                       :text (.text response)
                       response)))
            nil])))

(defn attach_env [env_map env_names env]
  (assoc
   env :fetch:fetch
   (fn [args]
     ((:fetch:fetch env)
      (assoc args :url
             (reduce
              (fn [url name] (.replace url (str "~" name "~") (get env_map name)))
              (:url args)
              env_names))))))

(defn attach_decoder [env]
  (assoc
   env :fetch:fetch
   (fn [args]
     [(.then
       (first ((:fetch:fetch env) args))
       (fn [resp]
         (if (and (= :json (:decoder (:props args))) (some? (:mapper (:props args))))
           (Promise.resolve ((:mapper (:props args)) resp))
           (Promise.resolve resp))))
      nil])))

(defn attach_log [env]
  (assoc
   env :fetch:fetch
   (fn [args]
     (eprintln "[REQUEST]" args)
     [(.then
       (first ((:fetch:fetch env) args))
       (fn [resp]
         (eprintln "[RESPONSE]" (JSON.stringify resp nil 2))
         (Promise.resolve resp)))
      nil])))
