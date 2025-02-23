(ns _)

(defn pure [x] (fn [_] [x nil]))

(defn then [effect f]
  (fn [w]
    (let [[r err] (effect w)]
      (if (some? err)
        [nil err]
        (let [effect2 (f r)]
          (effect2 w))))))

(defn batch [effects]
  (if (empty? effects)
    (pure [])
    (then
     (first effects)
     (fn [hr]
       (then
        (batch (rest effects))
        (fn [tr]
          (pure (concat [hr] tr))))))))

(defn thunk [name args f]
  (fn [w]
    (let [eh (:thunk w)]
      (if (nil? eh) (FIXME "No effect handler for :" name))
      (eh [name args] f))))
