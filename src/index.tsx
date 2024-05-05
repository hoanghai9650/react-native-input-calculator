import React, { useEffect, useRef, useState } from 'react';
import {
  AppState,
  Platform,
  requireNativeComponent,
  TextInput,
} from 'react-native';
import type { TextInputProps } from 'react-native';

const { State: TextInputState } = TextInput;

interface InputCalculatorProps extends TextInputProps {
  text?: string;
  forwardedRef?: React.Ref<any>;
}

export const Keys = {
  CALCULATOR_TEXT_INPUT_FOCUSED: 'CALCULATOR_TEXT_INPUT_FOCUSED',
  CALCULATOR_TEXT_INPUT_UNFOCUSED: 'CALCULATOR_TEXT_INPUT_UNFOCUSED',
  CALCULATOR_DISMISS: 'CALCULATOR_DISMISS',
  CALCULATOR_CALCULATE: 'CALCULATOR_CALCULATE',
  CALCULATOR_HEIGHT_UPDATED: 'CALCULATOR_HEIGHT_UPDATED',
  CALULATOR_PUSH_KEY: 'CALULATOR_PUSH_KEY',
};

const InputCalculator: React.FC<InputCalculatorProps> = (props) => {
  const [isFocus, setIsFocus] = useState(false);
  const [lastNativeText, setLastNativeText] = useState(props.value);
  const inputRef = useRef<TextInput>(null);

  useEffect(() => {
    const nativeProps = {};
    if (lastNativeText !== props.value && typeof props.value === 'string') {
      // @ts-ignore
      nativeProps.text = props.value;
    }
    if (
      Object.keys(nativeProps).length > 0 &&
      inputRef.current &&
      inputRef.current.setNativeProps
    ) {
      inputRef.current.setNativeProps(nativeProps);
    }

    if (props.selectionState && props.selection && props.selection.end) {
      props.selectionState.update(props.selection.start, props.selection.end);
    }
  }, [props.value, props.selection, props.selectionState, lastNativeText]);

  useEffect(() => {
    AppState.addEventListener('change', handleAppStateChange);
    return () => {
      AppState.addEventListener('change', handleAppStateChange);
    };
  }, []);

  const handleAppStateChange = (nextAppState: string) => {
    if (nextAppState === 'background' && Platform.OS === 'android') {
      blur();
    }
  };

  const blur = () => {
    if (inputRef.current && typeof inputRef.current.blur === 'function') {
      inputRef.current.blur();
    }
  };

  const focus = () => {
    if (inputRef.current && typeof inputRef.current.focus === 'function') {
      inputRef.current.focus();
    }
  };

  const getText = () => {
    if (typeof props.value === 'string') {
      console.log('props.value', props.value);
      return props.value;
    }
    if (typeof props.defaultValue === 'string') {
      return props.defaultValue;
    }

    return '';
  };

  const onFocus = (event: any) => {
    setIsFocus(true);
    if (props.onFocus) {
      props.onFocus(event);
    }
    if (inputRef.current != null && TextInputState) {
      TextInputState.focusTextInput(inputRef.current);
    }
  };

  const onBlur = (event: any) => {
    setIsFocus(false);
    if (props.onBlur) {
      props.onBlur(event);
    }
  };

  const onChange = (event: any) => {
    if (inputRef.current && inputRef.current.setNativeProps) {
      inputRef.current.setNativeProps({
        mostRecentEventCount: event.nativeEvent.eventCount,
      });
    }

    const { text } = event.nativeEvent;
    if (props.onChange) {
      props.onChange(event);
    }
    if (props.onChangeText) {
      props.onChangeText(text);
    }
    if (!inputRef) {
      return;
    }
    setLastNativeText(text);
  };

  return (
    <RCTInputCalculator
      {...props}
      ref={inputRef}
      onFocus={onFocus}
      onBlur={onBlur}
      onChange={onChange}
      text={getText()}
    />
  );
};

// @ts-ignore
const RCTInputCalculator =
  requireNativeComponent<InputCalculatorProps>('RCTInputCalculator');

export default InputCalculator;
