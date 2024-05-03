package com.inputcalculator.calculator


import android.R.attr.inputType
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.views.textinput.ReactEditText
import com.facebook.react.views.textinput.ReactTextInputManager


class RCTInputCalculatorManager : ReactTextInputManager() {
  private var layout: ConstraintLayout? = null
  private var screenHeight = 0
  private var calculatorHeight = 0
  private var keyboardPixel = 0
  private var statusBarHeight = 0

  override fun getName() = REACT_CLASS

  companion object {
    const val REACT_CLASS = "RCTInputCalculator"
  }

  @ReactProp(name = "value")
  fun setValue(view: ReactEditText, value: String) {
    val cursorPosition = view.selectionStart.coerceAtLeast(0)
    view.text = Editable.Factory.getInstance().newEditable(value)
    view.setSelection(cursorPosition.coerceAtMost(value.length))
  }


  override fun createViewInstance(context: ThemedReactContext): ReactEditText {
    val editText = CalculatorEditText(context)
    editText.returnKeyType = "done"
    editText.setShowSoftInputOnFocus(false)
    editText.inputType = inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE.inv()
    getCustomKeyboard(context, editText)
    editText.background = null


    editText.setOnClickListener { v: View ->
      UiThreadUtil.runOnUiThread {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
          v.windowToken,
          0
        )
        editText.setShowSoftInputOnFocus(false)
        if (!editText.isFocused) {
          editText.requestFocusFromJS()
        }
      }
    }


    val x = object : CalculatorEditText.OnFocusChangeListener {
      override fun onFocusChange(view: CalculatorEditText, hasFocus: Boolean) {
        UiThreadUtil.runOnUiThread {
          if (hasFocus) {
            addContentView(context)
          } else {
            removeContentView(context)
          }
          view.setOnKeyListener { view, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && hasFocus) {
              view.isFocusable = false
              view.clearFocus()
              return@setOnKeyListener true
            }
            false
          }
        }
      }
    }

    editText.onFocusListener = x

    return editText
  }

  private fun addContentView(context: ThemedReactContext) {
    if (layout!!.parent == null) {
      context.currentActivity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
      layout!!.y = calculatorHeight.toFloat()

      context.currentActivity!!.addContentView(
        layout,
        ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
      )
      layout!!.animate().translationY(0f).setDuration(250)
    }
  }

  private fun removeContentView(context: ThemedReactContext) {
    if (layout!!.parent != null) {
      layout!!.y = 0f
      layout!!.animate().translationY(calculatorHeight.toFloat()).setDuration(250).withEndAction {
        context.currentActivity!!.window
          .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (layout!!.parent != null) {
          (layout!!.parent as ViewGroup).removeView(layout)
        }
      }
    }
  }


  @SuppressLint("DiscouragedApi", "InternalInsetResource")
  private fun getCustomKeyboard(
    context: ThemedReactContext,
    editText: CalculatorEditText
  ): ConstraintLayout {
    layout = ConstraintLayout(context)


    val rootView = CustomKeyboardView(context)
    rootView.setEditText(editText)
    rootView.setBackgroundColor(Color.parseColor("#f9f9f9"))
    rootView.setPadding(7, 7, 7, 0)
    rootView.setContext(context)

    val displayMetrics = context.currentActivity!!.resources.displayMetrics
    val resourceId = context.currentActivity!!
      .resources.getIdentifier("status_bar_height", "dimen", "android")
    screenHeight = displayMetrics.heightPixels
    calculatorHeight = (displayMetrics.widthPixels * 0.675).toInt()
    keyboardPixel = screenHeight - calculatorHeight
    statusBarHeight = context.currentActivity!!.resources.getDimensionPixelSize(resourceId)

    val lParams = ConstraintLayout.LayoutParams(
      ConstraintLayout.LayoutParams.MATCH_PARENT,
      ConstraintLayout.LayoutParams.WRAP_CONTENT
    ).apply {
      height = calculatorHeight
      bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
      setMargins(0, keyboardPixel, 0, 0)
    }
    if (context.currentActivity != null) {
      (layout as ConstraintLayout).addView(rootView, lParams)
    }

    return layout as ConstraintLayout
  }

  override fun updateExtraData(p0: ReactEditText, p1: Any?) {}

}

