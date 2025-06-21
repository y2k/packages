(ns _ (:import [com.google.mlkit.vision.barcode BarcodeScanning]
               [com.google.mlkit.vision.barcode.common Barcode]
               [com.google.mlkit.vision.common InputImage]
               [android.content Context]
               [com.google.android.gms.tasks OnSuccessListener OnFailureListener]
               [android.net Uri]))

(defn decode_qr [uri props]
  ;; (eprintln "FIXME: decode_qr:" uri)
  (fn [w] ((:android_qr:recognize w) {:url uri :props props})))

(defn- recognize_ [env_atom context uri {callback :callback}]
  (eprintln "FIXME: recognize_:" uri)
  (let [scanner (BarcodeScanning/getClient)
        image (InputImage/fromFilePath (cast Context context) (cast Uri uri))]
    (->
     (.process scanner image)
     (.addOnSuccessListener
      ^com.google.android.gms.tasks.OnSuccessListener:void
      (fn [barcodes]
        ;; (eprintln "FIXME: onSuccess:" barcodes)
        (let [result (map (fn [x] {:raw-value (.getRawValue (cast Barcode x))}) barcodes)]
          ((callback [result nil]) (deref env_atom)))))
     (.addOnFailureListener
      ^com.google.android.gms.tasks.OnFailureListener:void
      (fn [e]
        ;; (eprintln "FIXME: onFailure:" e)
        ((callback [nil e]) (deref env_atom)))))))

(defn attach_effect_handler [context env_atom]
  (swap! env_atom (fn [env]
                    (assoc env :android_qr:recognize (fn [{uri :url props :props}]
                                                       (recognize_ env_atom context uri props)))))
  env_atom)
