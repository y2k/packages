package y2k;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class prelude_java_v2 {
    public static final Object __namespace = "prelude_java_v2";

    public static Object inc(Object p__3) throws Exception {
        int x = (int) ((int) p__3);
        return (x + 1);
    };

    public static Object fixme(Object loc, Object xs) throws Exception {

        return java.util.Objects.requireNonNull(
                null,
                String.format(
                        "%s%s%s",
                        loc,
                        " ",
                        xs));
    };

    public static Object get(Object xs, Object i) throws Exception {
        Object p__2;
        if (y2k.RT.toBoolean(
                (xs instanceof java.util.Map))) {
            p__2 = ((java.util.Map) xs).get(i);
        } else {
            Object p__1;
            if (y2k.RT.toBoolean(
                    (xs instanceof java.util.List))) {
                p__1 = ((java.util.List) xs).get(((int) i));
            } else {
                p__1 = y2k.prelude_java_v2.fixme(
                        "prelude/data/prelude_java_v2.clj:91:9",
                        java.util.Arrays.asList(
                                "Unsupported source: ",
                                String.format(
                                        "%s",
                                        xs),
                                ", key: ",
                                String.format(
                                        "%s",
                                        i)));
            }
            ;
            p__2 = p__1;
        }
        ;
        return p__2;
    };

    public static Object update(Object m, Object k, Object f) throws Exception {

        return y2k.RT.assoc(
                m,
                k,
                y2k.RT.invoke(
                        f,
                        y2k.prelude_java_v2.get(
                                m,
                                k)));
    };
}