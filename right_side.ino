#include <Keyboard.h>
#include <Wire.h>

const int numRows = 5;
const int numCols = 8;

struct Key {
  char normal;
  uint8_t special;
};

// Define row and column pins
const int rowPins[numRows] = {4, 3, 2, 1, 0};  
const int colPins[numCols] = {5, 6, 7, 8, 9, 10, 11, 12};

// Key mapping
Key keyMap[numRows][numCols] = {
    {{'6', 0}, {'7', 0}, {'8', 0}, {'9', 0}, {'0', 0}, {'-', 0}, {'=', 0}, {'\0', KEY_BACKSPACE}},
    {{'y', 0}, {'u', 0}, {'i', 0}, {'o', 0}, {'p', 0}, {'[', 0}, {']', 0}, {'\0', KEY_RETURN}},
    {{'h', 0}, {'j', 0}, {'k', 0}, {'l', 0}, {';', 0}, {'\'', 0}, {'#', 0}, {'\0', 0}},
    {{'b', 0}, {'m', 0}, {',', 0}, {'.', 0}, {'/', 0}, {'v', 0}, {'\0', KEY_RIGHT_SHIFT}, {'\0', 0}},
    {{' ', 0}, {'\0', 0}, {'\0', 0}, {'\0', KEY_RIGHT_ALT}, {'\0', KEY_RIGHT_GUI}, {'\0', 0}, {'\0', PLACEHOLDER}, {'\0', KEY_RIGHT_CTRL}}
};

Key last_press = {'\0', 0};  
bool isShiftPressed = false;
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
  anyKeyPressed = false;  // Reset key tracking

  for (int row = 0; row < numRows; row++) {
    digitalWrite(rowPins[row], LOW);

    for (int col = 0; col < numCols; col++) {
      if (digitalRead(colPins[col]) == LOW) {  // Key is pressed
        anyKeyPressed = true;  // At least one key is pressed

        if (!isSameKey(last_press, keyMap[row][col])) { // Avoid repeat presses
          if (keyMap[row][col].special == KEY_RIGHT_SHIFT) {
            isShiftPressed = true;
            Keyboard.press(KEY_RIGHT_SHIFT);  // Physically press Shift
          } else if (keyMap[row][col].normal != '\0') {
            if (isShiftPressed) {
              Keyboard.write(toupper(keyMap[row][col].normal)); // Send uppercase version
            } else {
              Keyboard.write(keyMap[row][col].normal);
            }
          } else if (keyMap[row][col].special != 0) {
            Keyboard.press(keyMap[row][col].special);
            delay(100);
            Keyboard.release(keyMap[row][col].special);
          }

          last_press = keyMap[row][col];  
          delay(100);  
        }
      }
    }

    digitalWrite(rowPins[row], HIGH);
  }

  if (!anyKeyPressed) {
    if (isShiftPressed) {
      Keyboard.release(KEY_RIGHT_SHIFT);
      isShiftPressed = false;
    }
    Keyboard.releaseAll();
    last_press = {'\0', 0}; 
  }
}


