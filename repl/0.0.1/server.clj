(ns _ (:require [js.readline/promises :as readline]))

(defn- call_command [env cmd]
  (let [parsed_cmd (.split cmd " ")]
    (->
     (fetch
      "http://localhost:8787/call"
      {:method "POST"
       :body (JSON.stringify {:module (:ns env)
                              :name (first parsed_cmd)
                              :args (rest parsed_cmd)})})
     (.then (fn [r] (.text r)))
     (.then (fn [r] (println r))))))

(defn- repl_loop [env]
  (let [rl (readline/createInterface
            {:input process.stdin
             :output process.stdout})]
    (->
     (.question rl (str (:ns env) "=> "))
     (.then (fn [answer]
              (.close rl)
              (cond
                (.startsWith answer "(ns ") (repl_loop (assoc env :ns (second (.split (.replace answer ")" "") " "))))
                :else (.then (call_command env answer)
                             (fn [] (repl_loop env)))))))))

(repl_loop {:ns "user"})
