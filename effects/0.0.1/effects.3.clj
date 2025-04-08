(ns effects)

(defn pure [x] (fn [] (Promise.resolve x)))

(defn then [effect f]
  (fn [w]
    (let [promise (effect w)]
      (.then
       promise
       (fn [result]
         ((f result) w))))))

(defn batch [effects]
  (fn [w]
    (cond
      (= (.-length effects) 0) (Promise.resolve [])
      :else (.then
             ((first effects) w)
             (fn [result]
               (.then
                ((batch (rest effects)) w)
                (fn [xs] (concat [result] xs))))))))
