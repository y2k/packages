(ns android-build)

(defn dependencies [items]
  (str "dependencies {
"
       (reduce (fn [result dependency]
                 (str result "    implementation('" dependency "')
"))
               ""
               items)
       "}
"))

(defn build-apk [{:package-id package-id}]
  (str "
IMAGE := y2khub/cljdroid@sha256:f7164ab4817da71aa0c6699ae0b62149f9c550f4fac5f01633bc1e9a372de646

.PHONY: build
build:
\t@mkdir -p .build/cache .build/apk
\t@ly2k --target eval < resources/manifest.clj > .build/bin/app/src/main/AndroidManifest.xml
\t@docker run --rm \\
\t\t-v \"$(CURDIR)/.build/cache:/root/.gradle\" \\
\t\t-v \"$(CURDIR)/.build/bin:/target:ro\" \\
\t\t-v \"$(CURDIR)/.build/apk:/output_apk\" \\
\t\t-e APP_ID=" package-id " \\
\t\t$(IMAGE) build

.PHONY: run
run: build
\tadb install -r .build/apk/app-debug.apk
\tadb shell monkey -p " package-id " -c android.intent.category.LAUNCHER 1
"))
