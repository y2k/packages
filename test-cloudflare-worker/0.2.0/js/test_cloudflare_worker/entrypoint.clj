(ns test-cloudflare-worker.entrypoint
  (:require [main :as m]))

(defn- effects-world [effects]
  (js/Proxy.
   {}
   {:get (fn [_target prop _receiver]
           (fn [args]
             (swap! effects (fn [items] (conj items (assoc args :type prop))))
             (Promise/resolve nil)))}))

(export-default
 {:fetch (fn [request env ctx]
           (let [effects (atom [])]
             (-> ((m/handle-fetch request env ctx) (effects-world effects))
                 (.then (fn [response] (.text response)))
                 (.then
                  (fn [body]
                    (Response.
                     (.stringify js/JSON {:effects (deref effects)
                                          :response body})
                     {:headers {"Content-Type" "application/json"}}))))))})
