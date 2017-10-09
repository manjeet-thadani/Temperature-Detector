# Android Temperature Sensor module using LM35


## Objective - 

	1. Sense the temperature of object.
	2. Make wireless communication between circuit and our Smartphone or PC.

### Prerequisites

	1. Arduino board (I used Uno)
	2. Temperature Sensor - LM35
	3. Bluetooth Module - HC-05
	4. Resistance’s, Capacitor, Jumper cables etc.

### Circuit Arrangement

#### Connecting sensor with Arduino:-
![alt text](https://github.com/manjeet-thadani/Temperature-Detector/blob/master/Extras/Images/lm35.png)

```
	1.	Connect VCC of LM – 35 to 5v of Arduino.
	2.	Connect Out to A1 of Arduino.
	3.	Connect Gnd to ground of Arduino.

```

#### Connecting Bluetooth Module (HC – 05) to Arduino:-
![alt text](https://github.com/manjeet-thadani/Temperature-Detector/blob/master/Extras/Images/arduino_connection.png)

```
	Arduino 	HC-05
	  5V      -      VCC
	  Gnd     -      Gnd
	  Rx      -      Tx
	  Tx      -      Rx

```

#### Overall Circuit Arrangement:-
![alt text](https://github.com/manjeet-thadani/Temperature-Detector/blob/master/Extras/Images/circuit.png)


## Arduino Code:-

Remove Rx and Tx pins from Arduino before coding after coding reconnect them.

```
int val;
int tempPin = 1;

void setup()
{
	Serial.begin(9600);
}
void loop()
{
	val = analogRead(tempPin);
	float mv = ( val/1024.0)*5000; 
	float cel = mv/10;
	float farh = (cel*9)/5 + 32;

	Serial.print(cel);
	Serial.print("°c");
	Serial.println();

	/* uncomment this to get temperature in Fahrenheit 
	Serial.print(farh);
	Serial.print("°f");
	Serial.println();
	*/
	delay(1000);
}


```

## Deployment

![alt text](https://github.com/manjeet-thadani/Temperature-Detector/blob/master/Extras/Images/app01.png)		
![alt text](https://github.com/manjeet-thadani/Temperature-Detector/blob/master/Extras/Images/app02.png)

