(ns second-project.csps
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.core.async :as a :refer [>! <! >!! <!!
                                              put! take!
                                              go go-loop
                                              alts! alts!!
                                              chan chan?
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

;; Let's put something on our channel. We use the "blocking put" function, `>!!`.
;; The blocking put function returns a boolean if successful.
(>!! c10 "hello")

;; If the channel has no available "slots", the blocking put function, `>!!`,
;; will cause the current thread to block until a consumer removes an item.
(>!! c10 "world")

;; The "blocking take" operator (function), `<!!` will take a single value
;; from a channel. If no values are available, executing this function will
;; **block** the thread executing the function until a value is available.
(<!! c10)

;; Channel functions follow the convention of using two exclamation points
;; to identify an operation that will block if no values are available on
;; the channel.
(<!! c10)

;; Remember, attempting to "take" from an empty channel using a
;; **blocking take** **will block** until some other thread "puts" an item
;; onto the channel.

;; Clojure also supports non-blocking operations on a channel. For example,
(defn our-tap
  "`our-tap` takes a core.async channel, `c`, and prints whatever messages
  have been put on `c`"
  [c]
  ;; When the channel is empty, `go-loop` will "park." This term means that
  ;; execution on the thread running the `go-loop` function will be suspended
  ;; and the thread made available to **other functions** until an item is
  ;; available on the channel for consumption. Consequently, this "parked"
  ;; thread **does not** consume any (or very few) operating system resources.
  ;; It simply results in a suspended thread that will be "unsuspended" when
  ;; an item is put onto the channel.
  (go-loop [msg (<! c)]
    (when msg
      (println "TAP:" msg)
      ;; go block "parks" here until a message comes through
      (recur (<! c)))))

;; Create a channel with 10 slots
(def tap-channel (chan 10))

;; Wrap the newly created channel with `out-tap`.
(our-tap tap-channel)

;; Because `tap-channel` is wrapped by `our-tap`, putting an item into the
;; channel results in "diagnostic" output when the item is consumed.
(>!! tap-channel "hello, channel!")

;; The `core.async` library supports one thread "putting" an item on
;; the channel "without" consuming the putting thread. For example,
(put! tap-channel "hello, put!")

;; The functions, `put!` and `take!`
;;

;; The function, `core.async/put!`, supports asynchronously sending messages
;; to a channel **without** a go block. For example,
(defn printer
  "Takes a collection, `coll`, and asynchronously puts all the values on a
  channel, `c`."
  [coll c]
  (when-first [n coll]
    (put! c n (fn [resp]
                (if (false? resp)
                  (println "Channel is closed.")
                  (printer (rest coll) c))))))

(printer (reverse (range 9)) tap-channel)

;; Similarly, the `take!` function supports consuming items from a channel
;; **without blocking** (but **with parking**). The `take!` function pulls
;; a value from its channel (parking when none available). When an element
;; is available, `take!`, similarly to `put!`, removes that item and
;; invokes the supplied callback function with that item. If the channel
;; is **closed**, `take!` will invoke its callback function with `nil`
(>!! c10 "from take")

(take! c10 (fn [value] (when value (println value))))

;; One can consume items from multiple channels (one at a time) using
;; `alts!` (non-blocking) or `alts!!` (blocking).
;;

;; The video assumes that a function named `chan?` exists that tests
;; if its argument is a channel. This implementation seems reasonable
;; (according to Google AI).
(defn chan?
  [c]
  (and (satisfies? clojure.core.async.impl.protocols/WritePort c)
       (satisfies? clojure.core.async.impl.protocols/ReadPort c)))

;; The function, `alts!`. takes a vector of channels and returns a vector
;; containing both a value and a channel from which that value wass pulled.
;; If more than one channel has available values, then `alts!` will pull
;; a value from one of the channels non-deterministically. Here's an example.
(defn alting
  "Takes a vector of channels, `vc`, and waits for a message from
  any of the channels."
  [vc]
  (assert (and (vector? vc)
               (chan? (first vc))) "Argument **must be** a vector of channels")
  (go-loop [[v ch] (alts! vc)]
    (when-not (= v :done)
      (do (println v)
          (recur (alts! vc))))))

;; Let's test our usage of `alts!`.
(defn alts-test
  []
  (let [chans (vec (repeatedly 20 chan))]
    (alting chans)
    (doseq [n (range 20)]
      (put! (get chans n)
            (str "hello from chanel " n "!" )))
    (put! (first chans) :done)))

;; And now, for a test!
(alts-test)

;; Truly. Random selection of channels at work.

;; Perform blocking operations in threads with `core.async` threads.
;;

;; `core.async` threads work like futures from Module 1 (Parallelism).
;; They execute a body of work in another thread and return the channel
;; immediately. Unlike futures, the last value of the thread is passed
;; to this returned channel - just like a go block. But, unlike a go block
;; we use **blocking** puts and takes to communicate in threads.

;; A channel with 10 slots.
(def tc (chan 10))

;; Create a thread looping over values taken from a channel.
(a/thread (loop [msg (<!! tc)]
            (when msg
              (println "Blocking TAP: " msg)
              ;; No parking here; the recursion **blocks** the thread.
              (recur (<!! tc)))))

;; If one puts a value onto this chanel, we'll see it tapped by
;; the thread.
(>!! tc "Hello, from thread!")

;; Now let's look at limitations of go blocks and go block best practices
;;

;; Go block limitations
;;

;; The `go` macro and function definitions
;;

;; Since `go` is a macro, it will actually **stop** at the boundary of a
;; function defition. For example, evaluating the following expression
;; throws an exception (an assertion error) but **not** at runtime.
#_(go (#(<! c10)))

;; The difficulty is that it may not be obvious if a called function
;; creates a new function internally which will result in this exception.
;;
;; Remember, it is always preferable to use `put` over a go block that
;; does not wait for a reply.

;; Using unbounded channels
;;

;; Techniques exist in to create back pressure; however, unbounded
;; channels are generally bad.
;;
;; One creates a unbounded channel by calling the zero arity `chan`
;; function. The number of pending `put` operations allowed by a
;; channel is **1024**. The throw exception in this scenario states,
;; "Consider using a windowed buffer."
#_(def unbounded (chan))
#_(doseq [n (range 1026)]
  (put! unbounded n))

;; The `core.async` package offers two channel implementations that
;; allow all `put` operations to occur **without** waiting.
;;
;; A dropping buffer allows the `put!` or `put!!` to succeed immediately by
;; **dropping** the item "put" in a buffer when it is full.

;; A (dropping) buffer with room for a single item.
(def dropper (chan (a/dropping-buffer 1)))

;; Put an item in the buffer.
(put! dropper "hi")

;; Put the next item "in the buffer".
(put! dropper "bye")

;; What's now in the buffer?
(take! dropper #(when % (println %)))

;; Note that we print out the message, "hi"; that is, the first item put
;; onto the channel.

;; A sliding buffer allows the `put!` or `put!!` to succeed by dropping the
;; "oldest" item in the channel and then adding the item supplied to the put.

;; A sliding buffer with room for a single item.
(def slider (chan (a/sliding-buffer 1)))

;; Put an item in the buffer
(put! slider "hi")

;; And put a second item in the buffer
(put! slider "bye")

;; What's in the buffer? The **last** item added; that is, "bye".
(take! slider #(when % (println %)))
