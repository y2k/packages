#!/bin/sh
set -eu

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
cd "$work"
mkdir -p src
printf '(ns sample)\n(def value 42)\n' > src/sample.clj

for target in java js; do
    ly2k --target eval > Makefile <<EOF
(ns build (:require [make :as make]))
(deps {:make "0.6.0"})
(make/makefile {:target "$target"
                :dirs [{:path "src" :build-dir "out"}]})
EOF
    make build
    case "$target" in
        java) runtime=out/y2k/language/language_runtime.java ;;
        js) runtime=out/language_runtime.js ;;
    esac
    cmp "$LY2K_PACKAGES_DIR/prelude/1.0.0/$target/language_runtime.$target" "$runtime"
    test -s "out/sample.$target"
    make -q build
    make clean
done
