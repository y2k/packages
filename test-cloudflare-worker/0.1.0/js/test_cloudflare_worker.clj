(ns test-cloudflare-worker
  (:require [main :as m]
            ["node:assert" :as assert]))

(defn create-assert-fetch-snapshot [request expected-base64]
  (fn []
    (let [effects (atom [])]
      (-> (run-with-mocks effects request {:TG_TOKEN "test-token" :TELEGRAM_CHAT_ID "test-chat"})
          (.then (fn [response] (.text response)))
          (.then
           (fn [body]
             (assert-json-snapshot
              {:effects (deref effects)
               :response body}
              expected-base64)))))))

(defn- run-with-mocks [effects request env]
  ((m/handle-fetch request env nil)
   (js/Proxy.
    {}
    {:get (fn [_target prop _receiver]
            (fn [args]
              (swap! effects (fn [items] (conj items (assoc args :type prop))))
              (Promise/resolve nil)))})))

(defn- base64-encode [text]
  (-> (.from js/Buffer text)
      (.toString "base64")))

(defn- base64-decode [text]
  (-> (.from js/Buffer text "base64")
      (.toString "utf8")))

(defn- assert-json-snapshot [actual expected-base64]
  (let [actual-json (.stringify js/JSON actual)
        actual-base64 (base64-encode actual-json)]
    (assert/deepStrictEqual
     (JSON/parse actual-json)
     (JSON/parse (base64-decode expected-base64))
     actual-base64)))
