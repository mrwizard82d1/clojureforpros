(ns second-project.polymorphism)

;; Dynamic dispatch using multimethods
;;

;; Multimethods are "first-class, dynamic dispatch" in Clojure.
;; "Dynamic dispatch" means that we can change the behavior of
;; a function based on (any of) its (input) arguments.

;; The name of our multmethod is `make-safe`. The dispatch function is
;; `class` (which returns the Class of its argument). Although in this
;; case, we use the class of the function argument, we could write a
;; function that dispatches on any (or all) of the function arguments.
;; When this multimethod is invoked, Clojure will invoke the dispatch
;; function with the actual function parameter(s) as its arguments.
;; The result of the dispatch function identifies the concrete method
;; to invoke.
(defmulti make-safe class)

;; The `defmethod` macro(?) takes
;;
;; - The name of the method
;; - The value on which to dispatch. (In our case, the type of the
;;   argument because the dispatch function is the function `class`.)
;; - The argument(s) to the method
;; - The body of the method
(defmethod make-safe java.lang.Number
  [n]
  (bigdec n))
(make-safe 13)

(defmethod make-safe java.lang.String
  [n]
  (try
    (-> n
        (Double/parseDouble)
        (bigdec))
    (catch NumberFormatException nfe
      (println "String must be a string of numeric characters.")
      (bigdec 0.0))
    (catch Exception e
      (println "An unknown error converting from string to money.")
      (bigdec 0.0))))
(make-safe "17")

(defmethod make-safe clojure.lang.PersistentVector
  [n]
  (try
    (let [num-bytes
          ;; THe number of integers in the array
          (->> n (filter int?) (count))]
      (if (= (count n) num-bytes)
        (->> n
             (map char)
             (clojure.string/join)
             (Double/parseDouble)
             (bigdec))
        (throw (ex-info "Can only convert from vector of bytes"
                        {:input n}))))
    (catch NumberFormatException nfe
      (println "Vector must be bytes representing ASCII number characters")
      (bigdec 0.0))
    (catch Exception e
      (println "Error converting from vector of bytes to money")
      (bigdec 0.0))))
(make-safe [49 57])

(defmethod make-safe :default
  [_]  ;; We don't care about this binding
  (bigdec 0.0))
(make-safe :zork)

(map make-safe ["0.1" 0.1 [0x30 0x2e 0x31]])

;; Although `make-safe` dispatches on the type (`class`) of the input data,
;; one can actually dispatch on any or all of the input data because we
;; **define** the dispatch function.
;;
;; Multimethods have a small performance penalty (compared to normal function
;; invocation); however, it is "largely negligible."
;;
;; You may be wondering, "Why use multimethods instead of using a `cond`
;; form?" Multimethods allow us to decouple the **usage** of a method
;; from the code being run for that method. This indirection allows us
;; to **extend** a multimethod **without** impacting its calling function
;; or other method implementations.

;; From "Clojure for the Brave and True"

;; Specify the multi-method with its dispatch function
(defmulti full-moon-behavior (fn [were-creature]
                               (:were-type were-creature)))

;; Define a multimethod for a specific "instance" (a `:wolf`)
(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

;; Define a multimethod for another "instance" (`:simmons`)
(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

;; Invoke the multimethod for some instance (a `:wolf`)
(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})

;; Invoke the multimethod for another instance (a `:simmons`)
(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})

;; Protocols
;;

;; We can define a protocol with `defprotocol`

;; Interfaces
;;
