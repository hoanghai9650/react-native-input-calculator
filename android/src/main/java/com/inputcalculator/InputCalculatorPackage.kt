package com.inputcalculator


import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.inputcalculator.calculator.RCTInputCalculatorManager


class InputCalculatorPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(InputCalculator(reactContext))
  }

  override fun createViewManagers(
    reactContext: ReactApplicationContext
  ) = listOf(RCTInputCalculatorManager())
}
