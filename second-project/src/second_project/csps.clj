(ns second-project.csps
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.core.async :as a :refer [>! <! >!! <!!
                                              put! take! go go-loop
                                              alts! alts!! chan
                                              sliding-buffer
                                              dropping-buffer]]
            ;; Or perhaps simply alias `core.async`
            ;; [clojure.core.async :as a]
            ;;
            ;; Apparently, the "popular abbreviation" for the
            ;; `core.async` package is "a". Sigh...
            ))

;; This video focuses on writing concurrent programs is Clojure.
;;

;; The dominant method for writing concurrent programs in Clojure is to
;; use the `core.async` library. The `core.async` library is a library
;; for programming communicating sequential processes (CSPs).
;;
;; One must install the `core.async` libraries as dependencies. The latest
;; version for projects using `deps.edn` files and for projects using
;; leiningen can be found on the `core.async` GitHub page,
;; `https://github.com/clojure/core.async`.

;; Go blocks
;;

;; Go blocks are a form that will execute their body in a special pool
;; of threads. For example,
(go (println "Hello, Go-block World!"))

;; As a mental model, one can think of a go block as spinning off a
;; thread of execution.
;;
;; Remember, a go block **immediately** returns to the parent thread.
;; **Beware**, one should **not** perform indefinite, blocking operations
;; in go blocks.

;; It is sometimes useful to have a go block execute a loop. For example,
(go (loop [l (range 10)]
      (when-first [n l]
        (println n)
        (recur (rest l)))))

;; The `core.async` package provides a convenience macro, `go-loop`, that
;; combines `go` and `loop`.
(go-loop [l (range 10)]
  (when-first [n l]
    (println n)
    (recur (rest l))))

;; **Remember**, go blocks **do not** join the main thread when finished.
;; Instead, the thread is returned to the pool of threads used for executing
;; go blocks. Most importantly, go block pass their result to a **channel**.
;;
;; A channel is a queue-like object for passing messages.

;; Channels
;;

;; By default, channels are **unbuffered**. Because they are unbuffered,
;; a producer will wait for a consumer to "arrive at the channel" before
;; moving on to other work. In other words, a channel is not a "fire and
;; forget" mechanism (like a Erlang/Elixir message queue). A producer and
;; a consumer must **communicate** to move data through the system.
(def c (chan))

;; One can create a channel with "slots" for items (a **buffered channel**).
;; This option allows producers to essentially "drop off" their data
;; **unless the channel is full**. For example, the following channel has
;; ten slots for data. Until all ten slots are filled, producers can simply
;; "drop off" their data and move on to other work.
(def c10 (chan 10))

;; One closes a chanel with the `close!` function. Remember, the training
;; "bang" character, '!', indicates an operation with side-effects.
(a/close! c)

;; Despite the "advice" from Google AI, apparently one cannot query a channel
;; using `a/closed?` to determine if it is closed. Some posts indicate that
;; if you want to determine if a channel is closed and you are consuming items,
;; a take will return `nil` from a closed channel.
(take! c #(if (nil? %)
            (println "I got nothin'.")
            (println "Got something")))
