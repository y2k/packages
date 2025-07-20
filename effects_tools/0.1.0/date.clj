(defn get_now [] (fn [w]
                   ((:now:now w))))

(defn get_unixtime [] (fn [w]
                        ((:now:now w))))

(defn effect_handler [w]
  (assoc w :now:now (fn [] [(unixtime) nil])))

;; Mock

(defn create_mock [time]
  (atom time))

(defn mock_effect_handler [time w]
  (assoc w :now:now (fn [] [(deref time) nil])))

(defn shift [time ^double offset]
  (swap! time (fn [^double t] (+ t offset))))
