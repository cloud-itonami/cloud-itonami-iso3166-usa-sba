(ns statute.facts-test
  "Offline invariants for the USA-SBA citation catalog.

  These tests never touch the network -- the live gate
  (`tools/verify_citations.cljs`) owns the question `is this still true of the
  world?`. What these own is the question `is this catalog internally honest?`:
  that no entry claims a jurisdiction it does not cite, that the URL a human
  is sent to is the same node the machine verified, and that the one
  substantive trap this catalog exists to close (SDVOSB certification lives in
  part 128, not part 125) cannot be silently un-closed by a later edit."
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.lang.text :as str]
            [statute.facts :as facts]))

(def entries (facts/entries "USA-SBA"))

(deftest catalog-is-not-empty
  ;; Evidence floor: a catalog that lost its entries must fail these tests
  ;; rather than pass them vacuously. Every `doseq`/`every?` below is true of
  ;; the empty collection.
  (is (seq entries) "USA-SBA has no entries -- every other test here would pass vacuously")
  (is (>= (count entries) 10)
      "the blueprint promises 8(a), HUBZone, WOSB and SDVOSB navigation; that
       cannot be backed by a handful of citations")
  (is (seq facts/absences) "absences is empty, but this catalog's central claim IS an absence"))

(deftest every-entry-is-well-formed
  (doseq [e entries]
    (testing (:statute/id e)
      (doseq [k [:statute/id :statute/title :statute/jurisdiction :statute/kind
                 :statute/law-number :statute/url :statute/url-provenance
                 :statute/cfr-title :statute/cfr-node :statute/verified-via
                 :statute/verified-label :statute/verified-at :statute/topic]]
        (is (contains? e k) (str "missing " k)))
      (is (seq (:statute/topic e)) "entry carries no topic, so no query can reach it")
      (is (= :official-ecfr (:statute/url-provenance e))
          "provenance must name the official source, not a summary site"))))

(deftest ids-are-unique
  (let [ids (map :statute/id entries)]
    (is (= (count ids) (count (set ids)))
        (str "duplicate ids: " (->> ids frequencies (filter #(> (val %) 1)) (map key))))))

(deftest ids-are-namespaced-to-this-leaf
  (doseq [e entries]
    (is (str/starts-with? (:statute/id e) "usa-sba.")
        (str (:statute/id e) " is not namespaced to this leaf, so it can collide
              with the country coordinator's catalog when the two compose"))))

(deftest every-entry-cites-the-jurisdiction-it-claims
  (doseq [e entries]
    (is (= "USA-SBA" (:statute/jurisdiction e)) (:statute/id e))))

(deftest cfr-title-agrees-with-the-verification-url
  ;; A verified-via pointing at a different title than :statute/cfr-title means
  ;; the label was confirmed against the wrong document.
  (doseq [e entries]
    (testing (:statute/id e)
      (is (str/includes? (:statute/verified-via e)
                         (str "title-" (:statute/cfr-title e) ".json"))
          (str ":statute/cfr-title is " (:statute/cfr-title e)
               " but it was verified against " (:statute/verified-via e))))))

(deftest law-number-agrees-with-cfr-title
  (doseq [e entries]
    (is (str/starts-with? (:statute/law-number e) (str (:statute/cfr-title e) " CFR "))
        (str (:statute/id e) ": law-number " (pr-str (:statute/law-number e))
             " does not begin with title " (:statute/cfr-title e)))))

(deftest url-agrees-with-the-node-that-was-verified
  ;; The human URL and the machine-verified node must be the same place.
  ;; Otherwise a reader opens one regulation while the gate green-lights another.
  (doseq [e entries]
    (testing (:statute/id e)
      (is (str/starts-with? (:statute/url e)
                            (str "https://www.ecfr.gov/current/title-" (:statute/cfr-title e) "/"))
          "url is not an eCFR address for the cited title")
      (doseq [[t i] (:statute/cfr-node e)]
        (is (str/includes? (:statute/url e) (str "/" (name t) "-" i))
            (str "url " (:statute/url e) " does not contain " (name t) " " i))))))

(deftest every-entry-is-under-the-sba-chapter
  ;; The scope claim: this leaf is the SBA-specific layer. An entry outside
  ;; 13 CFR chapter I belongs to the country coordinator, not here.
  ;;
  ;; Checked on the structured node path, NOT by substring on the URL. The
  ;; sibling leaf usa-ftc shipped a bug of exactly that kind: "/chapter-I" is
  ;; a substring of "/chapter-II", so the check written to exclude another
  ;; agency's chapter admitted all of them.
  (doseq [e entries]
    (testing (:statute/id e)
      (is (= 13 (:statute/cfr-title e)) "not in 13 CFR")
      (is (= [:chapter "I"] (first (:statute/cfr-node e)))
          (str "node path starts at " (pr-str (first (:statute/cfr-node e)))
               ", not 13 CFR chapter I")))))

(deftest node-paths-descend
  ;; A path must go chapter -> part -> (subpart|section), never skip up.
  (let [rank {:chapter 0 :subchapter 1 :part 2 :subpart 3 :section 4 :appendix 4}]
    (doseq [e entries]
      (let [ranks (map (comp rank first) (:statute/cfr-node e))]
        (is (every? some? ranks)
            (str (:statute/id e) ": unknown node type in " (pr-str (:statute/cfr-node e))))
        (is (apply < ranks)
            (str (:statute/id e) ": node path does not strictly descend: "
                 (facts/node-path (:statute/cfr-node e))))))))

(deftest sdvosb-certification-points-at-part-128-not-125
  ;; The single domain fact this catalog exists to get right. SBA took VOSB/
  ;; SDVOSB certification over from the VA and it became its own part; a lot
  ;; of surviving guidance still says 125. If a later edit re-files SDVOSB
  ;; certification under 125, this fails.
  (let [sdvosb (facts/by-topic "USA-SBA" :socioeconomic-sdvosb)
        certs (filter #(contains? (:statute/topic %) :certification) sdvosb)]
    (is (seq certs) "no SDVOSB certification entry at all")
    (doseq [e certs]
      (is (= [:part "128"] (second (:statute/cfr-node e)))
          (str (:statute/id e) " files SDVOSB certification under "
               (facts/node-path (:statute/cfr-node e))
               " -- certification is 13 CFR part 128, not part 125")))))

(deftest the-four-promised-programs-are-all-covered
  ;; README/blueprint promise 8(a), HUBZone, WOSB, SDVOSB navigation.
  ;; A promise with no citation behind it is the thing this catalog forbids.
  (doseq [topic [:socioeconomic-8a :socioeconomic-hubzone
                 :socioeconomic-wosb :socioeconomic-sdvosb]]
    (is (seq (facts/by-topic "USA-SBA" topic))
        (str "blueprint promises " topic " navigation with no citation behind it"))))

(deftest size-standards-are-cited
  ;; Every program above is gated on being small in the first place. A catalog
  ;; that certifies without citing part 121 sends an ineligible operator into
  ;; an application.
  (is (seq (facts/by-topic "USA-SBA" :size-eligibility))
      "no size-eligibility citation; every other program depends on it"))

(deftest absences-are-well-formed
  (doseq [a facts/absences]
    (testing (:absence/id a)
      (doseq [k [:absence/id :absence/jurisdiction :absence/claim
                 :absence/cfr-title :absence/absent-label :absence/absent-at-level
                 :absence/verified-via :absence/verified-at :absence/see-instead]]
        (is (contains? a k) (str "missing " k)))
      (is (str/includes? (:absence/verified-via a)
                         (str "title-" (:absence/cfr-title a) ".json")))
      (let [alt (:absence/see-instead a)]
        (is (every? #(contains? alt %)
                    [:statute/law-number :statute/url :statute/cfr-title
                     :statute/cfr-node :statute/verified-via :statute/verified-label])
            "see-instead must be as fully cited as a catalog entry, or it is
             just a suggestion")))))

(deftest the-absence-does-not-contradict-the-catalog
  ;; If we say title 48 has no SBA chapter, no catalog entry may cite one.
  (doseq [a facts/absences]
    (doseq [e entries]
      (is (not= (:absence/cfr-title a) (:statute/cfr-title e))
          (str (:statute/id e) " cites CFR title " (:statute/cfr-title e)
               " while absence " (:absence/id a)
               " claims this agency has nothing there")))))

(deftest verified-dates-are-real-and-not-in-the-future
  (let [today (str (java.time.LocalDate/now))
        dates (concat (map :statute/verified-at entries)
                      (map :absence/verified-at facts/absences)
                      (keep (comp :statute/verified-at :absence/see-instead) facts/absences))]
    (is (seq dates))
    (doseq [d dates]
      (is (re-matches #"\d{4}-\d{2}-\d{2}" d) (str "not an ISO date: " d))
      (is (<= (compare d today) 0)
          (str "verified-at " d " is in the future; nothing was verified then")))))

(deftest cfr-titles-reports-every-title-touched
  ;; Including the one only an absence cites -- otherwise a reader building a
  ;; source list from this function silently omits title 48.
  (is (contains? (facts/cfr-titles) 13))
  (is (contains? (facts/cfr-titles) 48)
      "title 48 is cited by the absence and its alternative but is not reported"))

(deftest accessors-agree-with-the-catalog
  (is (= (count entries) (count (facts/entries "USA-SBA"))))
  (is (empty? (facts/entries "USA-NOPE")) "unknown code must be empty, not nil-punned")
  (doseq [e entries]
    (is (= e (facts/entry (:statute/id e))) (str "entry lookup failed for " (:statute/id e))))
  (is (nil? (facts/entry "usa-sba.does-not-exist"))))

(deftest readme-counts-match-the-catalog
  ;; The README states how many regulations this leaf cites. If the catalog
  ;; grows and the README does not, the published number becomes a lie.
  (let [readme (slurp "README.md")
        stated (some-> (re-find #"(\d+) API-confirmed citations" readme) second parse-long)]
    (is stated "README no longer states a citation count in the expected form")
    (is (= (count entries) stated)
        (str "README says " stated " citations, catalog has " (count entries)))))
