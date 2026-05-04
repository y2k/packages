(ns test-cloudflare-worker
  (:require ["wrangler" :as w]
            ["node:test" :as t]
            ["node:assert" :as assert]))

(def worker (atom nil))

(defn before-after [bindings]
  (t/before (fn []
              (-> (w/unstable_startWorker
                   {:entrypoint "test/test_cloudflare_worker/entrypoint.js"
                    :config ""
                    :compatibilityDate "2026-03-30"
                    :compatibilityFlags ["nodejs_compat"]
                    :bindings bindings})
                  (.then (fn [started-worker]
                           (reset! worker started-worker))))))
  (t/after (fn [] (after))))

(defn before [bindings]
  (-> (w/unstable_startWorker
       {:entrypoint "test/test_cloudflare_worker/entrypoint.js"
        :config ""
        :compatibilityDate "2026-03-30"
        :compatibilityFlags ["nodejs_compat"]
        :bindings bindings})
      (.then (fn [started-worker]
               (reset! worker started-worker)))))

(defn after []
  (if (deref worker)
    (.dispose (deref worker))
    nil))

(defn test [title req expected]
  (t/test title
          (create-assert-fetch-snapshot req expected)))

(defn create-assert-fetch-snapshot [request expected-base64]
  (fn []
    (-> (if (= request.method "GET")
          (Promise/resolve nil)
          (.text request))
        (.then (fn [body]
                 (.fetch (deref worker)
                         request.url
                         {:method request.method
                          :headers request.headers
                          :body body})))
        (.then (fn [response] (.json response)))
        (.then (fn [body]
                 (assert-json-snapshot body expected-base64))))))

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
