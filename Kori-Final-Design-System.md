# কড়ি (Kori) Final Design System Specification

## 1. Principles
- **Quiet, Editorial, Dignified (শান্ত, মার্জিত, পাঠযোগ্য)**: Designed like a timeless personal notebook, not a flashy fintech or betting app.
- **No Cheap Dashboard Clutter**: Strictly avoids gradients, glassmorphism, glow effects, neon accents, and decorative gold borders.
- **High Contrast & Accessibility**: Meets WCAG AAA/AA standards across all light and dark variants.

---

## 2. Layout Geometry & Spacing Grid (8dp Baseline)
- **Screen Margin / Padding**: `20.dp`
- **Section Gap**: `28.dp`
- **Surface / Card Padding**: `18.dp`
- **List Row Vertical Padding**: `14.dp`
- **Component Gap**: `12.dp`
- **Surface Corner Radius**: `16.dp` to `20.dp`
- **Primary Button Height**: `52.dp` (Minimum interactive touch target: `48.dp`)
- **Body Text Minimum**: `16.sp`
- **Metadata Text Minimum**: `13.sp`

---

## 3. The Three Curated Color Identities

### A. কালি ও কাগজ (Ink and Paper) — Default
Inspired by classic Bengali literary publications and warm off-white newsprint:
- **Light Theme**:
  - Background (Canvas): `#FBF9F5` (Warm Cream)
  - Surface: `#FFFFFF` (Pristine White)
  - Primary / Text Main: `#1E2522` (Deep Forest Charcoal)
  - Accent / Primary Soft: `#EBE6DC` (Soft Sandstone)
  - Border / Outline: `#E0DCD3`
- **Dark Theme**:
  - Background: `#121514` (Deep Charcoal Black)
  - Surface: `#1A1E1C` (Muted Graphite)
  - Primary / Text Main: `#EDE8DE` (Parchment White)
  - Accent / Primary Soft: `#252B28`
  - Border / Outline: `#2C332F`

### B. মধ্যরাতের নীল (Midnight Indigo)
Evokes the tranquil quiet of late-night contemplation:
- **Light Theme**:
  - Background: `#F4F6F9` (Frost Mist)
  - Surface: `#FFFFFF`
  - Primary: `#182A4A` (Royal Navy)
  - Accent: `#DCE4F0`
- **Dark Theme**:
  - Background: `#0D1524` (Deep Abyss)
  - Surface: `#152035` (Midnight Navy)
  - Primary / Text Main: `#E4ECF7`
  - Accent: `#1F3050`

### C. বারগান্ডি ডায়েরি (Burgundy Journal)
Rich, tactile leather-bound journal aesthetic:
- **Light Theme**:
  - Background: `#FAF5F5` (Rose Mist)
  - Surface: `#FFFFFF`
  - Primary: `#4A1521` (Deep Bordeaux)
  - Accent: `#F2DEE2`
- **Dark Theme**:
  - Background: `#1D0B10` (Dark Wine)
  - Surface: `#291219` (Velvet Maroon)
  - Primary / Text Main: `#F7E4E8`
  - Accent: `#3D1B25`

### Functional Semantic Colors
- **Income (আয়)**: `#1B7F54` (Forest Green - Clear, reassuring, non-fluorescent)
- **Expense (ব্যয়)**: `#B3261E` (Crimson Terracotta - Distinct, cautious, non-jarring)
- **Warning / Budget Limit**: `#B26A00` (Warm Amber)
- *Color Independence*: Meaning is always reinforced with icons (↑ / ↓), +/- symbols, and explicit Bengali labels (আয় / ব্যয়).
