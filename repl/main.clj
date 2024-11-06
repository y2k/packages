(ns _ (:require ["../edn/main" :as edn]
                [js.fs.promises :as fs]))

(def- GEN_FILE_NAME "generated_repl_client.clj")

(defn- get_files []
  (->
   (fs/readdir (get process.argv 2))
   (.then (fn [xs]
            (->
             xs
             (.filter (fn [f] (and
                               (.endsWith f ".clj")
                               (not= GEN_FILE_NAME f))))
             (.map (fn [f] {:path f :name (.replace f ".clj" "")})))))))

(defn- generate_code_file [files]
  (str
   (edn/to_string
    (list 'ns '_
          (list
           (spread
            (concat
             (list :require ["../vendor/packages/repl/client_runtime" :as 'cr])
             (.map
              files
              (fn [x]
                [(str "../src/" (:name x)) :as (quote_of_string (:name x))])))))))
   "\n"
   (edn/to_string
    (list 'defn 'route ['request]
          (list 'cr/route
                (.reduce files
                         (fn [a x] (assoc a (:name x) (quote_of_string (:name x))))
                         {})
                'request)))))

(->
 (get_files)
 (.then (fn [x]
          (fs/writeFile
           (str (get process.argv 2) "/" GEN_FILE_NAME)
           (generate_code_file x)
           "utf-8"))))
