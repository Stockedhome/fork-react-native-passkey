import { requireNativeModule } from 'expo-modules-core';

const LINKING_ERROR = "The package 'react-native-passkey' doesn't seem to be linked. Check the react-native-passkey package!";

const nativeModule = requireNativeModule('PasskeyModule');

console.log(nativeModule);

export const NativePasskey = nativeModule
  ? nativeModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
