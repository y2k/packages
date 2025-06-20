(ns _ (:import [android.app Activity]
               [android.content Intent Context]
               [android.net Uri]
               [android.os Bundle]
               [android.provider MediaStore]
               [androidx.core.content FileProvider]
               [java.io File]))

(defn on_activity_result [^Context self ^int requestCode ^int resultCode ^Intent data]
  (let [cacheDir (.getCacheDir self)
        temp_file (File. cacheDir "image_fa67e34719fa.jpg")])
  (Uri/fromFile temp_file))

(defn get_image [] (fn [w] ((:android_gallery:get_image w))))

(defn- get_image_ [^Activity activity]
  (let [intent (Intent. MediaStore/ACTION_IMAGE_CAPTURE)
        cacheDir (.getCacheDir activity)
        temp_file (File. cacheDir "image_fa67e34719fa.jpg")
        photoUri (FileProvider/getUriForFile  activity "y2k.finance_tracker.fileprovider" temp_file)]
    (.putExtra intent MediaStore/EXTRA_OUTPUT photoUri)
    (.startActivityForResult activity intent 9146235)
    nil))

(defn attach_effect_handler [^Activity activity env]
  (assoc env :android_gallery:get_image (fn [] [(get_image_ activity) nil])))
