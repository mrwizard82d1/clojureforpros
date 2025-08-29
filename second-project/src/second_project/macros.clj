(ns second-project.macros)

;; Writing macros
;;

;; A macro is code that operates **on other code**.
;; Typically, they return other code.

(try
  (1 2 3)
  (catch Exception e
    (println "An unquoted list `(1 2 3)`")))

;; But a quoted list is just "ducky".
'(1 2 3)

;; Another quoted lists demonstrate the similarity of code and data.
;; Evaluating this form returns a **list**.
'(defn hello-world [] (println "Hello, data! Or code??"))

;; The `eval` function attempts to evaluate a quoted form or list.
(eval '(defn hello-world [] (println "Hello, data! Or code??")))

;; And we now have a function to invoke
(hello-world)

;; Macros operate on code after the reader has completed reading
;; but **before** evaluation. They essentially transform the AST
;; produced by the reader into another AST that can be evaluated.

;; Clojure has a special form for defining macros
(defmacro def-
  "Same as `defn` but yielding a non-public definition"
  [name & decls]
  (list* `def
         (with-meta name
           (assoc (meta name) :private true))
         decls))

;; A backtick ("`") in Clojure is called **syntax quote**.
`def

;; It is like a quote ("'") but it **resolves symbols**.
`(defn hello-world [] (println "Hello, data! Or code??"))

;; The form returned by evaluating a backtick-quoted form resolves
;; all symbols to their fully-qualified name. For example:
`defn
`hello-world
`println
`"Hello, data! Or code??"

;; When we are in a namespace, Clojure most often loads all of `clojure.core`
;; into that namespace for our use. **Not so** with macros. The syntax quote
;; makes macros easier to write by allowing us to write macros closer to how
;; we would write typical code. And macroexpansion expands these quoted
;; symbols to fully-qualified symobls. The use of fully-qualified symbols
;; makes explicit what is most typically implicit when we write code.

;; A typical use of `def` outside a macro.
(def x 5)
x

;; We can quote a list to avoid treating it as a function call.
(def lst '(a b c))
lst

;; Clojure macros introduce other "macro operators." The "operators" are
;; almost always "close" to a backtick. For example, the following form
;; uses the reader symbols
;;
;; - syntax quote (`)
;; - unquote (~)
;; - unquote splicing (~@)
;;
`(fred x ~x lst ~@lst 7 8 :nine)

;; Here is the definition of a "built-in" macro: `when-let` but in
;; "user" space. To use it, I need to comment out the `assert-args` form
;; which is defined as private to the `clojure.core` namespace.
(defmacro my-when-let
    "bindings => binging-form test
    When test is true, evaluates body with binding-form bound to the value of test."
    {:added "1.0"}
    [bindings & body]
    #_(assert-args
      (vector? bindings) "a vector for its binding"
      (= 2 (count bindings)) "exactly two forms in a binding")
    (let [form (bindings 0) tst (bindings 1)]
      `(let [temp# ~tst]
         (when temp#
           (let [~form temp#]
             ~@body)))))

;; Even though we have not defined this macro, because we do not have
;; access to `assert-args`, we can use `macroexpand-1` to run one "level"
;; of expanding this macro.
(macroexpand-1 '(my-when-let [cool "hi"] (println cool)))

;; And we can invoke `macroexpand` to fully expand this macro.
(macroexpand '(my-when-let [cool "hi"] (println cool)))

;; Expanding this macro generates "strange" symbol names, for example,
;; `temp__5804__auto__`. This symbol is automatically generated and is
;; guaranteed to be unique. It uses the `gensym` function.
(gensym)
(gensym "cool_symbol__")

;; Although macros seem cool, in the "real world", macros are rarely needed.
;;
;; Here are the three typical reasons for using a macro:
;;
;; - Clojure does not support the feature that I need
;; - You need to **stop** evaluation; for example, when logging, one may
;;   not need to log the result fully (when logging an infinite sequence,
;;   for example)
;; - Reduce code duplication; however, this is **not recommended**.
;;
;; Macros are notoriously difficult to get correct (that is, to debug).
;; Remember, you should never write a macro to accomplish something that
;; you can accomplish using Clojure code and existing macros.
;;
;; Remember the three rules of Clojure macros:
;;
;; - Don't do it.
;; - Still don't do it.
;; - Don't do it even yet.

;; Common macros
;;

;; The `and` and `or` macros
(and false "true")

;; Although it may seem that the `and` macro evaluates all its arguments,
;; but actually, it only evaluates enough of its arguments to determine
;; that the value is "falsey". Only if it is "truthy" are all arguments
;; evaluated (and the last value returned).

(macroexpand '(and false "true"))

;; The `or` macro also short-circuits. It only evaluates enough of its
;; arguments to determine if its value is "truthy". Otherwise, it evaluates
;; all its arguments and returns the last argument.
(or "true" false)

(macroexpand '(or "true" false))

;; Clojure supports a "family" of `when` macros. For example,
(when-not (even? 3)
  "it's odd")

(macroexpand '(when-not (even? 3) "it's odd"))

;; The `when-some` macro conditonally executes its body with its bindings
;; if the specified (local) binding **is not `nil`**.
(when-some [cool false]
  (str cool " is not nil, so it evaluates its body!"))

(macroexpand '(when-some [cool false]
  (str cool " is not nil, so it evaluates its body!")))

;; The `when-first` macro pulls the first element in its binding
;; if it is "truthy".
(when-first [the-first '(1 2 3)]
  (str "Question number the " the-first "st"))

(when-first [the-first '()]
  (str "Question number the " the-first "st"))

(macroexpand '(when-first [the-first '(1 2 3)]
  (str "Question number the " the-first "st")))

;; The `cond->>` macro (`cond` thread last)
(def thing {:hi "there"})

(cond->> thing
  (not-empty thing) :hi
  (empty? thing) (or "Good day")
  true (str "hey, "))

;; But if `thing` is undefined
(def thing {})

(cond->> thing
  (not-empty thing) :hi
  (empty? thing) (or "Good day")
  true (str "hey, " ))

;; The `condp` macro
;;
;; Rarely used in the "real world" but occasionally seen.
(defn fizz-buzz
  [[x & xs]]
  (when x
    (condp #(zero? (rem %2 %1)) x
      15 :>> #(do (println % "is fizzbuzz")
                  (fizz-buzz xs))
      3 :>> #(do (println % "is fizz")
                 (fizz-buzz xs))
      5 :>> #(do (println % "is buzz")
                 (fizz-buzz xs))
      (fizz-buzz xs))))
(fizz-buzz (range 10))

;; I do not understand this implementation of fizz-buzz. I see it working
;; but I do not quite understand why. (A combination of not quite
;; understanding `condp` and not understanding `:>>`.)

;; The `case` macro
;;
(def x 20)

(case x
  10 :ten
  20 :twenty
  30 :forty
  :dunno)

;; The last clause is the default.
(def x 40)
(case x
  10 :ten
  20 :twenty
  30 :forty
  :dunno)

;; If no default is supplied but no case matches, an exception is thrown.
(def x 42)
#_(case x
  10 :ten
  20 :twenty
  30 :forty)
