package y2k.language;

public final class language_runtime {

  @FunctionalInterface
  public interface Fn0 {
    Object call() throws Exception;
  }

  @FunctionalInterface
  public interface Fn1 {
    Object call(Object a) throws Exception;
  }

  @FunctionalInterface
  public interface Fn2 {
    Object call(Object a, Object b) throws Exception;
  }

  @FunctionalInterface
  public interface Fn3 {
    Object call(Object a, Object b, Object c) throws Exception;
  }

  @FunctionalInterface
  public interface Fn4 {
    Object call(Object a, Object b, Object c, Object d) throws Exception;
  }

  public static RuntimeException sneaky_throw(Throwable throwable) {
    language_runtime.<RuntimeException>sneaky_throw_unchecked(throwable);
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneaky_throw_unchecked(Throwable throwable) throws T {
    throw (T) throwable;
  }

  public static java.util.List<Object> list(Object... items) {
    return java.util.Arrays.asList(items);
  }

  public static Boolean vector_QMARK_(Object value) {
    return value instanceof java.util.List<?>;
  }

  public static java.util.List<Object> concat(Object... collections) {
    var result = new java.util.ArrayList<Object>();
    for (Object collection : collections) {
      if (!(collection instanceof java.util.List<?> items))
        throw new RuntimeException("concat expects lists");
      result.addAll(items);
    }
    return result;
  }

  public static java.util.LinkedHashMap<String, Object> hash_map(Object... items) {
    if (items.length % 2 != 0) {
      throw new RuntimeException("hash-map arguments must be key/value pairs");
    }
    var result = new java.util.LinkedHashMap<String, Object>();
    for (int i = 0; i < items.length; i += 2) {
      result.put(value_text(items[i]), items[i + 1]);
    }
    return result;
  }

  public static Object get(Object collection, Object key) {
    if (collection instanceof java.util.List<?> items && key instanceof Number index) {
      int i = index.intValue();
      return i >= 0 && i < items.size() ? items.get(i) : null;
    }
    if (collection instanceof java.util.Map<?, ?> items)
      return items.get(value_text(key));
    throw new RuntimeException("get expects a hash-map/list and a key/index");
  }

  public static boolean truthy(Object value) {
    return !(value == null || Boolean.FALSE.equals(value) || "false".equals(value) || "nil".equals(value));
  }

  public static Object print_result(Object value) {
    System.out.println(value_text(value));
    return null;
  }

  public static Object println(Object... items) {
    System.out.println(join_str(items, " "));
    return null;
  }

  public static Object eprintln(Object... items) {
    System.err.println(join_str(items, " "));
    return null;
  }

  public static Object missing(Object... items) {
    throw new RuntimeException("missing");
  }

  public static String str(Object... items) {
    return join_str(items, "");
  }

  public static Integer _PLUS_(Object... items) {
    int result = 0;
    for (Object item : items)
      result += ((Number) item).intValue();
    return result;
  }

  public static Integer _MINUS_(Object... items) {
    if (items.length == 0)
      throw new RuntimeException("- expects at least one number");
    int result = ((Number) items[0]).intValue();
    for (int i = 1; i < items.length; i++)
      result -= ((Number) items[i]).intValue();
    return result;
  }

  public static Integer _STAR_(Object... items) {
    int result = 1;
    for (Object item : items)
      result *= ((Number) item).intValue();
    return result;
  }

  public static Integer _SLASH_(Object... items) {
    if (items.length == 0)
      throw new RuntimeException("/ expects at least one number");
    int result = ((Number) items[0]).intValue();
    for (int i = 1; i < items.length; i++)
      result /= ((Number) items[i]).intValue();
    return result;
  }

  public static Integer count(Object collection) {
    if (collection instanceof java.util.List<?> list)
      return list.size();
    if (collection instanceof java.util.Map<?, ?> map)
      return map.size();
    throw new RuntimeException("count expects one collection");
  }

  public static java.util.List<Object> map(Object fn, Object collection) throws Exception {
    if (!(collection instanceof java.util.List<?> items)) {
      throw new RuntimeException("map expects a function and a list");
    }
    var result = new java.util.ArrayList<Object>();
    for (Object item : items)
      result.add(call_fn(fn, item));
    return result;
  }

  public static java.util.List<Object> drop(Object count, Object collection) {
    var items = (java.util.List<?>) collection;
    int start = Math.min(Math.max(((Number) count).intValue(), 0), items.size());
    return new java.util.ArrayList<Object>(items.subList(start, items.size()));
  }

  public static Object reduce(Object fn, Object collection) throws Exception {
    if (!(collection instanceof java.util.List<?> items)) {
      throw new RuntimeException("reduce expects a function and a list");
    }
    if (items.isEmpty()) {
      throw new RuntimeException("reduce expects a non-empty list");
    }
    Object acc = items.get(0);
    for (int i = 1; i < items.size(); i++)
      acc = call_fn(fn, acc, items.get(i));
    return acc;
  }

  public static Object reduce(Object fn, Object init, Object collection) throws Exception {
    if (!(collection instanceof java.util.List<?> items)) {
      throw new RuntimeException("reduce expects a function and a list");
    }
    Object acc = init;
    for (Object item : items)
      acc = call_fn(fn, acc, item);
    return acc;
  }

  static Object call_fn(Object fn, Object... args) throws Exception {
    if (fn instanceof Fn0 callable) {
      expect_args("function", args, 0);
      return callable.call();
    }
    if (fn instanceof Fn1 callable) {
      expect_args("function", args, 1);
      return callable.call(args[0]);
    }
    if (fn instanceof Fn2 callable) {
      expect_args("function", args, 2);
      return callable.call(args[0], args[1]);
    }
    if (fn instanceof Fn3 callable) {
      expect_args("function", args, 3);
      return callable.call(args[0], args[1], args[2]);
    }
    if (fn instanceof Fn4 callable) {
      expect_args("function", args, 4);
      return callable.call(args[0], args[1], args[2], args[3]);
    }
    throw new RuntimeException("value is not a function");
  }

  static void expect_args(String name, Object[] args, int count) {
    if (args.length != count) {
      throw new RuntimeException(name + " expects " + count + " arguments");
    }
  }

  static String value_text(Object value) {
    if (value == null)
      return "nil";
    if (value instanceof java.util.List<?> list) {
      var items = new java.util.ArrayList<String>();
      for (Object item : list)
        items.add(value_text(item));
      return "(" + String.join(" ", items) + ")";
    }
    if (value instanceof java.util.Map<?, ?> map) {
      var items = new java.util.ArrayList<String>();
      for (var entry : map.entrySet()) {
        items.add(entry.getKey() + " " + value_text(entry.getValue()));
      }
      return "{" + String.join(" ", items) + "}";
    }
    if (value instanceof Fn0 || value instanceof Fn1 || value instanceof Fn2 || value instanceof Fn3
        || value instanceof Fn4)
      return "#<function>";
    return String.valueOf(value);
  }

  static String str_text(Object value) {
    return value_text(value);
  }

  static String join_str(Object[] items, String separator) {
    var parts = new java.util.ArrayList<String>();
    for (Object item : items)
      parts.add(str_text(item));
    return String.join(separator, parts);
  }
}
