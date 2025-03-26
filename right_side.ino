#include <Wire.h>
#include <Keyboard.h>

const int numRows = 5;
const int numCols = 8;

const int motorY = 21;
const int motorU = 22;
const int motorI = 23;
const int motorO = 24;
const int motorP = 25;
const int motorH = 26;
const int motorJ = 27;
const int motorK = 28;
const int motorL = 29;
const int motorN = 30;
const int motorM = 31;

const int numMotors = 11;

const int motors[numMotors] = {motorY, motorU, motorI, motorO, motorP, motorH, motorJ, motorK, motorL, motorN, motorM};

struct Key {
    char normal;
    uint8_t special;
    int pressedCode; //0 means not pressed before or after loop; 1 means not pressed before but pressed after; 2 means pressed before but not pressed after; 3 means pressed before and still pressed
};

// Define row and column pins
const int rowPins[numRows] = {4, 3, 2, 1, 0};
const int colPins[numCols] = {5, 6, 7, 8, 9, 10, 11, 12};

// Key mapping
Key keyMap[numRows][numCols] = {
    {{'6', 0, 0}, {'7', 0, 0},  {'8', 0, 0},  {'9', 0, 0},              {'0', 0, 0},              {'-', 0, 0},  {'=', 0, 0},                {'\0', KEY_BACKSPACE, 0}},
    {{'y', 0, 0}, {'u', 0, 0},  {'i', 0, 0},  {'o', 0, 0},              {'p', 0, 0},              {'[', 0, 0},  {']', 0, 0},                {'\0', KEY_RETURN, 0}},
    {{'h', 0, 0}, {'j', 0, 0},  {'k', 0, 0},  {'l', 0, 0},              {';', 0, 0},              {'\'', 0, 0}, {'#', 0, 0},                {'\0', 0, 0}},
    {{'n', 0, 0}, {'m', 0, 0},  {',', 0, 0},  {'.', 0, 0},              {'/', 0, 0},              {'v', 0, 0},  {'\0', KEY_RIGHT_SHIFT, 0}, {'\0', 0, 0}},
    {{' ', 0, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'\0', KEY_RIGHT_ALT, 0}, {'\0', KEY_RIGHT_GUI, 0}, {'\0', 0, 0}, {'\0', KEY_MENU, 0},        {'\0', KEY_RIGHT_CTRL, 0}}
    };

void setup() {
  Keyboard.begin();
  Wire.begin(1);
  Wire.onReceive(activateMotors);


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
  sendToSlave();
  delay(50);
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

void sendToSlave(){
  String masterCode = "";
  for (int row = 0; row < numRows; row++) {
    for (int col = 0; col < numCols; col++) {
      if (keyMap[row][col].pressedCode == 1) {
        Wire.beginTransmission(2);
        Wire.write(row);
        Wire.write(col);
        Wire.write(keyMap[row][col].pressedCode);
        Wire.endTransmission();
        keyMap[row][col].pressedCode = 3;
        delay(5);
      } else if (keyMap[row][col].pressedCode == 2){
        Wire.beginTransmission(2);
        Wire.write(row);
        Wire.write(col);
        Wire.write(keyMap[row][col].pressedCode);
        Wire.endTransmission();
        keyMap[row][col].pressedCode = 0;
        delay(5);
      }
    }
  }

}

void pressButtons(){
  for (int row = 0; row < numRows; row++) {
    for (int col = 0; col < numCols; col++) {
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
  for (int motorCount = 0; motorCount < numMotors; motorCount++) {
    digitalWrite(motors[motorCount], LOW);
  }

  char command = Wire.read();

  if (command == 'y') {
    digitalWrite(motorY, HIGH);
    delay(10);
  } else if (command == 'u') {
    digitalWrite(motorU, HIGH);
    delay(10);
  } else if (command == 'i') {
    digitalWrite(motorI, HIGH);
    delay(10);
  } else if (command == 'o') {
    digitalWrite(motorO, HIGH);
    delay(10);
  } else if (command == 'p') {
    digitalWrite(motorP, HIGH);
    delay(10);
  } else if (command == 'h') {
    digitalWrite(motorH, HIGH);
    delay(10);
  } else if (command == 'j') {
    digitalWrite(motorJ, HIGH);
    delay(10);
  } else if (command == 'k') {
    digitalWrite(motorK, HIGH);
    delay(10);
  } else if (command == 'l') {
    digitalWrite(motorL, HIGH);
    delay(10);
  } else if (command == 'n') {
    digitalWrite(motorN, HIGH);
    delay(10);
  } else if (command == 'm') {
    digitalWrite(motorM, HIGH);
    delay(10);
  } else if (command == '0') {
    for (int motorCount = 0; motorCount < numMotors; motorCount++) {
      digitalWrite(motors[motorCount], LOW);
    }
    delay(10);
  }
}
