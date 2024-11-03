(defn parse_response [response configure_rewriter]
  (let [rewriter (HTMLRewriter.)
        config (configure_rewriter rewriter)]
    (->
     (Promise.resolve response)
     (.then (fn [res] (.transform rewriter res)))
     (.then (fn [x] (.arrayBuffer x)))
     (.then (fn [] ((:decode config)))))))

(defn parse_string [body configure_rewriter]
  (parse_response (Response. body) configure_rewriter))
