(ns harmony-series-no-1.subs
  (:require
   [re-frame.core :as re-frame]
   [harmony-series-no-1.db :as db]))

(re-frame/reg-sub
 ::parts
 (fn [db]
   (:parts db)))

(re-frame/reg-sub
 ::current-lines
 (fn [db]
   (:current-lines db)))

(re-frame/reg-sub
 ::playing?
 db/playing?)
