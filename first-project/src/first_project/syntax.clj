(ns first-project.syntax)

;; Basic syntax
;;

;; Functions

; 1 + 2 ==
(+ 1 2) ;; prefix notation

; System.out.println("hello, world!");
(println "Hello, world!") ;; Same form as addition

;; These expressions are all **forms**.

;; Clojure literals

;; a vector
[1 2 3]

;; a map
{:hi "there"}

;; a list
'(1 2 3)

;; Remember, many built-in functions apply to more than one collection type
(assoc {} 0 "hello")

(assoc [] 0 "hello")
