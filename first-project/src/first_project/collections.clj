(ns first-project.collections
  (:require clojure.set))

;; Demonstrate Clojure collections and operation
;;

;; Lists
;; Sequential, heterogeneous collection

;; literal
'(1 2 3)

;; Using the `list` function
(list 1 2 3)

;; First list item
(first '(1 2 3))

;; zero-based
(nth '(1 2 3) 1)

;; all but the first element
(rest '(1 2 3))

;; Vectors
;; Eager, sequential, heterogeneous collections

;; literal
[1 :two "three"]

;; Using `vector`
(vector 1 :two "three")

;; The functions, `first`, `nth`, and `rest` all apply to vectors
(first [1 :two "three"])
(nth [1 :two "three"] 1)
(rest [1 :two "three"])

;; Use `get` to access items by index
(get [1 :two "three"] 0)

;; Can also use a vector **as a function** (whose argument is an `int`)
([1 :two "three"] 1)

;; The `assoc` function associates an item with a vector index (an `int`)
(assoc [1 :two "three"] 3 "four")  ;; append
(assoc [1 :two "three"] 0 "one")  ;; replace existing

;; We can also conjoin an item to a vector (at the end)
(conj [1 :two "three"] 4)

;; But using `conj` on a list appends to the **front**
(conj '(1 :two "three") 4)

;; Maps
;; Associative dictionaries of a key to a value (which may be a collection)
{:hi "there"}

;; An expression like `:hi` is a **keyword**
;; Its value is itself
:hi

;; Identify a value from its key using `get`
(get {:hi "there"} :hi)

;; A map will act as a function of its keys
({:hi "there"} :hi)

;; And a key will act as a function of a map
(:hi {:hi "there"})

;; But if a map **does not** contain a key, `nil` is returned
({:hi "there"} :hip)

;; We can add new associations to a map using `assoc`
(assoc {:hi "there"} :so_long "farewell")

;; Calling `assoc` can also re-map an existing key
(assoc {:hello "there"} :hello "Dave")

;; The `update` function changes the value associated with a key by calling
;; a **function** on the value associated with the specified key.
;; Remember that `update` returns a **new** map and not a **changed** map.
(update {:one 0} :one inc)

;; Remove items from a map by calling `dissoc`
(dissoc {:hi "there" :bye "then"} :bye)

;; The functions
;; - `get-in`
;; - `assoc-in`
;; - `update-in`
;; conveniently allow one to perform these same operations on **nested** maps
(get-in {:hi {:there "friend!"}} [:hi :there])
(assoc-in {:hi {:there "friend!"}} [:hi :there] "Gandalf!")
(update-in {:one {:two 3}} [:one :two] inc)

;; Sets
;; A collection of unique items
#{:hi :bye}
(hash-set :hi :bye)

;; One cannot supply duplicate items to a set literal. Including duplicates raises
;; an exception.
;;#{:hi :hi :bye}

;; But duplicates are fine if using `hash-set`
(hash-set :hi :hi :bye)

;; One can create a set from another collection
(set [1 2 3])

;; The other collection
(set [:hi :hi :there])

;; Sets support
;; - `conj` to add items and
;; - `disj` to remove items
(conj #{"hi"} "there")
(disj #{"hi" "there"} "there")
