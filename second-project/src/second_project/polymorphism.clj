(ns second-project.polymorphism)

;; Dynamic dispatch using multimethods
;;

;; Multimethods are "first-class, dynamic dispatch" in Clojure.
;; "Dynamic dispatch" means that we can change the behavior of
;; a function based on (any of) its (input) arguments.

;; The name of our multmethod is `make-safe`. The dispatch function is
;; `class` (which returns the Class of its argument). Although in this
;; case, we use the class of the function argument, we could write a
;; function that dispatches on any (or all) of the function arguments.
;; When this multimethod is invoked, Clojure will invoke the dispatch
;; function with the actual function parameter(s) as its arguments.
;; The result of the dispatch function identifies the concrete method
;; to invoke.
(defmulti make-safe class)

;; The `defmethod` macro(?) takes
;;
;; - The name of the method
;; - The value on which to dispatch. (In our case, the type of the
;;   argument because the dispatch function is the function `class`.)
;; - The argument(s) to the method
;; - The body of the method
(defmethod make-safe java.lang.Number
  [n]
  (bigdec n))
(make-safe 13)

(defmethod make-safe java.lang.String
  [n]
  (try
    (-> n
        (Double/parseDouble)
        (bigdec))
    (catch NumberFormatException nfe
      (println "String must be a string of numeric characters.")
      (bigdec 0.0))
    (catch Exception e
      (println "An unknown error converting from string to money.")
      (bigdec 0.0))))
(make-safe "17")

(defmethod make-safe clojure.lang.PersistentVector
  [n]
  (try
    (let [num-bytes
          ;; THe number of integers in the array
          (->> n (filter int?) (count))]
      (if (= (count n) num-bytes)
        (->> n
             (map char)
             (clojure.string/join)
             (Double/parseDouble)
             (bigdec))
        (throw (ex-info "Can only convert from vector of bytes"
                        {:input n}))))
    (catch NumberFormatException nfe
      (println "Vector must be bytes representing ASCII number characters")
      (bigdec 0.0))
    (catch Exception e
      (println "Error converting from vector of bytes to money")
      (bigdec 0.0))))
(make-safe [49 57])

(defmethod make-safe :default
  [_]  ;; We don't care about this binding
  (bigdec 0.0))
(make-safe :zork)

(map make-safe ["0.1" 0.1 [0x30 0x2e 0x31]])

;; Although `make-safe` dispatches on the type (`class`) of the input data,
;; one can actually dispatch on any or all of the input data because we
;; **define** the dispatch function.
;;
;; Multimethods have a small performance penalty (compared to normal function
;; invocation); however, it is "largely negligible."
;;
;; You may be wondering, "Why use multimethods instead of using a `cond`
;; form?" Multimethods allow us to decouple the **usage** of a method
;; from the code being run for that method. This indirection allows us
;; to **extend** a multimethod **without** impacting its calling function
;; or other method implementations.

;; From "Clojure for the Brave and True"

;; Specify the multi-method with its dispatch function
(defmulti full-moon-behavior (fn [were-creature]
                               (:were-type were-creature)))

;; Define a multimethod for a specific "instance" (a `:wolf`)
(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

;; Define a multimethod for another "instance" (`:simmons`)
(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

;; Invoke the multimethod for some instance (a `:wolf`)
(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})

;; Invoke the multimethod for another instance (a `:simmons`)
(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})

;; Protocols
;;

;; One mental model of a protocol is a "bag of functions for a type."
;;
;; We define a protocol using `defprotocol`.
(defprotocol MoneySafe
  "A type to convert other money types to `bigdec`."
  ;; This protocol has a single function (signature); however, one can
  ;; specify the signatures of many functions.
  (make-safe [this] "Coerce a type to be safe for money arithmetic."))

;; We can, alternatively, implement our multimethod example using this
;; protocol.
;;
;; The `extend-protocol` form allows one to extend a protocol to cover
;; a number of types in one code location.
(extend-protocol MoneySafe
  java.lang.Number
  (make-safe [this] (bigdec this))

  java.lang.String
  (make-safe [this]
    (try
      (-> this
          (Double/parseDouble)
          (bigdec))
      (catch NumberFormatException nfe
        (println "String must be a string consisting only of numbers."))
      (catch Exception e
        (println "Unknown error converting from string to money.")
        (throw e))))

  clojure.lang.PersistentVector
  (make-safe [this]
    (try
      (let [num-bytes (->> this (filter int?) (count))]
        (if (= (count this) num-bytes)
          (->> this
               (map char)
               (clojure.string/join)
               (Double/parseDouble)
               (bigdec))
          (throw (ex-info "Can only convert a vector of bytes."
                          {:input this}))))
      (catch NumberFormatException nfe
        (println "Vector must be bypets representing ASCII digit characters")
        (throw nfe))
      (catch Exception e
        (println "Error converting from vector of bytest to money")
        (throw e)))))

(defn ->money
  [x]
  (make-safe x))

