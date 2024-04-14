(ns signals (:require [effects :as e]))

(defn signal_sink [name] {:name name})

(defn signal [from name handler]
  {:from from
   :name name
   :handler handler})

(defn create_signal_context [] {:signals []})

(defn attach_signal_context [ctx signal]
  (assoc ctx :signals (conj ctx.signals signal)))

;; (defn attach_dispatch_effect_handler [world ctx]
;;   (let [w2
;;         (e/attach_eff
;;          world
;;          :dispatch
;;          (fn [[signal data]]
;;            (e/run_effect (send_signal ctx signal data) w2)))]
;;     w2))

;; (defn database [sql args]
;;   (fn [world]
;;     (.perform world :database {:sql sql :args args} world)))

(defn- dispatch [ctx fx from]
  ;; (eprintln "[LOG][send_signal]" from.name)
  (fn [world]
    (.perform world :dispatch [fx from] world)))

(defn send_signal [ctx from arg]
      ;; (eprintln "[LOG][send_signal]" from.name)
  (fn [world]
    (.perform world :signal {:from from :arg arg} world)))

(defn send_signal_fx [ctx from arg]
  (->
   ctx.signals
   (.filter (fn [{f :from}] (= f from)))
   (.map (fn [s]
           (let [fx ((:handler s) arg)]
             (dispatch ctx fx s))))
   (e/batch))
  ;; (let [s (.find ctx.signals (fn [{from :from}] (= from signal)))
  ;;       fx ((:handler s) arg)]
  ;;   (->
  ;;    fx
  ;;    (e/next s (fn [x] x))))
  )

(defn attach_dispatch_effect_handler [world ctx]
  (let [w2
        (e/attach_eff
         world
         :dispatch
         (fn [[signal data]]
           (e/run_effect (send_signal ctx signal data) w2)))]
    w2))

;; (defn attach_dispatch_effect_handler [world ctx]
;;   (e/attach_eff
;;    world
;;    :dispatch
;;    (fn [[signal data]]
;;      (e/run_effect (send_signal ctx signal data) world))))
