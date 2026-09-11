package io.github.sixzleo.tabfold.device

object Compatibility {
    fun samsungTablet(manufacturer: String, model: String, smallestDp: Int): Boolean {
        if (!manufacturer.equals("samsung", true)) return false
        val code = model.uppercase()
        if (code.startsWith("SM-F")) return false // Fold/Flip phone hinges are not keyboard hinges.
        return code.startsWith("SM-X") || code.startsWith("SM-T") || code.startsWith("GT-P") || smallestDp >= 600
    }
    fun canRun(sdk: Int, samsungTablet: Boolean, hasMotion: Boolean): Boolean = sdk >= 33 && samsungTablet && hasMotion
    fun keyboardAccepted(name: String, virtual: Boolean, alphabetic: Boolean, allowOther: Boolean): Boolean =
        !virtual && alphabetic && (allowOther || name.contains("Book Cover", true) ||
            name.contains("EF-DX", true) || name.contains("EF-DT", true) || name.contains("Samsung", true))
}
