(ns _ (:import [java.net ServerSocket Socket])
    (:require ["../effects/effects" :as e]))

(defn write [socket ^"byte[]" data options]
  (e/thunk :write {:data data :options options}
           (fn []
             (.write (.getOutputStream (as socket Socket)) data)
             (if (:close options)
               (do
                 (.close (as socket Socket))
                 nil)))))

(defn read [server]
  (e/thunk :read_from_server_socket nil
           (fn []
             (let [socket (.accept (as server ServerSocket))
                   in (.getInputStream socket)
                   result (.readAllBytes in)]
               [result socket]))))
