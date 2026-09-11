# cloud-itonami-iso3166-usa-sba

Open ISO 3166 **agency-level** Blueprint for **USA-SBA**: Small Business Administration
(parent country: **USA**).

This leaf designs a forkable OSS business for an independent operator
navigating **Small Business Administration**-specific public-procurement / regulatory compliance
(8(a), HUBZone, WOSB, SDVOSB certification navigation), composing with the country coordinator
`cloud-itonami-iso3166-usa`.

## What this is NOT

- **Not Small Business Administration.** Commercial compliance navigation only.
- **Not legal advice.** Cite official sources; route licensed work to counsel.

## Official surface

- https://www.sba.gov/

## Regulatory catalog (spec-basis)

`src/statute/facts.cljk` carries **15 API-confirmed citations** into
13 CFR chapter I — the SBA's own rule chapter — plus **1 checked absence**.
Each entry records the byte-exact `label_description` the official eCFR
versioner API returned, the node path it was read from, and the date.

| Programme | Where it actually lives |
|---|---|
| Am I small at all? | 13 CFR part 121 (size standards; gates everything below) |
| 8(a) Business Development | 13 CFR part 124 |
| HUBZone | 13 CFR part 126 (certification: subpart C) |
| WOSB / EDWOSB | 13 CFR part 127 (certification: subpart C) |
| **VOSB / SDVOSB** | **13 CFR part 128** — *not* part 125 |
| Subcontracting limits after award | 13 CFR 125.6 |
| Appeals / protests | 13 CFR part 134 |

**Two traps this catalog exists to close.**

1. **SDVOSB certification is part 128, not part 125.** SBA took certification
   over from the VA and it became its own part. Guidance written before that
   move still points at 125, which now holds only the government-contracting
   rules.
2. **SBA has no 48 CFR chapter.** It is not a FAR-supplement agency — there is
   no `SBAAR`. Searching 48 CFR for `Small Business Administration` returns 31
   nodes, one of them a subpart titled *Contracting With the Small Business
   Administration (the 8(a) Program)*, so the mistake is easy to make. Those
   are other agencies' rules about dealing with SBA. The procurement side lives
   in the government-wide **FAR Part 19**, which the absence record points at.

### Verifying

```bash
kbb --backend sci --classpath src tools/verify_citations.cljk   # live: re-fetches the eCFR API
kbb -M:test                                    # offline: catalog invariants
```

The live gate exits **0** verified / **1** drifted / **2** could-not-answer.
Exit 2 is never a pass: an empty catalog, a vanished absence-alternative or an
unreachable API all report 2 rather than silently succeeding.

It deliberately does **not** `curl` the `:statute/url` values — www.ecfr.gov
answers automated clients with HTTP 200 and a *Request Access* interstitial,
so a status check there is a false green. Verification goes through the
documented machine API instead.

## Capability layer

Resolves via `kotoba-lang/iso3166` (`USA-SBA`, parent `USA`).

## License

AGPL-3.0-or-later.
