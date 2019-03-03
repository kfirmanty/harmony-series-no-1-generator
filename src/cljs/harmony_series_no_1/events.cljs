(ns harmony-series-no-1.events
  (:require
   [re-frame.core :as re-frame]
   [harmony-series-no-1.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [harmony-series-no-1.generator :as gen]
   [harmony-series-no-1.synth :as synth]))

(re-frame/reg-event-fx
 ::initialize-db
 (fn-traced [_ _]
            {:db db/default-db
             :dispatch [::generate]}))

(defn add-indexes-to-part [[pause pairs]]
  [(assoc pause :line 0) (map-indexed (fn [i p]
                                        (-> p
                                            (update :pause assoc :line (+ (* 2 i) 2))
                                            (update :tone-generation assoc :line (+ (* 2 i) 1)))) pairs)])

(re-frame/reg-event-db
 ::generate
 (fn [db _]
   (db/set-parts db (into {} (for [i (range 1 5)]
                               (let [part (add-indexes-to-part (gen/generate-part))
                                     performer (gen/generate-performer)]
                                 [i {:part part
                                     :performer performer
                                     :commands (synth/part->synth-commands part performer)
                                     :id i}]))))))

(re-frame/reg-event-fx
 ::play
 (fn [{:keys [db]} _]
   (let [master-effects {}]
     ;;(synth/set-master-defaults!)
     {:db (db/playing db master-effects)
      :dispatch-later (for [p (db/parts-ids db)]
                        (let [part (db/part db p)]
                          {:ms 0 :dispatch [::execute-part (assoc  part
                                                                   :synth (synth/create-synth! master-effects (:performer part)))]}))})))

(re-frame/reg-event-db
 ::stop
 (fn [db]
   (db/stop db)))

(re-frame/reg-event-fx
 ::execute-part
 (fn [{:keys [db]} [_ {:keys [commands synth id] :as part}]]
   (when (and (db/playing? db) (not-empty commands))
     (synth/execute-command! synth (first commands))) ;;FIXME: ugly side effect
   (merge {:db db}
          (when (and (db/playing? db) (not-empty commands))
            {:dispatch-later [{:ms (-> commands first :time (* 1000))
                               :dispatch [::execute-part (assoc part :commands (rest commands))]}]
             :db (db/set-current-line-for-part db id (-> commands first :line))}))))
