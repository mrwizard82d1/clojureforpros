(ns first-project.core
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint
                                    pp]]))

(defn -main
  "Our program entry point"
  [& args]
  (pprint args)
  (let [input (if (nil? (first args))
                "world"
                (s/join ", " (vals (first args))))]
    (println (str "Hello, " input "!"))))
