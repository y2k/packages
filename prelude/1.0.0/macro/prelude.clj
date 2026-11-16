(def cond
  (fn [clauses]
    (if (= clauses (list))
      nil
      (if (= (drop 1 clauses) (list))
        (macro-error "cond: expected test/result pairs")
        (list 'if
              (get clauses 0)
              (get clauses 1)
              (cond (drop 2 clauses)))))))

(def and
  (fn [clauses]
    (if (= clauses (list))
      true
      (if (= (count clauses) 1)
        (get clauses 0)
        (let [name (gensym)]
          (list 'let*
                (list name (get clauses 0))
                (list 'if name (and (drop 1 clauses)) name)))))))

(def or
  (fn [clauses]
    (if (= clauses (list))
      nil
      (if (= (count clauses) 1)
        (get clauses 0)
        (let [name (gensym)]
          (list 'let*
                (list name (get clauses 0))
                (list 'if name name (or (drop 1 clauses)))))))))

(def with-open-bindings
  (fn [source bindings body]
    (if (= (count bindings) 0)
      (if (= (count body) 0) nil (concat (list 'do) body))
      (if (< (count bindings) 2)
        (macro-error "with-open: expected simple symbol/value pairs")
        (let [name (get bindings 0)]
          (if (syntax-simple-symbol? name)
            (list 'let
                  (syntax-list-at source (list name (get bindings 1)))
                  (list 'try
                        (with-open-bindings source (drop 2 bindings) body)
                        (list 'finally
                              (list '.close (list 'cast 'java.lang.AutoCloseable name)))))
            (macro-error "with-open: expected simple symbol/value pairs")))))))

(def with-open
  (fn [args]
    (let [bindings (get args 0)]
      (if (syntax-vector? bindings)
        (with-open-bindings bindings (syntax-children bindings) (drop 1 args))
        (macro-error "with-open: expected a binding vector")))))
