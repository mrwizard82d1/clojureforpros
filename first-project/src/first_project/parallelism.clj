(ns first-project.parallelism)

;; Demonstrate the basic Clojure concurrency primitives.
;;

;; This section is very short because the community has almost completely
;; moved to using communicating sequently processes (`core.async`) instead
;; of using these primitives.

;; Futures
;;

;; A future is a simple concurency primitive. A future executes on a
;; thread and **caches** the result for future use.
(def f (future (Thread/sleep 2000) (println "done") 100))

;; Like other reference types, one can (repeatedly) query the value
;; of a future using `deref`. If one invokes `deref` **before** the
;; other thread has computed its value, our thread will **block**.
@f
(deref f)

;; If one invokes `deref` **before** the other thread has computed
;; its value, the thread invoking `deref` will **block**.
(def slower-f (future (Thread/sleep 10000) (println "done later") 101))
(deref slower-f)

;; Parallel mapping (`pmap`)
;;

;; The function, `pmap` is like `map` except that it uses **futures** to
;; execute the mapping function on the sequence.
(map inc (range 9))
(pmap inc (range 9))

;; Remember, only use `pmap` if the time complexity of the invoked
;; function dominates the execution time of the entire operation. Otherwise,
;; you will spend all your time **coordinating** the calculated results
;; instead of simply **calculating** the results.
;;
;; Additionally, remember that `pmap` is "semi-lazy". This expression means
;; that the computation will stay ahead of the realization.
