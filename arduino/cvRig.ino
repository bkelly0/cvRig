#include <Servo.h>

Servo hServo;
Servo vServo;
int horizontal;

int maxH = 120;
int minH = 60;

int maxV = 120;
int minV = 60;

byte positions[2];

void setup() {
  // put your setup code here, to run once:
  hServo.attach(7);
  vServo.attach(6);
  
  vServo.write(90);
  hServo.write(90);
  
  Serial.begin(9600);
  //Serial.print("setup");
}

void loop() {
  if (Serial.available() > 1) {
    Serial.readBytes(positions,2);
    hServo.write(positions[0]);
    if (positions[1] < minV) {
      vServo.write(minV);
    } else if (positions[1] > maxV) {
      vServo.write(maxV);
    } else {
      vServo.write(positions[1]);
    }
  } 
}


