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

(def case-branches
  (fn [name clauses]
    (if (= clauses (list))
      nil
      (if (= (drop 1 clauses) (list))
        (get clauses 0)
        (list 'if
              (list '= name (get clauses 0))
              (get clauses 1)
              (case-branches name (drop 2 clauses)))))))

(def case
  (fn [args]
    (let [name (gensym)]
      (list 'let*
            (list name (get args 0))
            (case-branches name (drop 1 args))))))

(def if-let-bindings
  (fn [source bindings then-form else-form]
    (if (= bindings (list))
      then-form
      (if (< (count bindings) 2)
        (macro-error "if-let: bindings must be name/value pairs")
        (let [name (get bindings 0)]
          (if (syntax-simple-symbol? name)
            (list 'let*
                  (syntax-list-at source (list name (get bindings 1)))
                  (list 'if
                        name
                        (if-let-bindings source (drop 2 bindings) then-form else-form)
                        else-form))
            (macro-error "if-let: binding names must be symbols")))))))

(def if-let
  (fn [args]
    (let [bindings (get args 0)
          size (count args)]
      (if (if (syntax-vector? bindings) (if (< size 2) false (< size 4)) false)
        (if (= (syntax-children bindings) (list))
          (macro-error "if-let: binding vector must not be empty")
          (if-let-bindings bindings (syntax-children bindings) (get args 1) (get args 2)))
        (macro-error "if-let: expected non-empty [name value ...], then branch, and optional else branch")))))

(def thread
  (fn [last? value steps]
    (if (= steps (list))
      value
      (let [step (get steps 0)
            items (if (syntax-list? step) (syntax-children step) (list step))
            head (list (get items 0))
            tail (drop 1 items)]
        (thread last?
                (syntax-list-at step
                                (if last?
                                  (concat head tail (list value))
                                  (concat head (list value) tail)))
                (drop 1 steps))))))

(def ->
  (fn [args]
    (thread false (get args 0) (drop 1 args))))

(def ->>
  (fn [args]
    (thread true (get args 0) (drop 1 args))))
