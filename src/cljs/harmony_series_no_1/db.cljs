(ns harmony-series-no-1.db)

(def default-db
  {:parts {}
   :playing? false})

(defn set-parts [db parts]
  (assoc db :parts parts))

(defn playing [db master-effects]
  (assoc db :playing? true
         :master-effects master-effects))

(defn stop [db]
  (assoc db :playing? false))

(defn playing? [db]
  (:playing? db))

(defn parts-ids [db]
  (-> db :parts keys))

(defn part [db id]
  (get-in db [:parts id]))

(defn set-current-line-for-part [db id line]
  (assoc-in db [:current-lines id] line))
