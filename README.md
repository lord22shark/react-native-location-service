
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
			bla: null,
			running: false,
			service: false
		};

	}

	componentDidMount () {

		//this.fromJava();

		this.subscription = DeviceEventEmitter.addListener('onLocationChanged', this.onLocationChanged.bind(this));

		this.serviceBinder = DeviceEventEmitter.addListener('onBindService', this.onBindService.bind(this));

		console.log('[JS] componentDidMount');

		/*RNLocationService.isPermited((result) => {
		
			console.log('1111', result);
		
			if (result === false) {
			
				RNLocationService.requestPermission();
			
			}
		
		});*/

		RNLocationService.isServiceRunning((result) => {

			if (result === true) {

				this.setState({
					service12: true
				});

			}

		});

		const data = "a=b&c=d&e=f&0=1";

		fetch('http://ilhost/public/echo.php', {
			headers: new Headers({
				'Content-Type': 'application/x-www-form-urlencoded',
				'Content-Length': data.length.toString(),
			}),
			body: data,
			method: 'POST',
			mode: 'cors',
			cache: 'no-cache'
		}).then(response => {

			return response.text();

		}).then(text => {

			console.log(`[JS] ${text}`);

		}).catch(error => {

			console.log(`[JS] ${error}`);

		});



	}

	componentWillUnmount () {

		this.subscription.remove();
		
		this.serviceBinder.remove();

	}

	onLocationChanged (data) {

		this.setState({
			bla: data
		});

	}

	onBindService (isRunning) {

		console.log(`[JS] Service - ${isRunning}`);

	}

	async fromJava () {

		/*await LocationServiceModule.sendLocationData(error => {

		console.error(error);

		}, msg => {

		this.setState({
		bla: msg
		});

		});*/

	}

	toJava () {

		//LocationServiceModule.receiveFromJs("O Sonho é Popular!");

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

			RNLocationService.startListener(2222, 10, 'http://192.168.0.250/public/echo.php', 'q1w2e3r4t5y6u7i8', '4', 'auth0ID', (result, error) => {

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

			RNLocationService.startService(2121, 5, 'http://ilhost/public/echo.php', 'q1w2e3r4t5y6u7i8', '4', 'auth0ID' (result, error) => {

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

	render () {

		return (
			<View style={styles.container}>
				<Text style={styles.instructions}>{instructions}</Text>
				<Text style={styles.instructions}>bla: {this.state.bla || null}</Text>
				<Button title="RODA (a)" onPress={this.a.bind(this)} />
				<Button title="RODA (b)" onPress={this.b.bind(this)} />
				<Button title="RODA (c)" onPress={this.c.bind(this)} />
				
				<Button title="吗" color="#D90000" onPress={this.isRunning.bind(this)} />
				
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
},
});

```
