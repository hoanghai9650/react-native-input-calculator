package com.inputcalculator.calculator

// CustomKeyboardView.kt
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.views.textinput.ReactEditText
import com.inputcalculator.R
import org.mariuszgromada.math.mxparser.Expression

interface CustomKeyboardDelegate {
  fun keyDidPress(key: String)
  fun clearText()
  fun onBackSpace()
  fun calculateResult()
}

@SuppressLint("SetTextI18n", "ViewConstructor")
class CustomKeyboardView : ConstraintLayout, CustomKeyboardDelegate {
  private var mContext: ThemedReactContext? = null

  constructor(context: ThemedReactContext) : super(context) {
    init(context)
    mContext = context
  }

  constructor(context: ThemedReactContext, attrs: AttributeSet?) : super(context, attrs) {
    init(context)
    mContext = context
  }

  constructor(context: ThemedReactContext, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    init(context)
    mContext = context
  }

  private var delegate: CustomKeyboardDelegate? = null
  private var editText: CalculatorEditText? = null
  private lateinit var viewModel: ButtonViewModel

  private val keys = listOf(
    listOf("AC", "÷", "×", "back"),
    listOf("7", "8", "9", "-"),
    listOf("4", "5", "6", "+"),
    listOf("1", "2", "3", "="),
    listOf("000", "", "0")
  )

  @ReactProp(name = "value")
  fun setValue(view: ReactEditText, value: String) {
    val cursorPosition = view.selectionStart.coerceAtLeast(0)
    view.text = Editable.Factory.getInstance().newEditable(value)
    view.setSelection(cursorPosition.coerceAtMost(value.length))
  }


