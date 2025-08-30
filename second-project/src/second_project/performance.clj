(ns second-project.performance)

;; Transducers, Transients, and Arrays
;;

;; Transducers
;;
;; Transducers parallelize reducers. Consequently, our code need not create
;; new, intermediate collections between transformations.

(transduce (comp (filter even?)
                 (map inc))
           + (range 1000))

;; Let's time our transducer
(time (transduce (comp (filter even?)
                       (map inc))
                 + (range 1000)))

;; Let's time the same implementation simply using reducers
(time (->> (range 1000)
           (filter even?)
           (map inc)
           (reduce +)))

;; I find it very interesting that our transducer implementation consumes
;; 0.40 msecs but our reducer implementation only consumes 0.24 msecs.
;; Very different from the results reported by the instructor (0.45 and
;; 0.58 msecs, respectively). This discrepancy may result from my setup:
;; emacs but with an `nrepl` window in emacs for execution.

;; But what if we have more items?
;;
(time (transduce (comp (filter even?)
                       (map inc))
                 + (range 10000000)))
(time (->> (range 10000000)
           (filter even?)
           (map inc)
           (reduce +)))

;; More items gives the behavior I expect: the implementation using a
;; transducer uses less time for the same number of items that the
;; implementation using a reducer.
;;
;; For 1-arity functions, Clojure reducers actually return a **transducer**.
;; We can compose these transformed reducers into new **transducers** by
;; using the `comp` function.
;;
;; Transducers are sometimes called:
;;
;; - Transforms
;; - Xforms
;; - Xf (an abbreviation in code)
;;
;; Because transducers are a performance optimization, they are best suited
;; for "large" collections. As you can see from the timings above, the normal
;; reducers perform relatively well for "small N". However, if input sizes
;; are in the millions, transducers perform better.

(mapv inc (filter even? (range 1000)))

(doseq [_ (time (mapv inc (filter even? (range 1000))))])

(doseq [_ (time (mapv inc (filter even? (range 10000000))))])

;; Transducers need not be used **only** with reducers. For example,
;; the `eduction` function allows one to apply a transducer to **each
;; element in a collection**.
(eduction (comp (filter even?)
                (map inc))
          (range 10))

;; **Although** most real world applications apply transducers using the
;; 3-arity version of `into`.
(into [] (comp (filter even?)
               (map inc))
      (range 10)
