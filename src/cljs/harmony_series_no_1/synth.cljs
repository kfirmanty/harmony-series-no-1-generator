(ns harmony-series-no-1.synth
  (:require [tonejs]))

(defn rand-between [from to]
  (+ from (rand-int (- to from))))

(defn pause-time [short?]
  (if short?
    (rand-between 3 10)
    (rand-between 20 120)))

(defn pause->command [{:keys [optional? short? line]} performer]
  (if (and optional? (> (rand) 0.5))
    nil
    {:type :pause
     :time (pause-time short?)
     :line line}))

(defn tone-command [tone-duration {:keys [personality]} frequency line]
  {:time (condp = tone-duration
           :very-long (rand-between 20 120)
           :long (rand-between 10 20)
           :short (rand-between 3 5))
   :frequency frequency
   :type :tone
   :line line})

(defn tone-generation->command [{:keys [tone-duration repetitions same? line]} performer]
  (let [pause-stub {:short? true
                    :line line}]
    (if same?
      (let [frequency (rand-nth (:frequencies performer))]
        (interleave (repeatedly repetitions #(tone-command tone-duration performer frequency line))
                    (repeat (pause->command pause-stub performer))))
      (interleave (repeatedly repetitions #(tone-command tone-duration performer (rand-nth (:frequencies performer)) line))
                  (repeatedly #(pause->command pause-stub performer))))))

(defn pair->command [{:keys [pause tone-generation]} performer]
  [(tone-generation->command tone-generation performer)
   (pause->command pause performer)])

(defn part->synth-commands [[pause pairs] performer]
  (->> [(pause->command (assoc pause :short? true) performer)
        (map #(pair->command % performer) pairs)]
       flatten
       (filter some?)))

(def synth-settings (clj->js {:envelope {:attack 2.0
                                         :decay 0.025
                                         :sustain 0.3
                                         :release 0.9}
                              :oscillator {:type "triangle"}}))

(defn to-master [n]
  (.toMaster n))

(defn create-synth! [master-effects performer]
  (let [synth (-> (new js/Tone.Synth synth-settings)
                  to-master)]
    (set! (.-volume synth) -32)
    synth))

(defn set-master-defaults! []
  (set! (.-volume js/Tone.Master) -12))

(defn play-note! [synth frequency]
  (.triggerAttack synth frequency))

(defn release-note! [synth]
  (.triggerRelease synth))

(defn execute-command! [synth {:keys [type frequency] :as command}]
  (condp = type
    :tone (play-note! synth frequency)
    :pause (release-note! synth)
    (println "Unknown command: " command)))
