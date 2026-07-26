(ns android-build-test
  (:require [android-build :as android]))

(deps {:android-build "0.1.0"})

(str (android/dependencies [])
     "--
"
     (android/dependencies ["example:one:1.0"])
     "--
"
     (android/dependencies ["example:one:1.0"
                            "example:two:2.0"])
     "<eof>")