(+' (->money "0.1") (->money 0.1) (->money [0x30 0x2e 0x31]))

;; Since no dispatch function exists, a protocol is actually compiled
;; **ahead-of-time** (unlike multimethods). This characteristics slightly
;; improves the performance of protocols compared to multimethods.

;; Another protocol example but using `extend-type`
;;
(defprotocol Normalizable
  (normalize [this] "Normalize the sensor reading to a 0-100 scale."))

(defprotocol ThresholdCheckable
  (check-threshold [this threshold]
    "Check if the reading breaches a specified threshold."))

;; Extend `java.lang.Integer` to implement these two protocols.
(extend-type java.lang.Integer
  Normalizable
  (normalize [this]
    (let [min-reading 0
          max-reading 1024]
      (-> this
          (- min-reading)
          (/ (- max-reading min-reading))
          (* 100))))

  ThresholdCheckable
  (check-threshold [this threshold]
    (if (> this threshold)
      (println "Threshold breached with reading:" this)
      (println "Reading within safe limits:" this))))

;; Interfaces
;;
;; Generally, a type is data that conforms to an interface. Additionally,
;; one can conceptualize an interface as a "bag of functions". Consequently,
;; a type is a "bag of function implementations."
;;
;; As a consequence, whenever we use an interface in our code, we can use
;; any type that implements that same 'bag of functions."

;; But what if we want to create our own type?
;;
;; We can create our own types using:
;;
;; - `reify`
;; - `deftype`
;; - `defrecord`

;; Create an anonymous type with `reify`.
;;
(defprotocol Notifiable
  (send-msg [this message]))

(defn notify-user [user-contact]
  (let [email-notifier (reify Notifiable
                         (send-msg [_ message]
                           (println (str "Sending email to " user-contact ": " message))))
        sms-notifier (reify Notifiable
                       (send-msg [_ message]
                         (println (str "Sending SMS to " user-contact ": " message))))
        notifiers (list email-notifier sms-notifier)]
    (doseq [notifier notifiers]
      (send-msg notifier "Thanks for buying Clojure for Pros!"))))

(notify-user ["foo@bar.com"])

;; `reify` is handy for creating "one-off" implementations of protocols.

;; Building on `reify`, we can create a named type with `deftype`.
(defprotocol Node
  "A protocol fo defining recursive binary trees."
  (get-value [this] "Returns the value of this node.")
  (get-left [this] "Returns the left child node.")
  (get-right [this] "Returns the right child node."))

(deftype BinaryTree [left right]
  Node
  (get-value [this] this)
  (get-left [this] left)
  (get-right [this] right))

(deftype Leaf [value]
  Node
  (get-value [this] value)
  (get-left [this] nil)
  (get-right [this] nil))

(defn search-tree
  "Returns true if `value` is in the tree `node`."
  [value node]
  (when node
    (or (= (get-value node) value)
        (search-tree value (get-left node))
        (search-tree value (get-right node)))))

(def tree-root
  (BinaryTree.
   (->BinaryTree (->BinaryTree (->Leaf :cool)
                               (->Leaf :beans))
                 (->BinaryTree (->Leaf :hi)
                               (->Leaf :okay)))
   (->BinaryTree (->BinaryTree (->Leaf :cool)
                               (->Leaf :beans))
                 (->Leaf :short))))

(search-tree :cool tree-root)

;; Similarly, we can use `defrecord` to create a type as well but with a
;; few **extras**.
;;
;; A Clojure record implements all the protocol implementations supplied
;; **plus** all the map members.
;;
;; It will create, behind the scenes,
;;
;; - A constructor
;; - Two factory functions

(defrecord SMSNotifier [user-contact]
  Notifiable
  (send-msg [this message]
    (println (str "Sending SMS to " user-contact ": " message))))

(SMSNotifier. "lajones")
(->SMSNotifier "lajones")
(map->SMSNotifier {:user-contact "lajones"})

(defrecord EmailNotifier [user-contact]
  Notifiable
  (send-msg [this message]
    (println (str "Sending email to " user-contact ": " message))))

(EmailNotifier. "lajones")
(->EmailNotifier "lajones")
(map->EmailNotifier {:user-contact "lajones"})

(defn create-notifiers
  []
  (let [user-contacts {"lajones" :email
                       "Graeme Crackers" :sms}]
    (map (fn [[user method]]
           (if (= method :email)
             (->EmailNotifier user)
             (->SMSNotifier user))) user-contacts)))

(defn notify-all
  []
  (doseq [notifier (create-notifiers)]
    (send-msg notifier "Thanks buying Clojure for Pros!")))

(notify-all)

;; The constructors are similar to creating a Java class representing our type.
;; However, we need not think of these created instances as Java objects. We
;; simply pass these instances to protocol functions like any other
;; Clojure type.
;;
;; Deciding whether to use `deftype` or `defrecord` depends on your goals.
;; Some people say, "Use `deftype` for **programming-related constructs**,
;; and use `defrecord` for **domain-related constructs**.-
;;
;; In the "real world", both are frowned upon for being "too object-oriented."

;; Perhaps the best use case is to use `reify`, `deftype`, and `defrecord`
;; to implement **Java interfaces**. The syntax is identical to implementing
;; protocols.
;;
;; For example, from clojuredocs.org,
(defrecord Thing [a]
  java.net.FileNameMap
  (getContentTypeFor [this fileName] (str a "-" fileName)))

(->Thing "hello.txt")
(:a (->Thing "hello.txt"))
