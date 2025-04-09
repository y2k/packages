(defn parse_with_dsl [body dsl]
  (let [items []]
    (->
     (Promise.resolve (Response. body))
     (.then (fn [res]
              (let [text_buffer (atom "")
                    rewriter (HTMLRewriter.)]
                (->
                 (.reduce
                  (.slice dsl 1)
                  (fn [[rw index] [rl attr]]
                    (if (Array.isArray attr)
                      (.reduce
                       (.slice attr 1)
                       (fn [[rw index2] [rl2 attr2]]
                         [(.on rw (str (get dsl 0) " " rl " " (get attr 0) " " rl2)
                               (cond

                                 (= attr2 :inner_text)
                                 {:element (fn [element]
                                             (reset! text_buffer "")
                                             (.onEndTag
                                              element
                                              (fn []
                                                (-> items (.at -1)  (.at index) (.at -1) (assoc! index2 (deref text_buffer)))
                                                (reset! text_buffer ""))))}

                                 :else
                                 {:element (fn [element]
                                             (-> items (.at -1) (.at index) (.at -1) (assoc! index2 (.getAttribute element attr2))))}))
                          (+ 1 index2)])
                       [(->
                         rewriter
                         (.on (str (get dsl 0) " " rl " " (get attr 0))
                              {:element (fn [element]
                                          (-> items
                                              (.at -1)
                                              (.at index)
                                              (.push (.fill (Array. (- attr.length 1)) nil))))}))
                        0])
                      nil)
                    [(.on rw (str (get dsl 0) " " rl)
                          (cond

                            (= attr :inner_text)
                            {:element (fn [element]
                                        (reset! text_buffer "")
                                        (.onEndTag element (fn []
                                                             (-> items (.at -1) (assoc! index (deref text_buffer)))
                                                             (reset! text_buffer ""))))}

                            (Array.isArray attr)
                            {:element (fn [element]
                                        (-> items (.at -1) (assoc! index [])))}

                            :else
                            {:element (fn [element]
                                        (-> items (.at -1) (assoc! index (.getAttribute element attr))))}))
                     (+ 1 index)])
                  [(->
                    rewriter

                    (.on (get dsl 0)
                         {:element (fn [element]
                                     (.push items (.fill (Array. (- dsl.length 1)) nil)))})

                    (.on (str (get dsl 0) " *")
                         {:text (fn [t]
                                  (reset! text_buffer (str (deref text_buffer) t.text)))}))

                   0])
                 first
                 (.transform res)))))
     (.then (fn [x] (.arrayBuffer x)))
     (.then (fn [] items)))))
