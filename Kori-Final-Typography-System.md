# কড়ি (Kori) Typography System Specification

## 1. Principles & Font Families
The typographic architecture of কড়ি honors the cultural and visual beauty of Bengali script, while maintaining strict functional clarity for financial recording.

### Primary UI Font: Hind Siliguri (হিন্দ শিলিগুড়ি)
- **Role**: Primary UI typeface across all inputs, numbers, buttons, lists, navigation tabs, and system messaging.
- **Characteristics**: Clean, geometric humanist Bengali sans-serif with high legibility at micro-sizes.
- **Genuine Static Weights**:
  - Regular (400) — `app/src/main/res/font/hind_siliguri_regular.ttf`
  - Medium (500) — `app/src/main/res/font/hind_siliguri_medium.ttf`
  - SemiBold (600) — `app/src/main/res/font/hind_siliguri_semibold.ttf`
  - Bold (700) — `app/src/main/res/font/hind_siliguri_bold.ttf`

### Editorial Accent Font: Noto Serif Bengali (নোটো সেরিফ বেঙ্গলি)
- **Role**: Restrained editorial accents only — used exclusively for:
  - The "কড়ি" wordmark
  - Today/Diary date headers
  - Empty-state headline
  - Major reflection section titles
- **Prohibited Uses**: Never used for amounts, numeric inputs, buttons, navigation labels, forms, or metadata.
- **Genuine Static Weights**:
  - Regular (400) — `app/src/main/res/font/noto_serif_bengali_regular.ttf`
  - Medium (500) — `app/src/main/res/font/noto_serif_bengali_medium.ttf`
  - SemiBold (600) — `app/src/main/res/font/noto_serif_bengali_semibold.ttf`

---

## 2. Type Hierarchy & Scale Tokens
- **Wordmark / Brand Header**: 28sp - 32sp (Noto Serif Bengali SemiBold)
- **Screen Title (স্ক্রিন শিরোনাম)**: 24sp (Hind Siliguri Bold)
- **Financial Amount Display (আয়-ব্যয় অঙ্ক)**: 28sp - 32sp (Hind Siliguri Bold)
- **Section Heading (উপ-শিরোনাম)**: 18sp (Hind Siliguri SemiBold)
- **Body Regular (মূল লেখা)**: 16sp (Hind Siliguri Regular)
- **Form Input & Buttons**: 16sp (Hind Siliguri Medium / SemiBold)
- **Metadata / Timestamps / Labels**: 13sp - 14sp (Hind Siliguri Regular / Medium)
- **Bottom Navigation Label**: 12sp (Hind Siliguri Medium)

---

## 3. Numerals & Localization
- Supports instant user-toggleable numeral systems:
  - **বাংলা সংখ্যা**: ১, ২, ৩, ৪, ৫, ৬, ৭, ৮, ৯, ০
  - **English Numerals**: 1, 2, 3, 4, 5, 6, 7, 8, 9, 0
- Standardized currency formatting: `৳[পরিমাণ]` with consistent space and comma groupings.
