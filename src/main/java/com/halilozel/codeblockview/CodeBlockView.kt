package com.halilozel.codeblockview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

/** Reusable read-only code card. Copy always uses the original source, without UI labels. */
class CodeBlockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialCardView(context, attrs) {
    private val label = TextView(context)
    private val copy = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
    private val code = TextView(context)
    private val numbers = TextView(context)
    private var source = ""
    private val resetCopy = Runnable { copy.setText(R.string.cbv_code_copy) }

    init {
        radius = dp(16).toFloat()
        strokeWidth = dp(1)
        strokeColor = color(R.color.cbv_md_outline_variant)
        setCardBackgroundColor(color(R.color.cbv_code_block_background))
        val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        addView(column, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        val header = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(4), dp(8), dp(4))
            setBackgroundColor(color(R.color.cbv_md_surface_container_high))
        }
        label.apply {
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(color(R.color.cbv_md_on_surface_variant))
        }
        header.addView(label, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        copy.apply {
            setText(R.string.cbv_code_copy)
            minHeight = dp(48)
            minimumWidth = dp(48)
            textSize = 12f
            setIconResource(R.drawable.cbv_ic_copy)
            iconSize = dp(16)
            setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.cbv_code_clip_label), source))
                setText(R.string.cbv_code_copied)
                removeCallbacks(resetCopy)
                postDelayed(resetCopy, 2000)
            }
        }
        header.addView(copy)
        column.addView(header, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            setPadding(dp(12), dp(16), dp(16), dp(16))
        }
        listOf(numbers, code).forEach {
            it.typeface = Typeface.MONOSPACE
            it.textSize = 14f
            it.setLineSpacing(dp(4).toFloat(), 1f)
            it.includeFontPadding = false
            it.textDirection = View.TEXT_DIRECTION_LTR
            it.setHorizontallyScrolling(true)
        }
        numbers.apply {
            gravity = Gravity.END
            setTextColor(color(R.color.cbv_code_comment))
            setPadding(0, 0, dp(16), 0)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        code.apply {
            setTextColor(color(R.color.cbv_code_block_text))
            setTextIsSelectable(true)
        }
        row.addView(numbers)
        row.addView(code)
        column.addView(HorizontalScrollView(context).apply {
            isFillViewport = true
            isHorizontalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
            addView(row, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    @JvmOverloads
    fun setCode(source: String, language: String = "kotlin") {
        this.source = source
        copy.removeCallbacks(resetCopy)
        copy.setText(R.string.cbv_code_copy)
        copy.isEnabled = source.isNotEmpty()
        val languageName = when (language.lowercase(Locale.ROOT)) {
            "kotlin", "kt", "kts" -> "Kotlin"
            "" -> context.getString(R.string.cbv_code_plain)
            else -> language
        }
        label.text = languageName
        val count = source.count { it == '\n' } + 1
        numbers.text = (1..count).joinToString("\n")
        val styled = SpannableString(source)
        if (language.lowercase(Locale.ROOT) in setOf("kotlin", "kt", "kts")) {
            KotlinSyntax.tokens(source).forEach { token ->
                val tint = when (token.kind) {
                    KotlinSyntax.Kind.COMMENT -> R.color.cbv_code_comment
                    KotlinSyntax.Kind.STRING -> R.color.cbv_code_string
                    KotlinSyntax.Kind.KEYWORD -> R.color.cbv_code_keyword
                    KotlinSyntax.Kind.NUMBER -> R.color.cbv_code_number
                    KotlinSyntax.Kind.ANNOTATION -> R.color.cbv_code_annotation
                }
                styled.setSpan(ForegroundColorSpan(color(tint)), token.start, token.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        code.text = styled
    }

    override fun onDetachedFromWindow() {
        copy.removeCallbacks(resetCopy)
        copy.setText(R.string.cbv_code_copy)
        super.onDetachedFromWindow()
    }

    private fun color(id: Int) = ContextCompat.getColor(context, id)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
