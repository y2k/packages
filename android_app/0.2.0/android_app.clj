(ns android_app
  (:require ["../chat_ui/chat_ui" :as ui]
            ["../../main" :as m])
  (:import [android.app Activity]
           [android.content Intent]
           [android.view View]
           [android.os Bundle]))

(gen-class
 :name MainActivity
 :extends Activity
 :prefix "activity_"
 :fields ["w_atom"]
 :init "init"
 :methods [[^Override onCreate [Bundle] void]
           [^Override onActivityResult [int int Intent] void]])

(defn- activity_init [^MainActivity self]
  (set! (.-w_atom self) (atom {})))

(defn- activity_onCreate [^MainActivity self ^Bundle bundle]
  (let [root (cast View (ui/root_ self))
        w_atom (.-w_atom self)]
    (.setContentView self root)

    (.setOnApplyWindowInsetsListener
     root
     ^android.view.View.OnApplyWindowInsetsListener
     (fn [view insets]
       (let [systemBar (.getInsets insets (android.view.WindowInsets.Type.systemBars))]
         (.setPadding
          root
          (.-left systemBar) (.-top systemBar) (.-right systemBar) (.-bottom systemBar))
         insets)))

    (ui/add_effect_handlers self root w_atom)
    (m/main :on-create
            {:context self
             :state w_atom})))

(defn- activity_onActivityResult [^MainActivity self ^int requestCode ^int resultCode ^Intent data]
  (m/main :on-activity-result
          {:context self
           :state (.-w_atom self)
           :request-code requestCode
           :result-code resultCode
           :data data}))
