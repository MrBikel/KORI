package com.aistudio.moneydiary.mndytr.domain.model

data class MoneyPaisa(val paisa: Long) : Comparable<MoneyPaisa> {

    init {
        require(paisa >= 0) { "Monetary amount cannot be negative: $paisa" }
    }

    operator fun plus(other: MoneyPaisa): MoneyPaisa {
        return MoneyPaisa(Math.addExact(this.paisa, other.paisa))
    }

    operator fun minus(other: MoneyPaisa): MoneyPaisa {
        require(this.paisa >= other.paisa) {
            "Subtraction would result in negative paisa ($paisa - ${other.paisa})"
        }
        return MoneyPaisa(Math.subtractExact(this.paisa, other.paisa))
    }

    operator fun times(multiplier: Long): MoneyPaisa {
        require(multiplier >= 0) { "Multiplier cannot be negative: $multiplier" }
        return MoneyPaisa(Math.multiplyExact(this.paisa, multiplier))
    }

    override fun compareTo(other: MoneyPaisa): Int = this.paisa.compareTo(other.paisa)

    fun isZero(): Boolean = paisa == 0L

    fun isPositive(): Boolean = paisa > 0L

    fun toFormattedTaka(showDecimals: Boolean = true): String {
        val takaPart = paisa / 100
        val paisaPart = paisa % 100
        val formattedTaka = formatGrouping(takaPart)
        return if (showDecimals && paisaPart > 0) {
            "৳ $formattedTaka.${paisaPart.toString().padStart(2, '0')}"
        } else if (showDecimals) {
            "৳ $formattedTaka.00"
        } else {
            "৳ $formattedTaka"
        }
    }

    fun toBengaliFormattedTaka(showDecimals: Boolean = true): String {
        val enStr = toFormattedTaka(showDecimals)
        return convertEnglishDigitsToBengali(enStr)
    }

    companion object {
        val ZERO = MoneyPaisa(0L)

        fun fromTaka(taka: Long): MoneyPaisa {
            return MoneyPaisa(Math.multiplyExact(taka, 100L))
        }

        fun fromPaisa(paisa: Long): MoneyPaisa {
            return MoneyPaisa(paisa)
        }

        fun parse(input: String): MoneyPaisa {
            val cleaned = normalizeDigits(input.trim())
                .replace("৳", "")
                .replace(",", "")
                .replace(" ", "")

            require(cleaned.isNotEmpty()) { "Input amount cannot be empty" }

            val parts = cleaned.split('.')
            require(parts.size <= 2) { "Invalid number format with multiple decimal points: $input" }

            val takaPartStr = parts[0]
            val taka = if (takaPartStr.isEmpty()) 0L else takaPartStr.toLong()
            require(taka >= 0L) { "Amount cannot be negative" }

            val paisaPart = if (parts.size == 2) {
                val pStr = parts[1]
                when {
                    pStr.isEmpty() -> 0L
                    pStr.length == 1 -> (pStr + "0").toLong()
                    pStr.length == 2 -> pStr.toLong()
                    else -> pStr.substring(0, 2).toLong()
                }
            } else {
                0L
            }
            require(paisaPart in 0L..99L) { "Invalid paisa part: $paisaPart" }

            val totalPaisa = Math.addExact(Math.multiplyExact(taka, 100L), paisaPart)
            return MoneyPaisa(totalPaisa)
        }

        fun normalizeDigits(input: String): String {
            val bengaliDigits = "০১২৩৪৫৬৭৮৯"
            val sb = StringBuilder(input.length)
            for (ch in input) {
                val idx = bengaliDigits.indexOf(ch)
                if (idx != -1) {
                    sb.append(idx)
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun convertEnglishDigitsToBengali(input: String): String {
            val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            val sb = StringBuilder(input.length)
            for (ch in input) {
                if (ch in '0'..'9') {
                    sb.append(bengaliDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        private fun formatGrouping(number: Long): String {
            val s = number.toString()
            if (s.length <= 3) return s
            val lastThree = s.substring(s.length - 3)
            val rest = s.substring(0, s.length - 3)
            val sb = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                sb.append(rest[i])
                count++
                if (count == 2 && i != 0) {
                    sb.append(',')
                    count = 0
                }
            }
            return sb.reverse().toString() + "," + lastThree
        }
    }
}
