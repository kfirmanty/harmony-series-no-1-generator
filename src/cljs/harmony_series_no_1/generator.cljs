(ns harmony-series-no-1.generator
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators]
            [clojure.spec.gen.alpha :as g]))

(s/def ::optional? boolean?)

(s/def ::same? boolean?)

(s/def ::pause (s/keys :req-un [::optional?]))

(s/def ::tone-duration #{:very-long :long :short})
(s/def ::repetitions (s/int-in 1 13))

(s/def ::tone-generation (s/and (s/keys :req-un [::tone-duration ::repetitions]
                                        :opt-un [::same?]) (fn [{:keys [tone-duration repetitions]}]
                                                             (condp = tone-duration
                                                               :very-long (< repetitions 3)
                                                               :long (< repetitions 8)
                                                               :short (< repetitions 13)))))

(s/def ::pair (s/keys :req-un [::tone-generation ::pause]))

(s/def ::part (s/tuple ::pause (s/coll-of ::pair :count 4)))

(s/def ::personality #{:eager :mellow})

(s/def ::instrument #{:sine :triange :saw :pulse})

(defn scale-from [freq scale]
  (let [step (/ freq 12)]
    (map #(+ freq (* % step)) scale)))

(def minor-pentatonic [0 3 5 7 10 12])

(s/def ::frequencies #{(scale-from 440 minor-pentatonic) (scale-from 220 minor-pentatonic)})

(s/def ::performer (s/keys :req-un [::personality ::instrument ::frequencies]))

(defn generate [spec]
  (-> spec s/gen g/generate))

(defn generate-part []
  (generate ::part))

(defn generate-performer []
  (generate ::performer))
