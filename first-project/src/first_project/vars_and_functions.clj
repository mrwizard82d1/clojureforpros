(ns first-project.vars-and-functions)

;; Defining variables (vars) and functions
;;

;; Defining a variable establishes a **binding** between a symbol, `greeting`,
;; and its value, the string "hello, world!"
(def greeting "hello, world!")
greeting

;; Defining top-level functions (using `def`)
(def hello-world (fn [name] (println (str "Hello, " name "!"))))
(hello-world "Larry")

;; Using `def` and `fn` to create a function and to bind it to a variable
;; is, as you might imagine, incredibly common. Clojure provides a shortcut,
;; the `defn` macro.
(defn hello-world [name]
  (println (str "hello, " name "!")))
(hello-world "Larry")

;; What about that `nil`? That value is the result of executing our function.
;; The return value of the `println` function is `nil`. This function has a
;; side-effect, printing a `string` to the terminal.

;; One could change the return value by returing, for example, the value of
;; the variable, `name`.
(defn hello-world [name]
  (println (str "hello, " name "!"))
  name)
(hello-world "Larry")

;; A function taking **two** arguments. Notice that we define this function
;; with a "doc-string." The "doc-string" documents the defined function.
(defn hello-world2
  "Takes two strings, `greeting` and `name` and prints the greeting
  addressed to name."
  [greeting name]
  (println (str greeting ", " name "!")))
(hello-world2 "Hello" "Guv'ner")

;; We can access the function doc-string using the function `clojure.repl/doc`.
(clojure.repl/doc hello-world2)

;; Clojure supports definining functions with the same name but
;; multiple **arities**.
(defn hello-world2
  "Takes an optional string, `greeting` and a `name` and prints a greeting
  addressed to name."
  ([name]
   (hello-world2 "Hello" name))
  ([greeting name]
   (println (str greeting ", " name "!"))))
(hello-world2 "Larry")
(hello-world2 "Howdy" "Larry")

;; Let's (re-)visit anonymous functions.
;;
;; The `fn` form creates an anonymous (unnamed) function. One can assign these
;; anonymous functions to a value as we saw previously.
(fn [name] (println (str "hello, " name "!")))

;; One can invoke an anonymous function by wrapping it and the actual
;; argument(s) in another pair of parentheses.
((fn [name] (println (str "hello, " name "!"))) "Larry")

;; Because defining anonymous functions is so common in Clojure, Clojure
;; provides an additional form most useful for creating small
;; easily-understood functions. To refer to actual arguments, one can use
;; `%` for a **single** formal parameter or `%1`, `%2`, and so on for
;; multiple formal parameters.
#(println (str "hello, " % "!"))
(#(println (str "hello, " %1 "!")) "Larry")

;; Use `let` to define "local variables" for functions (and other scopes).
;; Remember, `def` establishes a **global** binding even if used in a deeply
;; nested lexical scope.

;; The `let` form lets us establish a **local binding** for everything
;; (lexically) inside the `let` form.
(defn hello-world2
  [greeting name]
  (let [full-greeting (str greeting ", " name "!")]
    (println full-greeting)))
(hello-world2 "Good evening" "Larry")

;; Clojure supports destructuring of variables bound in a `let`. Destructuring
;; allows us to "take apart" complicated arguments to define some or all of
;; their constituent parts.

;; We'll destructure a list or a vector into its head and tail.
(let [our-seq (reverse (range 9))
      [head & tail] our-seq]
  (println "our-seq: " our-seq)
  (println "head: " head)
  (println "tail: " tail)
  (println "")
  (println "(first our-seq) =>" (first our-seq))
  (println "(rest our-seq) =>" (rest our-seq)))

;; Same problem but demonstrate a call to `second`
(let [our-seq (reverse (range 9))
      [head & tail] our-seq]
  (println "our-seq: " our-seq)
  (println "head: " head)
  (println "tail: " tail)
  (println "")
  (println "(first our-seq) =>" (first our-seq))
  (println "(second our-seq =>" (second our-seq))
  (println "(rest our-seq) =>" (rest our-seq)))

;; We need not call `tail` to get the rest of the items in the sequence.
;; An alternative is `nth`. Additionally, but not using the rest operator,
;; `&`, we only bind the first three items in `our-seq`.
(let [our-seq (reverse (range 9))
      [head neck shoulders] our-seq]
  (println "our-seq: " our-seq)
  (println "head: " head)
  (println "neck: " neck)
  (println "shoulders: " shoulders)
  (println "")
  (println "(first our-seq) =>" (first our-seq))
  (println "(second our-seq =>" (second our-seq))
  (println "(nth our-seq 2) =>" (nth our-seq 2)))

;; One can use the `&` to get the rest of the values in the binding if it
;; is "long". If the binding is to short, other values being bound will
;; be bound to `nil`.` However, the call to `nth` will raise an exception.
;; (Because an exception is raised, we save the expression but tell the
;; Clojure reader to ignore the entire expression.)
#_(let [our-seq (reverse (range 2))
      [head neck shoulders] our-seq]
  (println "our-seq: " our-seq)
  (println "head: " head)
  (println "neck: " neck)
  (println "shoulders: " shoulders)
  (println "")
  (println "(first our-seq) =>" (first our-seq))
  (println "(second our-seq =>" (second our-seq))
  (println "(nth our-seq 2) =>" (nth our-seq 2)))

;; Destructuring is not limited to vectors and lists. This form destructures
;; elements (keys and values) in a map.

;; The following code **does not** use destructuring to extract elements
;; from a map.
(let [m {:first-name "Lawrence"
         :middle-initial "A"
         :last-name "Jones"}
      first-name (:first-name m)
      middle-initial (:middle-initial m)
      last-name (:last-name m)]
  (println (str first-name " " middle-initial ". " last-name)))

;; Destructing lessens the amount of code we must right to extract
;; elements from a map.
(let [{first-name :first-name
       middle-name :middle-initial
       last-name :last-name} {:first-name "Lawrence"
                              :middle-initial "A"
                              :last-name "Jones"}]
  (println (str first-name " " middle-name ". " last-name)))

;; We can actually simplify this code still further if we can live with the
;; "constraint" that the names of our symbols (variables) is the same as
;; the textual part of the atoms used as keys in our map.
(let [{:keys [first-name middle-initial last-name]} {:first-name "Lawrence"
                                                     :middle-initial "A"
                                                     :last-name "Jones"}]
      (println (str first-name " " middle-initial ". " last-name)))
