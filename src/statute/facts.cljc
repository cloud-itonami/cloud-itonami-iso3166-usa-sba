(ns statute.facts
  "Agency-level compliance catalog for **USA-SBA** (United States Small Business
  Administration) -- the spec-basis behind this leaf's blueprint claim that an
  independent operator can navigate `8(a), HUBZone, WOSB, SDVOSB certification`.

  Scope. This is the SBA-specific layer only. Government-wide U.S. federal
  statutes (Sarbanes-Oxley, FLSA, ...) live in the country coordinator
  `cloud-itonami-iso3166-usa`'s `statute.facts` and are NOT duplicated here;
  the two catalogs compose, keyed `USA-SBA` -> `USA`.

  Provenance. Every entry cites the official eCFR (Electronic Code of Federal
  Regulations, GPO/Office of the Federal Register) address for the smallest
  stable unit that was independently confirmed. Nothing here is fabricated:
  each `:statute/verified-label` below is the byte-exact `label_description`
  returned by the eCFR versioner API on `:statute/verified-at`, and
  `tools/verify_citations.cljs` re-fetches that API and fails if any label
  drifts.

  Why the citation and the verification URL differ. `:statute/url` is the
  canonical human address a person should open. It is deliberately NOT the
  URL that was machine-verified: fetching www.ecfr.gov from an automated
  client returns HTTP 200 with a `Federal Register :: Request Access`
  interstitial rather than the regulation, so a status-code check against it
  would report success while proving nothing. We therefore verify through the
  documented machine API (`:statute/verified-via`) and record both. Do not
  `curl` the `:statute/url` and treat a 200 as confirmation -- it is not.

  What this catalog also records is an ABSENCE, and for SBA the absence is the
  single most misleading thing in the corpus. See `absences` below: **the SBA
  has no acquisition-regulation chapter of its own in 48 CFR.** It is not a
  FAR-supplement agency. An operator who greps 48 CFR for `Small Business
  Administration` gets 31 hits -- FAR subpart 19.8 is even titled `Contracting
  With the Small Business Administration (the 8(a) Program)` -- and can very
  reasonably conclude an `SBAAR` chapter exists somewhere. It does not. Those
  hits are *other* agencies' rules about dealing with SBA. The absence record
  carries a `:absence/see-instead` so the operator lands on the government-wide
  FAR part that actually governs the procurement side.

  One substantive trap the entries below pin down. SDVOSB certification is
  **13 CFR part 128** (Veteran Small Business Certification Program), not part
  125. SBA took certification over from the VA under the 2021 NDAA and the
  program got its own part; part 125 keeps the government-contracting rules
  (limitations on subcontracting, joint ventures, mentor-protege) but is no
  longer where a veteran-owned firm gets certified. Guidance written before
  that move still points at 125, which is why both parts are cited here with
  their live labels rather than described from memory.

  Extending. A regulation not in this table has NO spec-basis, full stop.
  Extend `catalog` with a real, API-confirmed citation; never invent an id,
  a URL, or a label."
  (:require [kotoba.lang.text :as str]))

