(ns _ (:import [io.requery.android.database.sqlite SQLiteDatabase]))

(defn query [sql args]
  (fn [w] ((:android_db:query w) {:sql sql :args args})))

(defn- query_ [db sql args]
  (let [c (.rawQuery (cast SQLiteDatabase db)
                     (cast String (:sql sql))
                     (into-array2 String (:args args)))
        result (if (.moveToFirst c) (.getString c 0) nil)]
    result))

(defn attach_effect_handler [{db_source :db} env]
  (let [^SQLiteDatabase db (SQLiteDatabase/openOrCreateDatabase (cast String db_source) nil)]
    (assoc env :android_db:query (fn [{sql :sql args :args}] (query_ db sql args)))))
