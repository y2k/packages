(ns _ (:import [android.widget LinearLayout Button TextView]
               [android.view View ViewGroup]
               [android.content Context])
    (:require ["../effects/effects" :as e]))

(defn ^View root_ [^Context context]
  (let [ll (LinearLayout. context)]
    (.setOrientation ll 1)
    (.setGravity ll 80)
    (.setPadding ll 0 0 0 150)
    ll))

(defn- ^View column_ [^Context context]
  (let [ll (LinearLayout. context)]
    (.setOrientation ll 1)
    ll))

(defn- ^View row_ [^Context context]
  (let [ll (LinearLayout. context)]
    (.setGravity ll 8388613)
    ll))

(defn- add_ [parent child]
  (.addView (cast ViewGroup parent) (cast View child))
  parent)

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
    view))

(defn- button [props]        (fn [w] ((:chat_ui:button w) props)))
(defn- add    [parent child] (fn [w] ((:chat_ui:add w) parent child)))
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

(defn- build_ui [ui_desc]
  (let [[name props] ui_desc
        children (drop 2 ui_desc)]
    (case name
      :label (label props)
      :button (button props)
      :column (fill_container (fn [x] (build_ui x)) (column) children)
      :row (fill_container (fn [x] (build_ui x)) (row) children)
      (FIXME "Unknown UI component: " name))))

;;

(defn- update_ [v] (fn [w] ((:chat_ui:update w) v)))

(defn update_ui [vui]
  (e/then
   (build_ui vui)
   (fn [v] (update_ v))))

;;

(defn add_effect_handlers [^Context self root w_atom]
  (swap! w_atom
         (fn [w]
           (merge w
                  {:chat_ui:add (fn [parent child] [(add_ parent child) nil])
                   :chat_ui:button (fn [props]
                                     [(button_
                                       self
                                       (assoc props :onclick (fn [_] ((:onclick props) (deref w_atom)))))
                                      nil])
                   :chat_ui:label (fn [props] [(label_ self props) nil])
                   :chat_ui:update (fn [v] [(add_ root v) nil])
                   :chat_ui:column (fn [] [(column_ self) nil])
                   :chat_ui:row (fn [] [(row_ self) nil])})))
  w_atom)
