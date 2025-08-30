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
