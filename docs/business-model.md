# Business Model: Independent SBA Set-Aside & Socio-Economic Certification Compliance Service — United States

## Classification

- Repository: `cloud-itonami-iso3166-usa-sba`
- ISO 3166 (agency-level): `USA-SBA`, parent `USA`
- Ooyake cross-reference: `gov.usa.sba` (Small Business Administration)
- Activity: 8(a), HUBZone, WOSB, SDVOSB certification navigation

## Customer

- an operator already using `cloud-itonami-iso3166-usa` whose contract
  touches Small Business Administration rules or buying channels
- a foreign SME entering a Small Business Administration-specific public program for the first time

## Offer

- walkthrough and evidence checklist for: 8(a), HUBZone, WOSB, SDVOSB certification navigation
- ongoing regulatory-change monitoring for this body's public sources
- compliance-audit export package

## Trust Controls

- `:filing/submit` never auto-commits at any phase
- fabricated regulatory claims are HARD holds
- not legal advice — cite https://www.sba.gov/

## Boundary

- **`cloud-itonami-iso3166-usa`**: country coordinator (general U.S. market entry)
- **`com-etzhayyim-ooyake`**: read-only civic atlas (never acts as the body)
