(ns clj-coinbase.core
  (:import java.time.LocalDateTime)
  (:require [clj-http.client :as client]))

(def api-base "https://api-public.sandbox.pro.coinbase.com")

(defn format-iso-8601
  "Formats UTC DateTime to be compatible with coinbase start/end bounds"
  [time]
  (.format time (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))

(defn ->utc-now []
  (LocalDateTime/now java.time.ZoneOffset/UTC))

(defn get-products
  "Retrieve current coinbase pro products"
  []
  (:body (client/get  (format "%s/products" api-base) {:as :json})))

(defn get-product
  "Retrieve a single coinbase pro product"
  [product-id]
  (:body (client/get (format "%s/products/%s" api-base product-id) {:as :json})))

(defn get-product-order-book
  "Retrieve product order book"
  [product-id]
  (:body (client/get (format "%s/products/%s/book" api-base product-id) {:as :json})))

(defn get-product-ticker
  "Retrieve product order book"
  [product-id]
  (:body (client/get (format "%s/products/%s/ticker" api-base product-id) {:as :json})))

(defn get-product-trades
  "Retrieve product order book"
  [product-id]
  (:body (client/get (format "%s/products/%s/trades" api-base product-id) {:as :json})))

(defn get-product-24hr-stats
  "Retrieve product 24 hours stats"
  [product-id]
  (:body (client/get (format "%s/products/%s/stats" api-base product-id) {:as :json})))

(defn get-currencies
  "Retrieve currencies"
  []
  (:body (client/get (format "%s/currencies" api-base) {:as :json})))

(defn get-historical-rates
  "Retrieve historical rates for the given ticker
   Start and end times must be UTC ZonedDateTime
   which will be formatted to 2021-08-14T02:12:00:00.000Z"
  [product-id start-time end-time granularity]
  (->> (client/get (format "%s/products/%s/candles?start=%s&end=%s&granularity=%s"
                           api-base
                           product-id
                           (format-iso-8601 start-time)
                           (format-iso-8601 end-time)
                           granularity)
                   {:as :json})
       :body
       (mapv (fn [[time open high low close]] {:time time :open open :high high :low low :close close}))))

(def candles (get-historical-rates
              "BTC-USD"
              (.minusHours (->utc-now) 1)
              (->utc-now)
              60))

(comment
  (do
    (get-products)
    (get-currencies)
    (get-product "BTC-USD")
    (get-product-order-book "BTC-USD")
    (get-product-ticker "BTC-USD")
    (get-product-trades "BTC-USD")
    (get-product-24hr-stats "BTC-USD")
    (doseq [candle candles]
      (println candle))))
