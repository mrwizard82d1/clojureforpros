(ns first-project.control-structures)

;; If and when forms
;;

;; The `if` statement has three components:
;; - A predicate
;; - A true branch
;; - A(n optional) false branch
;;
;; If the predicate is "truthy," return the true branch (the first expression
;; after the test); otherwise, return the false branch (the next expression).
(defn even-or-odd
  [n]
  (if (even? n)
    "even"
    "odd"))
(even-or-odd 2)
(even-or-odd 3)

;; This form captures the value of an `if` form (statement).
(defn even-or-odd
  [n]
  (let [is-it (if (even? n)
                "even"
                "odd")]
    is-it))
(even-or-odd 2)
(even-or-odd 3)

;; The `if` form allows exactly one form for the true branch and either
;; zero or one form for the false branch. To do more that one thing, one
;; can use a `do` form.
(if (even? 2)
  (do (println "It's an even in an if")
      :even))

;; If we only "care about" the true branch of the `if`, we can use a `when` macro.
;; The `when` macro surrounds its true exrpessions in an implicit `do` form.
(when (even? 2)
  (println "It's even in a when")
  :even)

;; If the value of the test expression in a `when` form is **falsey**,
;; the `when` form evaluates to `nil`
(when (odd? 2)
  (println "It's odd in this when")
  :odd)

;; Our final branching expression is `cond`. It evaluates, from top-to-bottom,
;; each odd form. The first form that evaluates "truthy" results in returning
;; the wvalue of the second expression .
(defn a-sign-function
  [n]
  (cond
    (pos? n) "positive"
    (neg? n) "negative"
    :else "zero!"))
(a-sign-function 1)
(a-sign-function -1)
(a-sign-function 0)

;; I discovered I do not need to test for non-numeric values at this point
;; becase both ``pos?` and `neg?` apparently test that their arguments are
;; numeric.
;;(a-sign-function :zork)

;; Looping (and recursion)
;;
;; See ``https://clojure.org/reference/special_forms` for a detailed explanation`
(loop [num 0]
  (when (< num 10)
    (println num)
    (recur (inc num))))

;; How to **not** write a loop (and an alternative to looping)
;;

;; A `for`macro seems to be a loop; however, the `for` macro is **lazy**
;; and is not guaranteed it is **not** guaranteed to evaluate **all** the
;; side-effects in the body.
(for [x (range 10)] (println x))

;; This `for` expression produces a sequence of even integer values.
(for [x (range 10)]
      (* x 2))

;; If you want side-effects, use a `do` form (or a `doseq`). These forms
;; allow one to specify side-effects as part of the body of the form.
;;
;; Evaluating this `doseq` form:
;; - Prints out the message ten times
;; - May or may not evaluate the `result` expression
;; - Evaluations to `nil`
(doseq [x (range 10)]
  (let [result (* 2 x)]
    (println "The calculated result =" result)
    result))
