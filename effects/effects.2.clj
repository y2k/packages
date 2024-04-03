(defn run_effect [fx w] (fx w))

(defn call [key data]
  (fn [env] (env/perform key data)))

(defn pure [x]
  (fn [env] (Promise/resolve x)))

(defn batch [xs]
  (fn [env] (->
             (.map xs (fn [f] (f env)))
             (Promise/all))))

(defn then [fx f]
  (fn [env]
    (let [pr (fx env)]
      (.then
       pr
       (fn [r] (let [r2 (f r)]
                 (r2 env)))))))

(defn seq [fx fx2]
  (fn [env]
    (let [pr (fx env)]
      (.then
       pr
       (fn [r1] (.then (fx2 env) (fn [r2] [r1 r2])))))))

;;

(defn run_io [w f] (f w))

(defn fetch [url props]
  (fn [world]
    (.perform world :fetch {:url url :props props} world)))

(defn database [sql args]
  (fn [world]
    (.perform world :database {:sql sql :args args} world)))

(defn dispatch [key data]  (call :dispatch [key data]))
(defn fork     [fx]        (call :fork     fx))
(defn sleep    [timeout]   (call :sleep    timeout))

(defn next [fx key f]
  (then fx (fn [json] (dispatch key (f json)))))

;;

(defn attach_empty_effect_handler [world]
  (assoc
   world :perform
   (fn [name args]
     (FIXME "Effect [" name "] not handled, args: " (JSON/stringify args)))))

(defn attach_eff [world key eff]
  (assoc
   world :perform
   (fn [name args]
     (if (not= name key)
       (world/perform name args)
       (eff args)))))

(defn attach_log [world]
  (assoc world :perform
         (fn [name args]
           (println "IN:" (JSON/stringify [name args] null 2))
           (.then
            (world/perform name args)
            (fn [result]
              (println "OUT:" (JSON/stringify result null 2))
              result)))))
