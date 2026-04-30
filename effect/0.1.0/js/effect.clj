(ns effect)

(defn pure [x]
  (fn [_]
    (Promise/resolve x)))

(defn thunk [promise-factory]
  (fn [_]
    (promise-factory)))

(defn then [fx f]
  (fn [ctx]
    (-> (fx ctx)
        (.then (fn [x] ((f x) ctx))))))

(defn recover [fx f]
  (fn [ctx]
    (-> (fx ctx)
        (.catch (fn [err] ((f err) ctx))))))

(defn batch [effects]
  (fn [ctx]
    (reduce (fn [acc fx]
              (.then
               acc
               (fn [result]
                 (.then
                  (fx ctx)
                  (fn [x] (conj result x))))))
            (Promise/resolve [])
            effects)))
