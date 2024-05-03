package com.inputcalculator.calculator

import android.content.Context
import android.graphics.Rect
import com.facebook.react.uimanager.events.Event
import com.facebook.react.views.textinput.ReactEditText

class CalculatorEditText(context: Context) : ReactEditText(context) {
  var onFocusListener: OnFocusChangeListener? = null

  override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
    // Always return true if we are already focused. This is used by android in certain places,
    // such as text selection.
    if (isFocused) {
      return true
    }
    isFocusable = true
    isFocusableInTouchMode = true
    return super.requestFocus(direction, previouslyFocusedRect)
  }

  override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect)
    onFocusListener?.onFocusChange(this, isFocused)
  }


  interface OnFocusChangeListener {
    fun onFocusChange(view: CalculatorEditText, hasFocus: Boolean)
  }
}
