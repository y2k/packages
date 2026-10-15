#!/bin/sh
set -eu

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
cd "$work"
work=$(pwd -P)
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
    if [ "$target" = js ]; then
        cat > src/sample.clj <<'EOF'
(ns sample)
(defn fail []
  (.parse JSON "invalid-json"))
(fail)
EOF
        make -B build
        node --input-type=module - "$work/src/sample.clj" <<'EOF'
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
const source = process.argv[2];
const js = readFileSync('out/sample.js', 'utf8');
const encoded = js.match(/sourceMappingURL=data:application\/json;base64,([^\s]+)/);
assert.ok(encoded, 'inline source map');
const map = JSON.parse(Buffer.from(encoded[1], 'base64').toString());
assert.deepEqual(map.sources, [source]);
assert.deepEqual(map.sourcesContent, [readFileSync(source, 'utf8')]);
const result = spawnSync(process.execPath, ['--enable-source-maps', 'out/sample.js'], { encoding: 'utf8' });
assert.notEqual(result.status, 0);
assert.match(result.stderr, /SyntaxError:/);
const frame = result.stderr.split('\n').map(line => line.trim())
  .find(line => line.startsWith('at ') && /sample\.(clj|js):/.test(line));
assert.ok(frame?.endsWith(source + ':3:3)') || frame?.endsWith(source + ':3:3'), result.stderr);
EOF
    fi
    make clean
done
