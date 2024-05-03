package com.inputcalculator.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ButtonViewModel : ViewModel() {
  private var _buttonWidth: MutableLiveData<Float> = MutableLiveData(0f)

  val buttonWidth: LiveData<Float>
    get() = _buttonWidth

  fun setButtonWidth(width: Float) {
    _buttonWidth.postValue(width)
  }

  override fun onCleared() {
    super.onCleared()
    _buttonWidth.postValue(0f)
  }
}
