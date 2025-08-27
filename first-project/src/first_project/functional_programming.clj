(ns first-project.functional-programming)

;; Functions and Purity
;;

;; Clojure discourages side-effects - but does not prevent them.

(defn greeter
  "Takes a function `f` and a string `s`.
  Calls f passing s and returns the result."
  [f s]
  (f s))
(greeter println "larry")

;; Using higher-order functions to build abstractions: reduce and reducers
;;
(reduce + 0 (range 9))

;; We could write `reduce` ourselves (but why?)
(defn reduce2
  ([f coll]
   (reduce2 f (first coll) (rest coll)))
  ([f init coll]
   (if (= 1 (count coll))
     (f init (first coll))
     (recur f (f init (first coll)) (rest coll)))))
(reduce2 + 0 (range 9))

;; We can actually use any function in `reduce`/`reduce2`
(reduce2 conj [] (range 7))

;; However, remember that Clojure offers **many, many** functions
;; "out of the box". For example, `into`.
(into [] (range 7))

;; Clojure supports the use of "higher order functions"; that is,
;; taking functions as arguments to other functions or returning
;; one function from another.
(reduce2 (fn [acc x]
           (conj acc (inc x)))
         []
         (range 7))

;; This use of `reduce2` has a generic name, `map`. Using the function,
;; `map`, we apply a function to each element in an existing collection
;; returning a new collection of applying a function to each element
;; of the original collection.
(defn mapper
  [f coll]
  (reduce2 (fn [acc x]
             (conj acc (f x)))
           []
           coll))
(mapper inc (range 7))

;; Although one can write `map` oneself, Clojure actually provides an
;; implementation.
(map inc (range 7))

;; Additionally, we can write our own filter function using `reduce2`
(defn filt
  [pred coll]
  (reduce2 (fn [acc x]
             (if (pred x)
               (conj acc x)
               acc))
           []
           coll))
(filt even? (range 7))

;; Clojure has a builtin function, `filter`
(filter even? (range 7))

;; Collectively, all these functions built on-top of `reduce` are called
;; "reducers". Each reducer returns a **brand new collection**; it
;; **does not** change a collection "in-place". This operation is
;; consistent with the immutablity of values in Clojure.
;;
;; Although you can often write your own reducer, you should prefer the
;; built-in functions since they are built for both behavior **and**
;; performance over different value types.
;;
;; We can think of functional programming as a series of transformations
;; on data (changing neither the original data nor any intermediate data).
;;
;; For example:
(reduce +
        (filter even?
                (map int (range 7))))

;; Although this kind of chaining is correct, it is difficult to "grok."
;; Consequently, Clojure provides threading macros that allow allow us
;; to mentally apply transformations one-step-at-a-time instead of
;; "inside-out". Using the "thread last" macro on our previous example
;; results in the following expression which generates the same result.
(->>
 (range 7)
 (map int)
 (filter even?)
 (reduce +))

;; Threading values through the **last** argument of a sequence of forms
;; is not always convenient. Clojure also provides the "thread first"
;; macro to handle those situations. The thread first macro threads
;; the value of each form into the **first** argument of each subsequent
;; form.
(-> {}
    (assoc :hello "clojure")
    (update :hello clojure.string/upper-case))
