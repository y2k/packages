;; Version 0.2.0

(ns _ (:import [android.widget LinearLayout Button TextView ScrollView]
               [android.view View ViewGroup]
               [android.content Context])
    (:require ["../effects/effects" :as e]))

(defn ^View root_ [^Context context]
  (let [scroll (ScrollView. context)
        ll (LinearLayout. context)]
    (.setGravity ll 80)
    (.setOrientation ll 1)
    (.addView scroll ll)
    (.setFillViewport scroll true)
    scroll))

(defn- ^View column_ [^Context context]
  (let [ll (LinearLayout. context)]
    (.setOrientation ll 1)
    ll))

(defn- ^View row_ [^Context context]
  (let [ll (LinearLayout. context)]
    (.setGravity ll 8388613)
    ll))

(defn- button_ [^Context context {title :title onclick :onclick}]
  (let [btn (Button. context)]
    (.setText btn (cast String title))
    (.setOnClickListener
     btn
     ^android.view.View.OnClickListener:void (fn [v]
                                               (onclick)
                                               (.toString v)))
    btn))

(defn- label_ [^Context context {text :text}]
  (let [view (TextView. context)]
    (.setText view (cast String text))
    (.setTextSize view 20)
    (.setGravity view 17) ;; Gravity.CENTER
    view))

(defn- button [props]        (fn [w] ((:chat_ui:button w) props)))
(defn- add    [parent child] (fn [w] ((:chat_ui:add w) {:parent parent :child child})))
(defn- row    []             (fn [w] ((:chat_ui:row w))))
(defn- column []             (fn [w] ((:chat_ui:column w))))
(defn- label  [props]        (fn [w] ((:chat_ui:label w) props)))

;;

(defn- fill_container [build_ui container_fx children]
  (e/then
   container_fx
   (fn [container]
     (e/then
      (e/batch
       (map (fn [x]
              (e/then
               (build_ui x)
               (fn [child] (add container child))))
            children))
      (fn [_] (e/pure container))))))

;;

(defn- build_ui [vui]
  (let [[name props] vui
        children (drop 2 vui)]
    (case name
      :label (label props)
      :button (button props)
      :column (fill_container (fn [x] (build_ui x)) (column) children)
      :row (fill_container (fn [x] (build_ui x)) (row) children)
      (FIXME "Unknown UI component: " name))))

(defn- add_ [parent child]
  (.addView (cast ViewGroup parent) (cast View child))
  parent)

(defn- update_impl [w ^ViewGroup root vui clear]
  (if clear
    (do (.removeAllViews root) nil))
  (let [v (first ((build_ui vui) w))
        result (add_ root v)
        scroll (cast ScrollView (.getParent (cast ViewGroup root)))]
    (.post scroll ^Runnable:void (fn [] (.fullScroll scroll 130))) ;; FOCUS_DOWN
    result))

;;

;; DEPRECATED
(defn update_ui [vui] (fn [w] ((:chat_ui:append w) vui)))

(defn replace [vui] (fn [w] ((:chat_ui:replace w) vui)))
(defn append [vui] (fn [w] ((:chat_ui:append w) vui)))

(defn add_effect_handlers [^Context self root2 w_atom]
  (let [root (.getChildAt (cast ViewGroup root2) 0)]
    (swap! w_atom
           (fn [w]
             (merge w
                    {:chat_ui:replace (fn [vui] [(update_impl (deref w_atom) root vui true) nil])
                     :chat_ui:append (fn [vui] [(update_impl (deref w_atom) root vui false) nil])
                     :chat_ui:add (fn [{parent :parent child :child}] [(add_ parent child) nil])
                     :chat_ui:button (fn [props]
                                       [(button_
                                         self
                                         (assoc props :onclick (fn [] ((:onclick props) (deref w_atom)))))
                                        nil])
                     :chat_ui:label (fn [props] [(label_ self props) nil])
                     :chat_ui:column (fn [] [(column_ self) nil])
                     :chat_ui:row (fn [] [(row_ self) nil])})))
    w_atom))
