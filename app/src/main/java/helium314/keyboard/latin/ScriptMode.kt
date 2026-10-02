// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

/**
 * LiBoard: superscript and subscript typing (x² / x₂ in the suggestion strip) – for chemistry,
 * biology, geology, maths: H₂O, SO₄²⁻, Ca²⁺, ¹⁴C, 10⁻³, m².
 *
 * A keyboard cannot format text in other apps, so the Unicode super-/subscript characters are
 * written; they work everywhere. Digits, + − = ( ) exist in both; most letters exist raised,
 * only some lowered. Characters without a counterpart are typed normally.
 */
object ScriptMode {
    enum class Mode { NONE, SUPER, SUB }

    @JvmStatic var mode = Mode.NONE

    private fun table(from: String, to: String): Map<Int, String> {
        val source = from.codePoints().toArray()
        val target = to.codePoints().toArray()
        require(source.size == target.size)
        return source.indices.associate { source[it] to String(Character.toChars(target[it])) }
    }

    private val SUPER = table(
        "0123456789+-−=()" + "abcdefghijklmnoprstuvwxyz" + "ABDEGHIJKLMNOPRTUVW",
        "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁻⁼⁽⁾" + "ᵃᵇᶜᵈᵉᶠᵍʰⁱʲᵏˡᵐⁿᵒᵖʳˢᵗᵘᵛʷˣʸᶻ" + "ᴬᴮᴰᴱᴳᴴᴵᴶᴷᴸᴹᴺᴼᴾᴿᵀᵁⱽᵂ",
    )

    private val SUB = table(
        "0123456789+-−=()" + "aehijklmnoprstuvxə",
        "₀₁₂₃₄₅₆₇₈₉₊₋₋₌₍₎" + "ₐₑₕᵢⱼₖₗₘₙₒₚᵣₛₜᵤᵥₓₔ",
    )

    /** The raised or lowered form of [codePoint] in the current mode, or null to type it as is. */
    @JvmStatic
    fun map(codePoint: Int): String? = when (mode) {
        Mode.SUPER -> SUPER[codePoint]
        Mode.SUB -> SUB[codePoint]
        Mode.NONE -> null
    }

    /** Tapping x² or x₂: switches that mode on, or off if it is already on. */
    @JvmStatic
    fun toggle(target: Mode) {
        mode = if (mode == target) Mode.NONE else target
    }
}
