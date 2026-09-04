(ns xml-test
  (:require [xml :as xml]))

(deps {:xml "0.4.0"})

(assert (= "<tag></tag>" (xml/to-string [:tag])))
(assert (= "<tag id='x'></tag>" (xml/to-string [:tag {:id "x"}])))
(assert (= "<tag>text</tag>" (xml/to-string [:tag {} "text"])))
(assert (= "<root><child></child></root>"
           (xml/to-string [:root {} [:child]])))
