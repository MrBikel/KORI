# Kori Stage 1.4 Master Device Checklist

## Visual QA & Responsiveness Verification Guide

### 1. Typography & Scaling Matrix
- [x] **1.0x Scale**: Default rendering, crisp baselines, no clipping on ৳ or Bengali diacritics.
- [x] **1.15x Scale**: Preserves single-line titles and action button labels.
- [x] **1.3x Scale**: Layout wraps gracefully into multi-line rows; no overflow.
- [x] **1.5x Scale**: Form fields scroll with keyboard visible; Save action remains reachable.
- [x] **2.0x Scale**: Extreme accessibility scaling verified; buttons expand vertically rather than truncating text.

### 2. Device Width & Aspect Ratios
- [x] **320dp Compact**: Modal dialogs fill available width; buttons wrap or stack cleanly.
- [x] **360dp Standard**: Default phone portrait layout; comfortable 20dp margin.
- [x] **412dp Standard Large**: Optimized line lengths and editorial reading hierarchy.
- [x] **600dp Tablet / Foldable**: Center-aligned max-width constraints prevent excessive stretching.

### 3. Screen Inspection Checklist
- **আজ (Today)**:
  - Clean brand header "কড়ি" with Noto Serif Bengali.
  - Zero entries shows clean empty state without empty charts or zero cards.
  - With entries: Today income, expense, and net change shown; single local insight appears only when sufficient data exists.
- **ডায়েরি (Diary)**:
  - Date-grouped timeline with date headers in Noto Serif Bengali.
  - Clear entry items with description, source text, category icon, and signed amount.
  - Full-text search by description and source.
- **বিশ্লেষণ (Analysis)**:
  - Period comparisons (Today vs Yesterday, This Week vs Last Week, This Month vs Last Month).
  - Category shares and frugality goal progress.
  - No empty charts or non-functional AI cards.
- **আরও (More)**:
  - Expense & income categories management.
  - Frugality goals, notification reminder toggles, numeral system switcher.
  - CSV & JSON device export with Android Sharesheet.
  - Cloud marked as "এখনো সংযুক্ত নয়".
  - Diagnostics visible ONLY in diagnostic build.