(def ecfr-structure-api
  "eCFR versioner structure endpoints these entries were verified against.
  Keyed by CFR title. The date is the title's `up_to_date_as_of` at
  verification time, so the call is reproducible rather than `current`."
  {13 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
   48 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-48.json"})

(def catalog
  "iso3166 code -> vector of regulation entries.

  `USA-SBA` is an agency-level key (parent `USA`), matching
  `blueprint.edn`'s `:itonami.blueprint/iso3166`.

  `:statute/cfr-node` is the path from the CFR title down to the cited node,
  as [type identifier] pairs. The live gate walks the eCFR structure tree by
  this path -- it does not string-match the URL, because hierarchical
  identifiers nest as substrings of one another (`part 12` is a prefix of
  `part 121`, `subpart A` of nothing but matched loosely would hit any
  subpart) and a substring test silently accepts the wrong node."
  {"USA-SBA"
   [;; -- 13 CFR chapter I -- the SBA's own chapter -----------------------
    {:statute/id "usa-sba.13cfr-chapter-i"
     :statute/title "13 CFR Chapter I -- Small Business Administration (the agency's entire rule chapter)"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Chapter I"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Small Business Administration"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:agency-scope}}

    ;; -- Part 121 -- the threshold question: are you small at all? --------
    ;; Every other program below is gated on this. An operator who skips it
    ;; can complete a certification they were never eligible for.
    {:statute/id "usa-sba.size-121"
     :statute/title "Small business size regulations -- the size standards that gate every SBA program"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 121"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-121"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "121"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Small Business Size Regulations"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:size-eligibility}}

    {:statute/id "usa-sba.size-121-subpart-a"
     :statute/title "Size eligibility provisions and standards -- NAICS-keyed thresholds, affiliation rules"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 121 Subpart A"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-121/subpart-A"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "121"] [:subpart "A"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Size Eligibility Provisions and Standards"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:size-eligibility}}

    ;; -- Part 124 -- 8(a) Business Development ---------------------------
    {:statute/id "usa-sba.8a-124"
     :statute/title "8(a) Business Development / Small Disadvantaged Business status determinations"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 124"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-124"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "124"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "8(a) Business Development/Small Disadvantaged Business Status Determinations"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-8a}}

    {:statute/id "usa-sba.8a-124-subpart-a"
     :statute/title "8(a) Business Development -- admission, terms, graduation, termination"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 124 Subpart A"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-124/subpart-A"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "124"] [:subpart "A"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "8(a) Business Development"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-8a}}

    ;; -- Part 125 -- government contracting programs ----------------------
    ;; NOTE: this is NOT where SDVOSB certification lives any more. See 128.
    {:statute/id "usa-sba.govcon-125"
     :statute/title "Government contracting programs -- subcontracting limits, joint ventures, mentor-protege"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 125"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-125"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "125"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Government Contracting Programs"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:government-contracting}}

    ;; The single operating rule most often breached after award.
    {:statute/id "usa-sba.limitations-on-subcontracting-125-6"
     :statute/title "Limitations on subcontracting -- the performance percentage a set-aside prime must self-perform"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR 125.6"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-125/section-125.6"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "125"] [:section "125.6"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "What are the prime contractor's limitations on subcontracting?"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:government-contracting :ongoing-obligation}}

    ;; -- Part 126 -- HUBZone ---------------------------------------------
    {:statute/id "usa-sba.hubzone-126"
     :statute/title "HUBZone Program"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 126"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-126"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "126"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "HUBZone Program"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-hubzone}}

    {:statute/id "usa-sba.hubzone-126-certification"
     :statute/title "HUBZone certification -- application and decision procedure"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 126 Subpart C"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-126/subpart-C"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "126"] [:subpart "C"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Certification"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-hubzone}}

    ;; -- Part 127 -- WOSB / EDWOSB ---------------------------------------
    {:statute/id "usa-sba.wosb-127"
     :statute/title "Women-Owned Small Business Federal Contract Program"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 127"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-127"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "127"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Women-Owned Small Business Federal Contract Program"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-wosb}}

    {:statute/id "usa-sba.wosb-127-certification"
     :statute/title "Certification of EDWOSB or WOSB status"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 127 Subpart C"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-127/subpart-C"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "127"] [:subpart "C"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Certification of EDWOSB or WOSB Status"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-wosb}}

    ;; -- Part 128 -- VetCert: where SDVOSB certification ACTUALLY lives ---
    {:statute/id "usa-sba.vetcert-128"
     :statute/title "Veteran Small Business Certification Program -- the VOSB/SDVOSB certifying authority (moved from VA to SBA; NOT part 125)"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 128"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-128"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "128"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Veteran Small Business Certification Program"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-sdvosb}}

    {:statute/id "usa-sba.vetcert-128-certification"
     :statute/title "Certification of VOSB or SDVOSB status"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 128 Subpart C"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-128/subpart-C"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "128"] [:subpart "C"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Certification of VOSB or SDVOSB Status"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:certification :socioeconomic-sdvosb}}

    ;; -- Part 134 -- OHA: what you do when a decision goes against you ----
    {:statute/id "usa-sba.oha-134"
     :statute/title "Rules of procedure before the Office of Hearings and Appeals -- how a denial or a size protest is appealed"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 134"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-134"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "134"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Rules of Procedure Governing Cases Before the Office of Hearings and Appeals"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:appeals}}

    {:statute/id "usa-sba.oha-134-vosb-protests"
     :statute/title "OHA rules for VOSB/SDVOSB status protests -- the appeal path attached to part 128"
     :statute/jurisdiction "USA-SBA"
     :statute/kind :regulation
     :statute/law-number "13 CFR Part 134 Subpart J"
     :statute/url "https://www.ecfr.gov/current/title-13/chapter-I/part-134/subpart-J"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 13
     :statute/cfr-node [[:chapter "I"] [:part "134"] [:subpart "J"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-13.json"
     :statute/verified-label "Rules of Practice for Protests of Eligibility for Inclusion in the SBA Veteran Small Business Certification Program Database (VOSB or SDVOSB Status Protests)"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:appeals :socioeconomic-sdvosb}}]})

(def absences
  "Checked NON-existence. These are facts, not gaps in the catalog, and the
  live gate re-verifies each one: if the node ever appears, the gate fails
  loudly rather than letting a stale `there is no such thing` stand.

  `:absence/see-instead` names the node an operator should read instead, and
  the gate verifies THAT one exists -- an absence whose alternative has itself
  gone away is not a usable answer."
  [{:absence/id "usa-sba.no-48cfr-chapter"
    :absence/jurisdiction "USA-SBA"
    :absence/claim
    (str "The Small Business Administration has no acquisition-regulation "
         "chapter in 48 CFR. There is no `SBAAR`. Unlike DoD (chapter 2), VA "
         "(chapter 8) or DOE (chapter 9), SBA is not a FAR-supplement agency: "
         "it administers the small-business programs that OTHER agencies' "
         "contracting officers apply, so the procurement-side rules live in "
         "the government-wide FAR, not in an SBA chapter.")
    :absence/why-an-operator-would-assume-otherwise
    (str "48 CFR mentions `Small Business Administration` in 31 nodes, "
         "including a FAR subpart literally titled `Contracting With the "
         "Small Business Administration (the 8(a) Program)`. Searching the "
         "title for the agency name therefore returns a great deal of text "
         "and no signal that none of it is SBA's own chapter.")
    :absence/cfr-title 48
    :absence/absent-label "Small Business Administration"
    :absence/absent-at-level :chapter
    :absence/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-48.json"
    :absence/verified-at "2026-08-19"
    :absence/see-instead
    {:statute/title "FAR Part 19 -- Small Business Programs (government-wide; the procurement side of every program in 13 CFR chapter I)"
     :statute/law-number "48 CFR Part 19"
     :statute/url "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-D/part-19"
     :statute/cfr-title 48
     :statute/cfr-node [[:chapter "1"] [:subchapter "D"] [:part "19"]]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-48.json"
     :statute/verified-label "Small Business Programs"
     :statute/verified-at "2026-08-19"}}])

;; ---------------------------------------------------------------------------
;; Accessors
;; ---------------------------------------------------------------------------

(defn entries
  "All catalog entries for `code` (e.g. \"USA-SBA\"). Empty vector if unknown."
  [code]
  (get catalog code []))

(defn entry
  "The single entry with `:statute/id` = `id`, or nil."
  [id]
  (->> (vals catalog) (apply concat) (filter #(= id (:statute/id %))) first))

(defn by-topic
  "Entries for `code` carrying `topic` in `:statute/topic`."
  [code topic]
  (filterv #(contains? (:statute/topic %) topic) (entries code)))

(defn topics
  "The set of topics present for `code`."
  [code]
  (into #{} (mapcat :statute/topic) (entries code)))

(defn cfr-titles
  "CFR titles this catalog cites, including those cited only by an absence."
  []
  (into (sorted-set)
        (concat (map :statute/cfr-title (apply concat (vals catalog)))
                (map :absence/cfr-title absences)
                (keep (comp :statute/cfr-title :absence/see-instead) absences))))

(defn node-path
  "Human-readable rendering of a `:statute/cfr-node` path, e.g.
  `chapter I > part 128 > subpart C`."
  [node]
  (str/join " > " (map (fn [[t i]] (str (name t) " " i)) node)))
