(ns effect-fetch)

(defn fetch [url props]
  (fn [env]
    ((:effects-promise.fetch:fetch env) {:url url :props props})))

(defn- decode_response [response props]
  (case (:decoder props)
    :json (.json response)
    :text (.text response)
    response))

(defn with-fetch [fetch-fn effect]
  (fn [w]
    (effect
     (assoc
      w
      :effects-promise.fetch:fetch
      (fn [{url :url props :props}]
        (.then
         (fetch-fn url props)
         (fn [response]
           (decode_response response props))))))))
