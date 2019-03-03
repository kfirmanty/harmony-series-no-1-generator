(ns harmony-series-no-1.views
  (:require [re-frame.core :as re-frame]
            [harmony-series-no-1.subs :as subs]
            [harmony-series-no-1.events :as events]
            [clojure.string :as s]))

(def num->text
  {1 "one"
   2 "two"
   3 "three"
   4 "four"
   5 "five"
   6 "six"
   7 "seven"
   8 "eight"
   9 "nine"
   10 "ten"
   11 "eleven"
   12 "twelve"})

(defn tone-generation->text [{:keys [tone-duration repetitions same?]}]
  (str (s/join " " [(-> repetitions num->text s/capitalize)
                    (-> tone-duration name (s/replace "-" " "))
                    (if (= repetitions 1)
                      "tone"
                      "tones")
                    (when same? "(the same each time)")])))

(defn pause->text [{:keys [optional?]}]
  (if optional?
    "[Pause.]"
    "Pause."))

(defn wrap-in-bold [current-line {:keys [line]} text]
  (if (= current-line line)
    [:b text]
    text))

(defn part->text [[pause pairs] current-line]
  [:div
   (let [pause-text (pause->text pause)]
     (if (= 0 current-line)
       [:b pause-text]
       [:div pause-text]))
   [:div
    [:br]
    [:br]]
   (interleave (->> pairs
                    (mapcat (fn [{:keys [tone-generation pause]}]
                              [(wrap-in-bold current-line tone-generation (tone-generation->text tone-generation))
                               (wrap-in-bold current-line pause (pause->text pause))])))
               (repeat [:div [:br] [:br]]))])

(defn main-panel []
  (let [parts @(re-frame/subscribe [::subs/parts])
        current-lines @(re-frame/subscribe [::subs/current-lines])
        playing? @(re-frame/subscribe [::subs/playing?])]
    [:article
     [:h1 "harmony series no. 1 generator"]
     [:p {:class "subtitle"} "based on composition by Michael Pisaro"]
     [:div {:on-click #(re-frame/dispatch (if playing?
                                            [::events/stop]
                                            [::events/play]))
            :style {:border-style :solid
                    :border-width :thin
                    :display :inline-block}}
      (if playing?
        "Stop playing"
        "Start playing")]
     (for [part (sort (keys parts))]
       ^{:key part}[:section
                    [:h2 (str "(Part " part ")")]
                    [part->text (:part (get parts part)) (get current-lines part)]])]))
