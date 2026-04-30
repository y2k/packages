(ns fetch)

(defn fetch [url props]
  (fn [env]
    ((:effects-promise.fetch:fetch env) {:url url :props props})))

(defn- decode_response [response props]
  (case (:decoder props)
    :json (.json response)
    :text (.text response)
    response))

(defn with-fetch [env]
  (assoc
   env
   :effects-promise.fetch:fetch
   (fn [{url :url props :props}]
     (.then (globalThis.fetch url props)
            (fn [response]
              (decode_response response props))))))
