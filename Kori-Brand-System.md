# Kori (কড়ি) - Brand System & Visual Identity Specification

## 1. Brand Identity & Strategy

| Attribute | Specification |
|---|---|
| **Bengali Brand Name** | **কড়ি** |
| **English Working Name** | **Kori** |
| **Visible Launcher Label** | **কড়ি** |
| **Proposed Store Title** | **কড়ি: আয়-ব্যয়ের ব্যক্তিগত খাতা** |
| **Primary Tagline** | **প্রতিটি কড়ির নির্ভুল হিসাব** |
| **Alternative Tagline** | **ছোট খরচ থেকে পুরো হিসাব** |
| **Brand Personality** | Bengali-first, personal, precise, calm, private, trustworthy, fast, premium, minimalist, and non-judgmental. |

---

## 2. Pending Brand-Clearance Tasks (Collision Disclaimers)

The name "Kori" / "কড়ি" has potential market and service-name collisions in commercial domains. Therefore:
- **Trademark Uniqueness**: Status pending formal trademark registry search in target jurisdictions.
- **Google Play Name Availability**: Status pending store title availability submission.
- **Domain Name Availability**: Status pending web domain registration lookup.
- **Social Handles**: Status pending handle availability audit across major platforms.

---

## 3. Visual Icon Direction & Assets

### A. Icon Philosophy
- **Core Symbol**: A single, clean vector representation of an abstract Cowrie Shell (কড়ি) monetary unit with subtle ledger ticks.
- **Style**: Flat vector construction, zero gradients, zero heavy drop-shadows, zero leather/realistic metallic textures.
- **Scalability**: Tested for strong visual clarity at 24px viewport width.

### B. Color Palette
- **Background Color**: `#0F5C4D` (Deep Teal)
- **Primary Foreground Color**: `#FFFFFF` (Pure White)
- **Restrained Accent Color**: `#F4D77B` (Warm Gold)

### C. Mipmap Density & Adaptive XML Vector Manifest
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)
- `mipmap-anydpi-v26/ic_launcher.xml` (Adaptive icon linking `@drawable/ic_launcher_background` and `@drawable/ic_launcher_foreground`).

---

## 4. Wordmark & Typography Rules

- **Primary Wordmark**: `কড়ি`
- **Typeface**: Bundled local `Hind Siliguri` SemiBold (`res/font/hind_siliguri.ttf`) under SIL Open Font License 1.1 (`LICENSES/OFL-HindSiliguri.txt`).
- **Grapheme Precision**: The Bengali letter "ড়" is rendered with explicit dot clearance for legibility at small viewport sizes.
- **Tagline Usage Policy**: Taglines (`প্রতিটি কড়ির নির্ভুল হিসাব`) are displayed exclusively on Onboarding, Splash, Welcome screens, and promotional materials. **Taglines are strictly omitted from the Dashboard header.**