  private fun init(context: ThemedReactContext) {
    val activity = context.currentActivity as? AppCompatActivity
    if (activity != null) {
      viewModel = ViewModelProvider(activity)[ButtonViewModel::class.java]
      viewModel.buttonWidth.observe(activity) { buttonWidth ->
        renderUI(buttonWidth)
      }
    } else {
      throw RuntimeException("Invalid context. Expected AppCompatActivity.")
    }

    this.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
      val totalWidth = width  // This is the total width of the rootView
      val leftPadding = paddingLeft  // This is the left padding of the rootView
      val rightPadding = paddingRight  // This is the right padding of the rootView

      val widthAfterPadding: Float = (totalWidth - leftPadding - rightPadding).toFloat()  //
      val columns: Float = 4f
      val separatorWidth = 5f

      val buttonWidth: Float =
        ((if (widthAfterPadding > 0) widthAfterPadding else 375 - (columns - 1) * separatorWidth) - 15f) / columns

      if (buttonWidth > 0f && buttonWidth != viewModel.buttonWidth.value) {
        viewModel.setButtonWidth(buttonWidth)
      }
    }
  }


  @SuppressLint("DrawAllocation")
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
  }

  private fun renderUI(buttonWidth: Float) {
    val separatorWidth = 5f

    val buttonHeight = buttonWidth / 2
    var yOffset = 0f
    for ((_, row) in keys.withIndex()) {
      var xOffset = 0f
      for ((_, key) in row.withIndex()) {
        val button = if (key == "back") {
          createImageButton(key, xOffset, yOffset, buttonWidth, buttonHeight)
        } else {
          createButton(key, xOffset, yOffset, separatorWidth, buttonWidth, buttonHeight)
        }

        addView(button)

        xOffset += buttonWidth + separatorWidth
      }
      yOffset += buttonHeight + separatorWidth
    }
  }

  private fun createButton(
    key: String,
    xOffset: Float,
    yOffset: Float,
    separatorWidth: Float,
    buttonWidth: Float,
    buttonHeight: Float
  ): Button {
    val specialKeys = listOf("=", "-", "×", "÷", "AC", "back", "+")
    return Button(context).apply {
      val typeface = ResourcesCompat.getFont(context, R.font.protext)
      val shapeInit = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(Color.WHITE)
        setBackgroundColor(Color.WHITE)
      }
      gravity = Gravity.CENTER
      background = shapeInit
      text = key
      setTypeface(typeface)
      textSize = 24.toFloat()
      setTextColor(Color.BLACK)
      stateListAnimator = null
      layoutParams = LayoutParams(
        buttonWidth.toInt(),
        buttonHeight.toInt()
      ).apply {
        constrainedWidth = false
      }
      when (key) {
        "" -> {
          layoutParams = LayoutParams(
            0,
            0
          ).apply {
            constrainedWidth = false
          }
        }

        "=" -> {
          layoutParams = LayoutParams(
            buttonWidth.toInt(),
            buttonHeight.toInt() * 2 + separatorWidth.toInt() + 1
          ).apply {
            constrainedWidth = false
          }
        }

        "000" -> {
          layoutParams = LayoutParams(
            buttonWidth.toInt() * 2 + separatorWidth.toInt(),
            buttonHeight.toInt()
          ).apply {
            constrainedWidth = false
          }
        }
      }

      if (specialKeys.contains(key)) {
        background = GradientDrawable().apply {
          shape = GradientDrawable.RECTANGLE
          cornerRadius = 20f
          setColor(Color.parseColor("#d9d9d9"))
        }
      }
      translationX = xOffset.toInt().toFloat()
      translationY = yOffset.toInt().toFloat()
      setOnClickListener { onKeyPress(key) }
    }
  }

  private fun createImageButton(
    key: String,
    xOffset: Float,
    yOffset: Float,
    buttonWidth: Float,
    buttonHeight: Float
  ): ImageButton {
    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false)
    val bitmapDrawable = BitmapDrawable(resources, resizedBitmap)
    return ImageButton(context).apply {
      val shapeInit = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(Color.parseColor("#d9d9d9"))
      }
      background = shapeInit
      setImageDrawable(bitmapDrawable)
      stateListAnimator = null
      layoutParams = LayoutParams(
        buttonWidth.toInt(),
        buttonHeight.toInt()
      ).apply {
        constrainedWidth = false
      }
      translationX = xOffset.toInt().toFloat()
      translationY = yOffset.toInt().toFloat()
      setOnClickListener { onKeyPress(key) }
    }
  }

  fun setEditText(editText: CalculatorEditText) {
    this.editText = editText
  }

  fun setContext(context: ThemedReactContext) {
    mContext = context
  }

  private fun onKeyPress(key: String) {
    when (key) {
      "AC" -> {
        clearText()
      }

      "back" -> {
        onBackSpace()
      }

      "=" -> {
        calculateResult()
      }

      "×", "+", "-", "÷" -> keyDidPress(" $key ")
      else -> {
//        var text = editText?.text?.toString() ?: ""
//        text += key
//        val dispatcher = mContext?.getNativeModule(UIManagerModule::class.java)?.eventDispatcher
//        dispatcher?.dispatchEvent(ReactTextChangedEvent(-1, editText?.id ?: 0, text, 1))
        editText?.text?.insert(editText!!.selectionStart, key)
      }
    }
  }

  override fun keyDidPress(key: String) {
    println("Key pressed: $key")

    editText?.text?.replace(editText!!.selectionStart, editText!!.selectionEnd, key)
  }

  override fun clearText() {
    editText?.text?.clear()
  }

  override fun onBackSpace() {
    val start = editText?.selectionStart
    val end = editText?.selectionEnd
    if (start != null) {
      if (start > 0) {
        val newText = end?.let { editText?.text?.replaceRange(start - 1, it, "") }
        editText?.setText(newText)
        editText?.setSelection(start - 1)
      }
    }
  }

  override fun calculateResult() {
    val text = editText?.text.toString().replace("×", "*").replace("÷", "/")
    val pattern = "^\\s*(-?\\d+(\\.\\d+)?\\s*[-+*/]\\s*)*-?\\d+(\\.\\d+)?\\s*$"
    val regex = Regex(pattern)
    if (regex.matches(text)) {
      try {
        val result = eval(text).toString()
        editText?.setTextKeepState(result)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    } else {
      println("Invalid expression")
    }
  }

  private fun eval(str: String): Long? {
    val e = Expression(str)
    println("Expression: $e")
    return if (e.checkSyntax()) {
      e.calculate().toLong()
    } else {
      null
    }
  }

}
