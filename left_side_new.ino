#include <Keyboard.h>
#include <Wire.h>

const int numRows = 5;
const int numCols = 7;

const int numColsAll = 15;

struct Key {
  char normal;
  uint8_t special;
};
struct MasterKey {
    char normal;
    uint8_t special;
    int pressedCode; //0 means not pressed before or after loop; 1 means not pressed before but pressed after; 2 means pressed before but not pressed after; 3 means pressed before and still pressed
};

// Define row and column pins
const int rowPins[numRows] = {4, 13, 12, 1, 0};
const int colPins[numCols] = {5, 6, 7, 8, 9, 10, 11};



// Key mapping
Key keyMap[numRows][numCols] = {
    {{'\0', 0},              {'\0', 0},            {'1', 0},             {'2', 0},  {'3', 0},  {'4', 0}, {'5', 0}},
    {{'\0', KEY_TAB},        {'\0', 0},            {'q', 0},             {'w', 0},  {'e', 0},  {'r', 0}, {'t', 0}},
    {{'\0', KEY_CAPS_LOCK},  {'\0', 0},            {'a', 0},             {'s', 0},  {'d', 0},  {'f', 0}, {'g', 0}},
    {{'\0', KEY_LEFT_SHIFT}, {'\\', 0},            {'z', 0},             {'x', 0},  {'c', 0}, { 'v', 0}, {'b', 0}},
    {{'\0', KEY_LEFT_CTRL},  {'\0', KEY_LEFT_GUI}, {'\0', KEY_LEFT_ALT}, {'\0', 0}, {'\0', 0}, {' ', 0}, {'\0', 0}}
};
MasterKey masterKeyMap[numRows][numColsAll] = {
    {{'\0', 0, 0},              {'\0', 0, 0},            {'1', 0, 0},             {'2', 0, 0},  {'3', 0, 0},  {'4', 0, 0},  {'5', 0, 0},  {'6', 0, 0}, {'7', 0, 0},  {'8', 0, 0},  {'9', 0, 0},              {'0', 0, 0},              {'-', 0, 0},  {'=', 0, 0},               {'\0', KEY_BACKSPACE, 0}},
    {{'\0', KEY_TAB, 0},        {'\0', 0, 0},            {'q', 0, 0},             {'w', 0, 0},  {'e', 0, 0},  {'r', 0, 0},  {'t', 0, 0},  {'y', 0, 0}, {'u', 0, 0},  {'i', 0, 0},  {'o', 0, 0},              {'p', 0, 0},              {'[', 0, 0},  {']', 0, 0},               {'\0', KEY_RETURN, 0}},
    {{'\0', KEY_CAPS_LOCK, 0},  {'\0', 0, 0},            {'a', 0, 0},             {'s', 0, 0},  {'d', 0, 0},  {'f', 0, 0},  {'g', 0, 0},  {'h', 0, 0}, {'j', 0, 0},  {'k', 0, 0},  {'l', 0, 0},              {';', 0, 0},              {'\'', 0, 0}, {'#', 0, 0},               {'\0', 0, 0}},
    {{'\0', KEY_LEFT_SHIFT, 0}, {char(0xEC), 0, 0},            {'z', 0, 0},             {'x', 0, 0},  {'c', 0, 0},  { 'v', 0, 0}, {'b', 0, 0},  {'b', 0, 0}, {'m', 0, 0},  {',', 0, 0},  {'.', 0, 0},              {'/', 0, 0},              {'v', 0, 0},  {'\0', KEY_RIGHT_SHIFT, 0}, {'\0', 0, 0}},
    {{'\0', KEY_LEFT_CTRL, 0},  {'\0', KEY_LEFT_GUI, 0}, {'\0', KEY_LEFT_ALT, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'p', 0, 0},  {'\0', 0, 0}, {' ', 0, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'\0', KEY_RIGHT_ALT, 0}, {'\0', KEY_RIGHT_GUI, 0}, {'\0', 0, 0}, {'\0', 0, 0}, {'\0', KEY_RIGHT_CTRL, 0}}
};



Key last_press = {'\0', 0};
bool isShiftPressed = false;
bool isCtrlPressed = false;
bool isAltPressed = false;
bool anyKeyPressed = false;

bool isSameKey(Key k1, Key k2) {
  return (k1.normal == k2.normal) && (k1.special == k2.special);
}

void setup() {
  Keyboard.begin();
  Wire.begin();

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
  readSlave();
  pressButtons();
  delay(50);
}

void readMatrix(){
  for (int row = 0; row < numRows; row++) {
    digitalWrite(rowPins[row], LOW);

    for (int col = 0; col < numCols; col++) {
      if (digitalRead(colPins[col]) == LOW) {  // Key is pressed

        if (masterKeyMap[row][col].pressedCode == 0) {
            masterKeyMap[row][col].pressedCode = 1;
        } else {
            masterKeyMap[row][col].pressedCode = 3;
        }
      } else {
        if (masterKeyMap[row][col].pressedCode == 3) {
            masterKeyMap[row][col].pressedCode = 2;
        } else {
            masterKeyMap[row][col].pressedCode = 0;
        }
      }
    }
    digitalWrite(rowPins[row], HIGH);
  }
}

void readSlave(){}

void pressButtons(){
  for (int row = 0; row < numRows; row++) {
    for (int col = 0; col < numColsAll; col++) {
      if (masterKeyMap[row][col].pressedCode == 1) {
        if (masterKeyMap[row][col].special != 0) {
          Keyboard.press(masterKeyMap[row][col].special);
        } else if (masterKeyMap[row][col].normal != '\0') {
          Keyboard.press(masterKeyMap[row][col].normal);
        }
        masterKeyMap[row][col].pressedCode = 3;
      } else if (masterKeyMap[row][col].pressedCode == 2){
        if (masterKeyMap[row][col].special != 0) {
          Keyboard.release(masterKeyMap[row][col].special);
        } else if (masterKeyMap[row][col].normal != '\0') {
          Keyboard.release(masterKeyMap[row][col].normal);
        }
        masterKeyMap[row][col].pressedCode = 0;
      }
    }
  }
}