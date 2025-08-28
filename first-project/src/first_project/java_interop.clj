(ns first-project.java-interop)

;; Creating Java objects in Clojure
;;

;; Creating a new Java object using `new`
(def m (new java.util.HashMap))
m

;; An briefer alternative to creating Java instances: the "dot syntax"
(def m1 (java.util.HashMap.))
m1

;; Additionally, one uses the "dot syntax" to invoke methods on Java objects
(.put m "a" 1)
m

;; One can access an instance variable using the "dot-dash" syntax
;;
;; For example, the `.-v` expression allows us to access the **original**
;; vector of a sub-vector.
(subvec [1 2 3] 2)
(.-v (subvec [1 2 3] 2))
(type (subvec [1 2 3] 2))

;; The Clojure macro, `doto`, is a convience macro for accessing run-time
;; paths of Java objects. For example,
(doto (java.util.HashMap.)
  (.put "a" 1)
  (.put "b" 2))

;; One can supply an existing instance as the first argument of `doto`
;;
;; Notice that the value `m1` is actually changed in-place consistent
;; with Java instance semantics.
(doto m1
  (.put "a" 1)
  (.put "b" 2))
m1

;; "Under the hood," all Clojure objects **are** Java objects and can
;; be manipulated using Java; however, using these methods is **not**
;; recommended because then one loses the benefits of Clojure semantics.

;; Calling Clojure code **from** Java
;;

;; To perform these calls, the `namespace` macro is our friend.

;; Adding the `(:gen-class)` option instructs the Clojure compiler to
;; generate Java classes for our namespace. Similarly, using the `import`
;; keyword support importing Java classes into a Clojure namespace.

;; Java Arrays in Clojure
;;

;; Create a Java array using `make-array`
(def an-array (make-array Integer/TYPE 3))
an-array
(get an-array 0)
(get an-array 1)
(get an-array 2)
(get an-array 3)

;; Create a Java array with an implicit type
(def an-int-array (int-array (range 9)))
(get an-int-array 0)
(get an-int-array 1)
(get an-int-array 8)
(get an-int-array 9)

;; Although I used `get` to access elements of the array, the preferred
;; method is `aget`
(def a (int-array (reverse (range 9))))
(aget ^ints a 0)  ;; Using a type hint
(aget a 1)        ;; No type hint
(aget ^ints a 7)
(aget ^ints a 8)

;; But the following expressing will throw an
;; `ArrayIndexOutOfBoundsException`
;; (aget ^ints a 9)

;; Java arrays come with their own map and reduce functions
(defn asum [^floats arr]
  (areduce arr i acc (float 0)
           (let [x (aget arr i)]
             (+ acc x))))
(asum (float-array (range 9)))

;; But remember, invoking `asum` on an **integer** array will throw an
;; exception.
;; (asum (int-array (range 5)))

;; Notice that `asum` and `sum` have **different** signatures
(defn sum [arr]
  (reduce (fn [acc x]
            (+ acc x))
          (float 0)  ;; Causes all operations to be floating point
          arr))
(sum (range 9))

;; Notice the difference between `areduce` and `reduce`. Using `areduce`
;; requres **the programmer** to get the next value in the array. (Notice
;; the use of `i` and the expression `aget arr i`.) However, `reduce`
;; passes the current value of the accumulator and the next value to
;; our reducer function, `(fn [acc x] (+ acc x))`, **without** us
;; accessing array elements.

;; The `amap` function is similar to `map`.
(defn inker [^ints arr]
  (amap ^ints arr
        index
        acc
        (+ (int 1)
           (aget ^ints arr index))))
(def an-incremented-array (inker (int-array (range 9))))
(aget an-incremented-array 0)
(aget an-incremented-array 1)
(aget an-incremented-array 7)
(aget an-incremented-array 8)
;; (aget an-incremented-array 9)

;; Clojure exception-handling (try-catch)
;;

;; Errors in Clojure are just Java exceptions. Just like Java, clojure
;; has forms for `try` `catch` and `finally`.

;; Be careful not to confuse arrays with vectors. For example, the following
;; form raises an exception because `subvec` expects a Clojure vector but
;; `int-array` generates a Java array.
;; (subvec (int-array (range 10) 2))

;; Using `try` and `catch` to handle exceptions more gracefully
(def handling-array-exception
  (try
    (subvec (int-array (range 10) 2))
    (catch Exception e
      (println (.getMessage e))
      (int-array 0))))
(alength handling-array-exception)

;; One can supply **multiply** `catch` expressions in a `try`
(try
  (/ 4 0)
  (catch ArithmeticException e
    (println "Probably trying to divide by zero...")
    111)
  (catch ClassCastException e
    (println "Did you try to do math with a string?")
    222)
  (catch Exception e
    (println "Some other exception. Won't be caught in this case...")
    333)
  (finally
    (println "Always executed but **does not** return a value.")))
