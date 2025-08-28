(ns first-project.state)

;; Managing state through
;; - Vars
;; - Atoms
;; - Refs
;; - Agents
;;

;; Vars
;;

;; A `var` is a thread-local, isolated binding between a symbol and its value.
(def greeting "hi")
greeting

;; Alter the root binding of a var
(alter-var-root (var greeting) (fn [v] "bye"))
greeting

;; An alternative using the "reader macro", `#'`
(alter-var-root #'greeting (fn [v] "hello, again"))
greetingA

;; Remember, although one can alter a `var`, one is encouraged to **not**
;; alter vars. Instead, use other mechanisms like an `atom`.

;; Atoms
;;

;; An `atom` provides uncoordinate, synchronous access to a value.
;;
;; They are the most common way of managing state within Clojure.

;; An `atom` is used for atomic, stateful operations. They are atomic in that
;; they support change in such a way that all "observers" (or consumers) of
;; the `atom` always see a consistent value. In other words, an `atom`
;; prevents the race conditition in which
;;
;; - Two threads attempt to change the value of an `atom`
;; - One thread reads the value of the `atom` to compute a new value and
;;   is interrupted by a second thread
;; - The second thread **both** reads the value of the `atom` **and**
;;   changes it
;; - The first thread now writes a new value to the `atom`
;;
;; Consequently, the change made by the second thread is "lost" because
;; the first thread never "saw" the value written by the second thread.

;; Define a `var` whose value is an `atom`.
(def counter (atom 0))
@counter

;; Atomically swap the current value of `counter` with the value
;; resulting from calling the function, `inc` on the current value
;; of `counter`.
;;
;; Remember, Clojure convention appends an exclamation point, "!", to the
;; name of a function that has **side effects**.
(swap! counter inc)
@counter

;; Similarly, the `reset!` function sets the value of an `atom` to a
;; new value (not a newly **computed** value).
(reset! counter 0)
@counter

;; Another, perhaps rarely used, operation on an `atom` is `compare-and-set!`.
;; This function takes three arguments. The first argument is the name of
;; the atom. The second value is the expected and current value of the atom.
;; The third argument is the new value of the atom.
;;
;; The function, `compare-and-set!`, sets the value of the atom to the
;; new value if and only if the **current value** of the atom equals the
;; second value supplied to the function.
(compare-and-set! counter 0 10)
@counter

;; If `compare-and-set!` actually changes the value of the atom, it returns
;; `true`; otherwise it returns false.
(compare-and-set! counter 0 10)

;; Remember, the functions `reset!` and `swap!` continuously and repeatedly
;; loop, trying to change value of the atom supplied as the first argument.
;; This behavior can result in a "busy spin" operation trying to change
;; the value if multiple threads are contending to change the value of the
;; atom "simultaneously".

;; The reader macro, `@counter`, is equivalent to the expression,
;; `(deref counter)`
(deref counter)
@counter
