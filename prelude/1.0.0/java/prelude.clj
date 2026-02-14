(ns prelude)

(__compiler_emit "
public interface Fn0 {
    public Object invoke() throws Exception;
}

public interface Fn1 extends java.util.function.UnaryOperator<Object> {
    public Object invoke(Object a) throws Exception;

    public default Object apply(Object a) {
        try {
            return invoke(a);
        } catch (Exception e) {
            return throwSilent(e);
        }
    }
}

public interface Fn2 {
    public Object invoke(Object a, Object b) throws Exception;
}

public interface Fn3 {
    public Object invoke(Object a, Object b, Object c) throws Exception;
}

public interface Fn4 {
    public Object invoke(Object a, Object b, Object c, Object d) throws Exception;
}

public static Object fn(Fn0 f) {
    return new Fn0() {
        @Override
        public Object invoke() throws Exception {
            return f.invoke();
        }
    };
}

public static Object fn(Fn1 f) {
    return new Fn1() {

        @Override
        public Object invoke(Object a) throws Exception {
            return f.invoke(a);
        }
    };
}

public static Object fn(Fn2 f) {
    return new Fn2() {

        @Override
        public Object invoke(Object a, Object b) throws Exception {
            return f.invoke(a, b);
        }
    };
}

public static Object fn(Fn3 f) {
    return new Fn3() {

        @Override
        public Object invoke(Object a, Object b, Object c) throws Exception {
            return f.invoke(a, b, c);
        }
    };
}

public static Object fn(Fn4 f) {
    return new Fn4() {

        @Override
        public Object invoke(Object a, Object b, Object c, Object d) throws Exception {
            return f.invoke(a, b, c, d);
        }
    };
}

public static Object invoke(Object f) {
    try {
        return ((Fn0) f).invoke();
    } catch (Exception e) {
        return throwSilent(e);
    }
}

public static Object invoke(Object f, Object a) {
    try {
        return ((Fn1) f).invoke(a);
    } catch (Exception e) {
        return throwSilent(e);
    }
}

public static Object invoke(Object f, Object a, Object b) {
    try {
        return ((Fn2) f).invoke(a, b);
    } catch (Exception e) {
        return throwSilent(e);
    }
}

public static Object invoke(Object f, Object a, Object b, Object c) {
    try {
        return ((Fn3) f).invoke(a, b, c);
    } catch (Exception e) {
        return throwSilent(e);
    }
}

public static Object invoke(Object f, Object a, Object b, Object c, Object d) {
    try {
        return ((Fn4) f).invoke(a, b, c, d);
    } catch (Exception e) {
        return throwSilent(e);
    }
}

public static Object apply(Object f, Object args) {
    java.util.List<?> argList = (java.util.List<?>) args;
    int size = argList.size();
    try {
        switch (size) {
            case 0:
                return ((Fn0) f).invoke();
            case 1:
                return ((Fn1) f).invoke(argList.get(0));
            case 2:
                return ((Fn2) f).invoke(argList.get(0), argList.get(1));
            case 3:
                return ((Fn3) f).invoke(argList.get(0), argList.get(1), argList.get(2));
            case 4:
                return ((Fn4) f).invoke(argList.get(0), argList.get(1), argList.get(2), argList.get(3));
            default:
                throw new RuntimeException(\"apply: too many arguments (\" + size + \")\");
        }
    } catch (Exception e) {
        return throwSilent(e);
    }
}
")

(defn nop [] nil)

(__compiler_emit "
public static boolean toBoolean(Object x) {
    if (x instanceof Boolean) {
        return (Boolean) x;
    }
    return x != null;
}")

(__compiler_emit "
public static Object eprintln(Object... xs) {
    for (Object x : xs) {
        System.err.print(x);
        System.err.print(\" \");
    }
    System.err.println();
    return null;
}")

(__compiler_emit "
private static <T extends Throwable> void throwException(Throwable exception, Object dummy) throws T {
    throw (T) exception;
}")

(__compiler_emit "
public static <T> T throwSilent(Object exception) {
    throwException((Throwable) exception, null);
    return null;
}")

(defn assoc [xs k v]
  (__compiler_emit "
var col = (java.util.Map<Object, Object>) xs;
var result = new java.util.HashMap<>(col);
result.put(k, v);
")
  result)

(defn concat [xs ys]
  (__compiler_emit "
var a = (java.util.List<Object>) xs;
var b = (java.util.List<Object>) ys;
var result = new java.util.ArrayList<>(a);
result.addAll(b);")
  result)

(__compiler_emit "
public static Boolean contains(Object xs, Object x) {
    var col = (java.util.Map<?, ?>) xs;
    return col.containsKey(x);
}")

(__compiler_emit "
public static Object re_find(Object re, Object s) {
    var pattern = (java.util.regex.Pattern) re;
    var matcher = pattern.matcher((CharSequence) s);
    if (matcher.find()) {
        return matcher.group();
    } else {
        return null;
    }
}")

(defn reduce [f init xs]
  (__compiler_emit "
var func = (Fn2) f;
var col = (java.util.Collection<Object>) xs;
var result = init;
for (Object x : col) {
    try {
        result = func.invoke(result, x);
    } catch (Exception e) {
        throwException(e, null);
    }
}")
  result)

(__compiler_emit "
public static java.util.List<?> vec(Object xs) {
    if (xs instanceof java.util.Collection) {
        return (java.util.List<?>) xs;
    } else if (xs instanceof Object[]) {
        return java.util.Arrays.asList((Object[]) xs);
    }
    throw new RuntimeException(\"Unsupported source: \" + xs);
}")

(__compiler_emit "
public static Object hash_map(Object... xs) {
    var result = new java.util.HashMap<Object, Object>();
    for (int i = 0; i < xs.length; i += 2) {
        result.put(xs[i], xs[i + 1]);
    }
    return result;
}")

(__compiler_emit "
public static Object hash_map_from(Object xs) {
    var result = new java.util.HashMap<Object, Object>();
    var items = (java.util.List<Object>) xs;
    for (int i = 0; i < items.size(); i += 2) {
        result.put(items.get(i), items.get(i + 1));
    }
    return result;
}")

(__compiler_emit "
public static java.util.List<Object> take(Object n, Object xs) {
    var col = (java.util.List<Object>) xs;
    return col.subList(0, (Integer) n);
}")

(__compiler_emit "
public static java.util.List<Object> shuffle(Object seed, Object xs) {
    var col = (java.util.Collection<Object>) xs;
    var result = new java.util.ArrayList<>(col);
    var seed2 = (long) (((double) seed) * Long.MAX_VALUE);
    java.util.Collections.shuffle(result, new java.util.Random(seed2));
    return result;
}")

(__compiler_emit "
public static java.util.List<Object> conj(Object xs, Object x) {
    var col = (java.util.Collection<Object>) xs;
    var result = new java.util.ArrayList<>(col);
    result.add(x);
    return result;
}")

(__compiler_emit "
public static java.util.List<Object> map(Object f, Object xs) {
    var func = (java.util.function.Function<Object, Object>) f;
    if (xs instanceof java.util.Map) {
        var map = (java.util.Map<Object, Object>) xs;
        var result = new java.util.ArrayList<Object>(map.size());
        for (java.util.Map.Entry<Object, Object> entry : map.entrySet()) {
            result.add(func.apply(java.util.List.of(entry.getKey(), entry.getValue())));
        }
        return result;
    }
    var col = (java.util.Collection<Object>) xs;
    var result = new java.util.ArrayList<Object>(col.size());
    for (Object x : col) {
        result.add(func.apply(x));
    }
    return result;
}")

(__compiler_emit "
public static java.util.List<Object> filter(Object f, Object xs) {
    var func = (java.util.function.Function<Object, Object>) f;
    var col = (java.util.Collection<Object>) xs;
    var result = new java.util.ArrayList<Object>();
    for (Object x : col) {
        if (toBoolean(func.apply(x))) {
            result.add(x);
        }
    }
    return result;
}")

(__compiler_emit "
public static Object drop(Object n, Object xs) {
    var col = (java.util.List<Object>) xs;
    return col.subList((Integer) n, col.size());
}")

(__compiler_emit "
public static java.util.Map<Object, Object> merge(Object as, Object bs) {
    var a = (java.util.Map<Object, Object>) as;
    var b = (java.util.Map<Object, Object>) bs;
    var result = new java.util.HashMap<>(a);
    result.putAll(b);
    return result;
}")

(__compiler_emit "
public static Object[] into_array(Class<?> cls, Object xs) {
    var col = (java.util.List<Object>) xs;
    var result = (Object[]) java.lang.reflect.Array.newInstance(cls, col.size());
    return col.toArray(result);
}")

;;
;;
;;

(defn inc [^int x]
  (+ x 1))

(defn fixme [loc xs]
  (java.util.Objects.requireNonNull
   nil
   (str loc " " xs)))

;; Collections

(defn get [xs i]
  (cond
    (instance? java.util.Map xs) (.get (cast java.util.Map xs) i)
    (instance? java.util.List xs) (.get (cast java.util.List xs) (cast int i))
    :else (FIXME "Unsupported source: " (str xs) ", key: " (str i))))

(defn update [m k f]
  (assoc m k (y2k.prelude.invoke f (get m k))))
