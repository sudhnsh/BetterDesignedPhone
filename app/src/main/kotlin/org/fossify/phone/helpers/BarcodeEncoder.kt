package org.fossify.phone.helpers

object BarcodeEncoder {
    private val PATTERNS = arrayOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312",
        "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222",
        "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131",
        "311222", "321122", "321221", "312212", "322112", "322211", "212123", "212321",
        "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121",
        "313121", "211331", "231131", "213113", "213311", "213131", "311123", "311321",
        "331121", "312113", "312311", "332111", "314111", "221411", "431111", "111224",
        "111422", "121124", "121421", "141122", "141221", "112214", "112412", "122114",
        "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112",
        "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113",
        "114311", "411113", "411311", "113141", "114131", "311141", "411131", "211412",
        "211214", "211232", "2331112"
    )

    /**
     * Converts a string into a list of boolean modules (true = bar, false = space) using Code 128-B.
     */
    fun encodeCode128(text: String): List<Boolean> {
        if (text.isEmpty()) return emptyList()

        val startB = 104
        val stop = 106
        var checksum = startB
        val codes = mutableListOf(startB)

        text.forEachIndexed { i, char ->
            val code = char.code - 32
            if (code in 0..95) {
                codes.add(code)
                checksum += code * (i + 1)
            }
        }
        codes.add(checksum % 103)
        codes.add(stop)

        val sb = StringBuilder()
        for (code in codes) {
            if (code in PATTERNS.indices) {
                sb.append(PATTERNS[code])
            }
        }

        val modules = mutableListOf<Boolean>()
        var isBar = true
        for (ch in sb.toString()) {
            val count = ch.digitToIntOrNull() ?: 1
            repeat(count) { modules.add(isBar) }
            isBar = !isBar
        }
        return modules
    }
}
