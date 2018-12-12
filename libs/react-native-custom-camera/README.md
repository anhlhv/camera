
# react-native-custom-camera

## Getting started

`$ npm install react-native-custom-camera --save`

### Mostly automatic installation

`$ react-native link react-native-custom-camera`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-custom-camera` and add `RNCustomCamera.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNCustomCamera.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNCustomCameraPackage;` to the imports at the top of the file
  - Add `new RNCustomCameraPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-custom-camera'
  	project(':react-native-custom-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-custom-camera/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-custom-camera')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNCustomCamera.sln` in `node_modules/react-native-custom-camera/windows/RNCustomCamera.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Custom.Camera.RNCustomCamera;` to the usings at the top of the file
  - Add `new RNCustomCameraPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNCustomCamera from 'react-native-custom-camera';

// TODO: What to do with the module?
RNCustomCamera;
```
  