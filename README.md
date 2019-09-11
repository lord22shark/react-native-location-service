
# react-native-location-service

## Getting started

`$ npm install react-native-location-service --save`

### Mostly automatic installation

`$ react-native link react-native-location-service`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import me.buskar.RNLocationServicePackage;` to the imports at the top of the file
  - Add `new RNLocationServicePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-location-service'
  	project(':react-native-location-service').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-location-service/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-location-service')
  	```


## Usage
```javascript
import RNLocationService from 'react-native-location-service';

// TODO: What to do with the module?
RNLocationService;
```

## Example
```javascript
/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Alert, Platform, DeviceEventEmitter, ActivityIndicator, Button, StyleSheet, Text, View} from 'react-native';
import RNLocationService from 'react-native-location-service';

const instructions = Platform.select({
	ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
	android: 'Double tap R on your keyboard to reload,\n' + 'Shake or press menu button for dev menu',
});

type Props = {};

export default class App extends Component<Props> {

	constructor (props) {

		super(props);

		this.state = {
			coords: null,
			running: false,
			service: false
		};

	}

	componentDidMount () {

		this.subscription = DeviceEventEmitter.addListener('onLocationChanged', this.onLocationChanged.bind(this));

		this.serviceBinder = DeviceEventEmitter.addListener('onBindService', this.onBindService.bind(this));

		console.log('[JS] componentDidMount');

	}

	componentWillUnmount () {

		this.subscription.remove();
		
		this.serviceBinder.remove();

	}

	onLocationChanged (data) {

		this.setState({
			coords: data
		});

	}

	onBindService (isRunning) {

		console.log(`[JS] Service - ${isRunning}`);

	}

	a () {

		RNLocationService.addPreference("key", "value", (result, error) => {

			console.log("RNLocationService.addPreference", result, error);

		});

	}

	b () {

		RNLocationService.getPreference((result, error) => {

			console.log("RNLocationService.getPreference", result, error);

		});

	}

	c () {

		RNLocationService.removePreference("key", (result, error) => {

			console.log("RNLocationService.removePreference", result, error);

		});

	}

	toggleListener () {

		if (this.state.running === false) {

			RNLocationService.startListener(2222, 10, 'http://192.168.0.1/echo.php', 'q1w2e3r4t5y6u7i8', '4', 't408jg85jg980j', (result, error) => {

				console.log('startListener', result, error);

				if (error === null) {

					this.setState({
						running: true
					});

				}

			});

		} else {

			RNLocationService.stopListener((result, error) => {

				console.log('stopListener', result, error);

				if (error === null) {

					this.setState({
						running: false
					});

				}

			});

		}

	}

	toggleService () {

		if (this.state.service === false) {

			RNLocationService.startService(2121, 5, 'http://192.168.0.1/echo.php', 'q1w2e3r4t5y6u7i8', '4', 't408jg85jg980j', (result, error) => {

				console.log('[JS] Callback Start Service', result, error);

				if (error === null) {

					this.setState({
						service: true
					});

				}

			});

		} else {

			RNLocationService.stopService((result, error) => {

				console.log('[JS] Callback Stop Service', result, error);

				if (error === null) {

					this.setState({
						service: false
					});

				}

			});

		}

	}

	isRunning () {

		RNLocationService.isServiceRunning((result) => {

			Alert.alert('RN', `RESULT: ${result}`);

		});

	}

	isListenerRunning () {

		RNLocationService.isListenerRunning((result) => {

			Alert.alert('RN', `RESULT: ${result}`);

		});

	}

	isPermited () {

		RNLocationService.isPermited((result) => {
		
			Alert.alert('PERMISSION', `R: ${result}`);
		
		});

	}

	requestPermission () {

		RNLocationService.requestPermission();

	}

	render () {

		return (
			<View style={styles.container}>
				<Text style={styles.instructions}>{instructions}</Text>
				<Text style={styles.instructions}>COORDS: {this.state.coords || null}</Text>
				<Button title="RODA (a)" onPress={this.a.bind(this)} />
				<Button title="RODA (b)" onPress={this.b.bind(this)} />
				<Button title="RODA (c)" onPress={this.c.bind(this)} />
				
				<Button title="我可以吗" color="#D9A900" onPress={this.isPermited.bind(this)} />
				<Button title="吗 Service Running" color="#D90000" onPress={this.isRunning.bind(this)} />
				<Button title="吗 Listener Running" color="#D90000" onPress={this.isListenerRunning.bind(this)} />
				<Button title="你让我用马" color="#D9A909" onPress={this.requestPermission.bind(this)} />
				
				<View style={{justifyContent: 'center', flexDirection: 'row', alignSelf: 'stretch'}}>
					<Button title={(this.state.running === false) ? "START LISTENER" : "STOP LISTENER"} onPress={this.toggleListener.bind(this)} />
					{(this.state.running === true) ? (<ActivityIndicator color='#0F1DD9' />) : null }
				</View>
				<View style={{justifyContent: 'center', flexDirection: 'row', alignSelf: 'stretch'}}>
					<Button title={(this.state.service === false) ? "START SERVICE" : "STOP SERVICE"} onPress={this.toggleService.bind(this)} />
					{(this.state.service === true) ? (<ActivityIndicator color='#AF10B9' />) : null }
				</View>
			</View>
		);

	}

}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		backgroundColor: '#F5FCFF',
	},
	welcome: {
		fontSize: 20,
		textAlign: 'center',
		margin: 10,
	},
	instructions: {
		textAlign: 'center',
		color: '#333333',
		marginBottom: 5,
	}
});

```
