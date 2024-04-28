(defn run_effect [fx a] (fx a))

(defn call [key data]
  (fn [env] (.perform env key data)))

(defn pure [x]
  (fn [env] (.resolve Promise x)))

(defn batch [xs]
  (fn [env] (->
             (.map xs (fn [f] (f env)))
             Promise.all)))

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

(defn fetch [url decoder props]
  (fn [world]
    (.perform world :fetch {:url url :decoder decoder :props props} world)))

(defn database [sql args]
  (fn [world]
    (.perform world :database {:sql sql :args args} world)))

(defn dispatch [key data]  (call :dispatch [key data]))
(defn fork     [fx]        (call :fork     fx))
(defn sleep    [timeout]   (call :sleep    timeout))

(defn broadcast [key fx f]
  (then fx (fn [json] (dispatch key (f json)))))

(defn next [fx key f]
  (then fx (fn [json] (dispatch key (f json)))))

;;

(defn attach_empty_effect_handler [world]
  (assoc
   world :perform
   (fn [name args]
     (FIXME "Effect [" name "] not handled, args: " (.stringify JSON args)))))

(defn attach_eff [world key eff]
  (assoc
   world :perform
   (fn [name args]
     (if (not= name key)
       (.perform world name args)
       (eff args)))))

(defn attach_log [world]
  (assoc world :perform
         (fn [name args]
           (println "IN:" (.stringify JSON [name args] null 2))
           (.then
            (.perform world name args)
            (fn [result]
              (println "OUT:" (.stringify JSON result null 2))
              result)))))