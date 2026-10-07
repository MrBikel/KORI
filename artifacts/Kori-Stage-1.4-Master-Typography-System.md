# Kori Stage 1.4 Master Typography System

## Two-Family Bengali Editorial Typography System

### 1. Families & Files
#### Primary UI Family: Hind Siliguri
- Real bundled local OTF/TTF files:
  - `hind_siliguri_regular.ttf` (Weight: 400 Regular)
  - `hind_siliguri_medium.ttf` (Weight: 500 Medium)
  - `hind_siliguri_semibold.ttf` (Weight: 600 SemiBold)
  - `hind_siliguri_bold.ttf` (Weight: 700 Bold)
- License: `LICENSES/OFL-HindSiliguri.txt` (SIL Open Font License 1.1)

#### Editorial Accent Family: Noto Serif Bengali
- Real bundled local OTF/TTF files:
  - `noto_serif_bengali_regular.ttf` (Weight: 400 Regular)
  - `noto_serif_bengali_medium.ttf` (Weight: 500 Medium)
  - `noto_serif_bengali_semibold.ttf` (Weight: 600 SemiBold)
- License: `LICENSES/OFL-NotoSerifBengali.txt` (SIL Open Font License 1.1)

---

### 2. Exact Type Scale
| Token | Family | Weight | Size (sp) | Line Height (sp) | Application |
|---|---|---|---|---|---|
| **Wordmark** | Noto Serif Bengali | SemiBold (600) | 26 | 34 | App Brand Title ("কড়ি") |
| **Primary Amount** | Hind Siliguri | SemiBold (600) | 34 | 42 | Dominant Summary Figures |
| **Secondary Amount** | Hind Siliguri | SemiBold (600) | 22 | 30 | Card Amounts & Comparisons |
| **Transaction Amount**| Hind Siliguri | Medium (500) | 17 | 24 | List Row Amounts (`+৳৫০`, `−৳৪৫০`) |
| **Diary Date** | Noto Serif Bengali | Medium (500) | 21 | 30 | Timeline Date Headers |
| **Screen Title** | Hind Siliguri | SemiBold (600) | 22 | 30 | Screen Top Titles |
| **Section Title** | Hind Siliguri | SemiBold (600) | 18 | 26 | Section Headers |
| **Entry Title** | Hind Siliguri | Medium (500) | 16 | 24 | Item Row Titles |
| **Body** | Hind Siliguri | Regular (400) | 16 | 25 | Primary Text & Narrative |
| **Secondary Body** | Hind Siliguri | Regular (400) | 14 | 22 | Guidance & Subtitles |
| **Metadata** | Hind Siliguri | Regular (400) | 13 | 19 | Timestamps, Source Chips |
| **Primary Button** | Hind Siliguri | Medium (500) | 16 | 22 | Button Labels |
| **Bottom Navigation**| Hind Siliguri | Medium (500) | 13 | 18 | Nav Bar Labels |
| **Form Input** | Hind Siliguri | Regular (400) | 17 | 25 | Text Field Values |
| **Form Label** | Hind Siliguri | Medium (500) | 14 | 20 | Text Field Labels |
| **Empty State Title**| Noto Serif Bengali | Medium (500) | 20 | 30 | Empty State Headlines |
| **Insight Title** | Noto Serif Bengali | Medium (500) | 18 | 27 | Analytical Narrative Headers |

---

### 3. Glyph & Conjunct Verification
- Complex conjuncts supported: ক্ষ, জ্ঞ, ত্র, শ্র, ন্দ্র, ষ্ট্র, দ্ধ, স্ত্রী, প্রয়োজনীয়, মিতব্যয়িতা, বিদ্যুৎ, ঔষধ
- Currency and numerals: ৳, ০-৯, 0-9
- Signed amounts: Signed prefix aligned without digit clipping (`+৳`, `−৳`)
