#include <Keyboard.h>
#include <Wire.h>

const int numRows = 5;
const int numCols = 7;

const int numColsAll = 15;

const int motorQ = 21;
const int motorW = 22;
const int motorE = 23;
const int motorR = 24;
const int motorT = 25;
const int motorA = 26;
const int motorS = 27;
const int motorD = 28;
const int motorF = 29;
const int motorG = 30;
const int motorZ = 31;
const int motorX = 32;
const int motorC = 33;
const int motorV = 34;
const int motorB = 35;

const int numMotors = 15;

const int motors[numMotors] = {motorQ, motorW, motorE, motorR, motorT, motorA, motorS, motorD, motorF, motorG, motorZ, motorX, motorC, motorV, motorB};


struct Key {
    char normal;
    uint8_t special;
    int pressedCode; //0 means not pressed before or after loop; 1 means not pressed before but pressed after; 2 means pressed before but not pressed after; 3 means pressed before and still pressed
};

// Define row and column pins
const int rowPins[numRows] = {4, 13, 12, 1, 0};
const int colPins[numCols] = {5, 6, 7, 8, 9, 10, 11};



// Key mapping
Key keyMap[numRows][numColsAll] = {
    {{'\0', 0, 0},              {'\0', 0, 0},            {'1', 0, 0},             {'2', 0, 0},  {'3', 0, 0},  {'4', 0, 0},  {'5', 0, 0},  {'6', 0, 0}, {'7', 0, 0},  {'8', 0, 0},  {'9', 0, 0},              {'0', 0, 0},              {'-', 0, 0},  {'=', 0, 0},                {'\0', KEY_BACKSPACE, 0}},
    {{'\0', KEY_TAB, 0},        {'\0', 0, 0},            {'q', 0, 0},             {'w', 0, 0},  {'e', 0, 0},  {'r', 0, 0},  {'t', 0, 0},  {'y', 0, 0}, {'u', 0, 0},  {'i', 0, 0},  {'o', 0, 0},              {'p', 0, 0},              {'[', 0, 0},  {']', 0, 0},                {'\0', KEY_RETURN, 0}},
    {{'\0', KEY_CAPS_LOCK, 0},  {'\0', 0, 0},            {'a', 0, 0},             {'s', 0, 0},  {'d', 0, 0},  {'f', 0, 0},  {'g', 0, 0},  {'h', 0, 0}, {'j', 0, 0},  {'k', 0, 0},  {'l', 0, 0},              {';', 0, 0},              {'\'', 0, 0}, {'#', 0, 0},                {'\0', 0, 0}},
    {{'\0', KEY_LEFT_SHIFT, 0}, {char(0xEC), 0, 0},      {'z', 0, 0},             {'x', 0, 0},  {'c', 0, 0},  { 'v', 0, 0}, {'b', 0, 0},  {'n', 0, 0}, {'m', 0, 0},  {',', 0, 0},  {'.', 0, 0},              {'/', 0, 0},              {'v', 0, 0},  {'\0', KEY_RIGHT_SHIFT, 0}, {'\0', 0, 0}},
    {{'\0', KEY_LEFT_CTRL, 0},  {'\0', KEY_LEFT_GUI, 0}, {'\0', KEY_LEFT_ALT, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'p', 0, 0},  {'\0', 0, 0}, {' ', 0, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'\0', KEY_RIGHT_ALT, 0}, {'\0', KEY_RIGHT_GUI, 0}, {'\0', 0, 0}, {'\0', KEY_MENU, 0},        {'\0', KEY_RIGHT_CTRL, 0}}
};

void setup() {
  Keyboard.begin();
  Wire.begin(2);
  Wire.onReceive(readMaster);
  Serial.begin(9600);

  for (int motorCount = 0; motorCount < numMotors; motorCount++) {
    pinMode(motors[motorCount], OUTPUT);
  }

  // Set row pins as OUTPUT and initialize HIGH
  for (int row = 0; row < numRows; row++) {
    pinMode(rowPins[row], OUTPUT);
    digitalWrite(rowPins[row], HIGH);
  }

  // Set column pins as INPUT with internal pull-ups
  for (int col = 0; col < numCols; col++) {
    pinMode(colPins[col], INPUT_PULLUP);
  }
}

