(ns first-project.sequences-and-laziness)

;; Lazy sequences
;;

;; The single argument `range` function returns the values from 0 up-to
;; but not including the argument passed to `range`.
(range 7)

;; The two-arugement `range` function returns values from the first
;; argument up-to but not including the last argument passed to `range`.
(range 2 7)

;; The three-argument `range` function returns values from the first
;; argument up-to but not including the second argument with each
;; successive values separated from the previous value by the third argument.
(range 2 7 2)

;; In some ways, the most interesting variant of `range` is the zero
;; argument function, `(range)`. This variant lazily returns all integers
;; beginning with 0. As one might imagine, this is a rather large number
;; of integers.
(take 5 (range))

;; The form, `(range)`, generates an infinite sequence of integers;
;; however, all overloads of `range` use **lazy sequences**. These
;; sequences are often referred to as "lazy seqs". A lazy seq provides
;; the next value of the sequence **only** when it is needed (thus, the
;; adjective "lazy"). Because of this behavior, the expression
;; `(take 5 (range))` can be successfully evaluated because it only
;; requires the first 5 values of an infinite sequence.
;;
;; We describe generating a value from a sequence as "realizing" the
;; sequence.
;;
;; Additionally, a sequence which generates all its values immediately
;; is "eager".
;;
;; Because the REPL prints the result of an expression, it forces the
;; realization of all required values from a lazy sequence.
;;
;; Lazy sequences are great for large or infinite collections because
;; one need not realize all the values of the sequence eliminating
;; "memory pressure" from (some of) the evaluation process.
;;
;; As a practical matter, Clojure internally calculates next values
;; in "chunks." This lessens the "memory pressure" of infinite sequences
;; while providing the next values "quickly."
;;
;; Finally, as another practical matter, one typicially need not include
;; the chunked nature of sequences in one's mental model of code execution.

;; Generating lazy sequences
;;
