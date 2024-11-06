(ns _ (:require [js.readline/promises :as readline]))

(defn- call_command [cmd]
  (let [parsed_cmd (.split cmd " ")]
    (->
     (fetch
      "http://localhost:8787/call"
      {:method "POST"
       :body (JSON.stringify {:module (first parsed_cmd)
                              :name (second parsed_cmd)
                              :args (rest (rest parsed_cmd))})})
     (.then (fn [r] (.text r)))
     (.then (fn [r] (println r))))))

(defn- repl_loop []
  (let [rl (readline/createInterface
            {:input process.stdin
             :output process.stdout})]
    (->
     (.question rl "> ")
     (.then (fn [answer]
              (.close rl)
              (call_command answer)))
     (.then (fn [] (repl_loop))))))

(repl_loop)
