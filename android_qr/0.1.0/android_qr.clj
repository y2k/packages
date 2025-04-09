(ns _ (:import [com.google.mlkit.vision.barcode BarcodeScanning]
               [com.google.mlkit.vision.barcode.common Barcode]
               [com.google.mlkit.vision.common InputImage]
               [android.content Context]
               [com.google.android.gms.tasks OnSuccessListener OnFailureListener]
               [android.net Uri]))

(defn decode_qr [uri props]
  (fn [w] ((:android_qr:recognize w) uri props)))

(defn- recognize_ [context uri {callback :callback}]
  (unchecked!
   (let [scanner (BarcodeScanning/getClient)
         image (InputImage/fromFilePath (cast Context context) (cast Uri uri))]
     (->
      (.process scanner image)
      (.addOnSuccessListener
       ^OnSuccessListener:void
       (fn [barcodes]
         (let [result (map (fn [x] {:raw-value (.getRawValue (cast Barcode x))}) barcodes)]
           (callback [result nil])
          ;;  (println "FIXME:barcodes: " (map
          ;;                               (fn [x] {:raw-value (.getRawValue (cast Barcode x))})
          ;;                               barcodes))
                                        )))
      (.addOnFailureListener ^OnFailureListener:void (fn [e]
                                                       (callback [nil e])
                                                      ;;  (println "FIXME:error: " e)
                                                       )))
     nil)))

(defn attach_effect_handler [context env]
  (assoc env :android_qr:recognize (fn [uri props] (recognize_ context uri props))))
