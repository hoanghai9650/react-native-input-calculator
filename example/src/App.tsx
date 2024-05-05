import * as React from 'react';

import {
  Keyboard,
  StyleSheet,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import InputCalculator from 'react-native-input-calculator';

export default function App() {
  const [value, setValue] = React.useState<string | undefined>('23');

  return (
    <View style={styles.container}>
      <TouchableWithoutFeedback
        style={{ backgroundColor: 'red', width: 200, height: 200 }}
        onPress={Keyboard.dismiss}
      >
        <InputCalculator
          placeholder="Enter a number"
          value={value}
          onChangeText={(text) => {
            setValue(text);
          }}
        />
      </TouchableWithoutFeedback>
      <TextInput
        value={value}
        placeholder="Enter a number"
        // onChangeText={(text) => setValue(text)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