void loop() {
  readMatrix();
  pressButtons();
  delay(100);
}

void readMatrix(){
  for (int row = 0; row < numRows; row++) {
    digitalWrite(rowPins[row], LOW);

    for (int col = 0; col < numCols; col++) {
      if (digitalRead(colPins[col]) == LOW) {  // Key is pressed

        if (keyMap[row][col].pressedCode == 0) {
            keyMap[row][col].pressedCode = 1;
        } else {
            keyMap[row][col].pressedCode = 3;
        }
      } else {
        if (keyMap[row][col].pressedCode == 3) {
            keyMap[row][col].pressedCode = 2;
        } else {
            keyMap[row][col].pressedCode = 0;
        }
      }
    }
    digitalWrite(rowPins[row], HIGH);
  }
}

void readMaster(){
  int row = Wire.read();
  int col = Wire.read();
  col += 7;
  int pressedCodeNew = Wire.read();
  keyMap[row][col].pressedCode = pressedCodeNew;
}

void pressButtons(){
  for (int row = 0; row < numRows; row++) {
    for (int col = 0; col < numColsAll; col++) {
      if (keyMap[row][col].pressedCode == 1) {
        if (keyMap[row][col].special != 0) {
          Keyboard.press(keyMap[row][col].special);
        } else if (keyMap[row][col].normal != '\0') {
          Keyboard.press(keyMap[row][col].normal);
        }
        keyMap[row][col].pressedCode = 3;
      } else if (keyMap[row][col].pressedCode == 2){
        if (keyMap[row][col].special != 0) {
          Keyboard.release(keyMap[row][col].special);
        } else if (keyMap[row][col].normal != '\0') {
          Keyboard.release(keyMap[row][col].normal);
        }
        keyMap[row][col].pressedCode = 0;
      }
    }
  }
}

void activateMotors(){
  if (Serial.available() > 0) {
    for (int motorCount = 0; motorCount < numMotors; motorCount++) {
      digitalWrite(motors[motorCount], LOW);
    }
  }

  char command = Serial.read();

  if (command == 'q') {
    digitalWrite(motorQ, HIGH);
    delay(10);
  } else if (command == 'w') {
    digitalWrite(motorW, HIGH);
    delay(10);
  } else if (command == 'e') {
    digitalWrite(motorE, HIGH);
    delay(10);
  } else if (command == 'r') {
    digitalWrite(motorR, HIGH);
    delay(10);
  } else if (command == 't') {
    digitalWrite(motorT, HIGH);
    delay(10);
  } else if (command == 'a') {
    digitalWrite(motorA, HIGH);
    delay(10);
  } else if (command == 's') {
    digitalWrite(motorS, HIGH);
    delay(10);
  } else if (command == 'd') {
    digitalWrite(motorD, HIGH);
    delay(10);
  } else if (command == 'f') {
    digitalWrite(motorF, HIGH);
    delay(10);
  } else if (command == 'g') {
    digitalWrite(motorG, HIGH);
    delay(10);
  } else if (command == 'z') {
    digitalWrite(motorZ, HIGH);
    delay(10);
  } else if (command == 'x') {
    digitalWrite(motorX, HIGH);
    delay(10);
  } else if (command == 'c') {
    digitalWrite(motorC, HIGH);
    delay(10);
  } else if (command == 'v') {
    digitalWrite(motorV, HIGH);
    delay(10);
  } else if (command == 'b') {
    digitalWrite(motorB, HIGH);
    delay(10);
  } else {
    Wire.beginTransmission(1);
    Wire.write(command);
    Wire.endTransmission();
  }
  if (command == '0') {
    for (int motorCount = 0; motorCount < numMotors; motorCount++) {
      digitalWrite(motors[motorCount], LOW);
    }
  }
}