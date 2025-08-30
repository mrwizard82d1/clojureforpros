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
;; are in the millions, transducers perform much, much better.

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
      (range 10))

;; Transducers are **context independent**. This term means we can compose
;; transducers from **other transducers**.

;; Transients
;;

;; We can now investigate the reason that normal reducers are characterized
;; by performance comparable to transducers on smaller collections.
;;
;; Lazy reducers rely on transients. A transient is a stable version of our
;; typical collections.

;; From clojure.org
;; (Does **not** rely on transients.)
(defn vrange [n]
  (loop [i 0 v []]
    (if (< i n)
      (recur (inc i) (conj v i))
      v)))

;; Transients are intended to be used as part of the "encapsulated state
;; pattern". (According to Google AI, "The Encapsulated State Pattern,
;; more commonly known as the State Design pattern,...") If you need to
;; use a transient, it should be used in the scope of a **single function**.
;; Here's an example of the previous function whose implementation uses
;; transients.
(defn vrange2 [n]
  (loop [i 0 v
         ;; Creates a transient from an existing persisent collection
         (transient [])]
    (if (< i n)
      (recur (inc i)
             ;; **Mutates** our transient collection
             (conj! v i))
      ;; When finished, converts the transient (mutable) collection back
      ;; into a persistent collection.
      (persistent! v))))

;; Time the different implementations. The non-transient version is about
;; 95 msecs but the transient version is only 38 msecs.
(doseq [_ (time (vrange 1000000))])
(doseq [_ (time (vrange2 1000000))])

;; We see similar improvement for collections an order of magnitude larger.
;; The non-transient version is about 713 msecs but the transient version
;; is only 328 msecs.
(doseq [_ (time (vrange 10000000))])
(doseq [_ (time (vrange2 10000000))])

;; Transient collections support a more limited set of operations:
;;
;; - `conj!`
;; - `assoc!`
;; - `dissoc!`
;; - `disj!`
;; - `pop!`
;;
(persistent! (conj! (transient []) "hello"))
(persistent! (assoc! (transient {}) :greeting "hello"))
(persistent! (dissoc! (transient {:greeting "hello"}) :greeting))
(persistent! (disj! (transient #{:greeting "hello"}) :greeting))
(persistent! (pop! (transient ["hello"])))

;; ** WARNING **
;; **Always** use the return value of these operations because the internal
;; representation of the tranient might **change** as it mutates.
;;
;; Finally, remember that transients are **not thread safe**. If you use a
;; transient, you must ensure that all mutations occur on a **single thread**.

;; Array performance characteristics
;;

;; Arrays are, by far, the most performant collection option in Clojure.
;; But the cost is **all the benefits** offered by Clojure collections.
;; Arrays are, quite literally, just Java arrays, and all the Java array
;; rules apply to them.
(defn arange [n]
  (let [an-array (int-array n)]
    (amap ^ints an-array
          idx
          ret
          (+ (int 1)
             (aget ^ints an-array (if (pos? idx)
                                    (- idx 1)
                                    idx))))))

(let [ar (arange 5)]
  (println (count ar))
  (println (aget ar 0))
  (println (aget ar 4)))

;; Generally, the performance of arrays are on-par (if not better than) the
;; performance of transients. But remember, arrays are **far less flexible**
;; than more typical Clojure collections.
(doseq [_ (time (arange 10000000))])
(doseq [_ (time (arange 100000000))])
(doseq [_ (time (arange 1000000))])
