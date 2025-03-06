#include <Keyboard.h> 

const int motorS = 8; //wildcat - motor names changed to match character they represent 
const int motorD = 13;  
const int motorF = 2;  
const int motorG = 7;  
const int motorH = 12; 
const int motorJ = 1;  
const int motorK = 6;    

const int rowPins[] = {9, 10, 11};        
const int colPins[] = {3, 4, 5};       
const int numRows = 3;   
const int numCols = 3;     
char keys[3][3] = { {'a', 's', 'd'}, {'f', 'g', 'h'}, {'j', 'k', 'l'} };   
int keycodes[3][3] = { {97, 115, 100}, {102, 103, 104}, {106, 107, 108} };   
//wildcat - same as keys but with ascii codes for the lower case letters instead.  
//needs to be kept at parity with keys.  

  

void setup() {  
  pinMode(motorS, OUTPUT);  
  pinMode(motorD, OUTPUT);  
  pinMode(motorF, OUTPUT);  
  pinMode(motorG, OUTPUT);  
  pinMode(motorH, OUTPUT);  
  pinMode(motorJ, OUTPUT);  
  pinMode(motorK, OUTPUT);  

  Serial.begin(9600);  
  Keyboard.begin();   

  for (int i = 0; i < numRows; i++) {   
    pinMode(rowPins[i], OUTPUT);   
    digitalWrite(rowPins[i], HIGH);    
  }   

  for (int i = 0; i < numCols; i++) {   
    pinMode(colPins[i], INPUT_PULLUP);   
  }   
}  



void loop() {  
  if (Serial.available() > 0) {  
    digitalWrite(motorS, LOW);  
    digitalWrite(motorD, LOW);  
    digitalWrite(motorF, LOW);  
    digitalWrite(motorG, LOW);  
    digitalWrite(motorH, LOW);  
    digitalWrite(motorJ, LOW);  
    digitalWrite(motorK, LOW);  

    char command = Serial.read();  
    
    if (command == 's') {  
      digitalWrite(motorS, HIGH);  
      Serial.println("motor S is HIGH");  
      delay(10);  
    }   
    if (command == 'd') {  
      digitalWrite(motorD, HIGH);  
      Serial.println("motor D is HIGH");  
      delay(10);  
    }   
    if (command == 'f') {  
      digitalWrite(motorF, HIGH);  
      Serial.println("motor F is HIGH");  
      delay(10);  
    }   
    if (command == 'g') {  
      digitalWrite(motorG, HIGH);  
      Serial.println("motor G is HIGH");  
      delay(10);  
    }   
    if (command == 'h') {  
      digitalWrite(motorH, HIGH);  
      Serial.println("motor H is HIGH");  
      delay(10);  
    }   
    if (command == 'j') {  
      digitalWrite(motorJ, HIGH);  
      Serial.println("motor J is HIGH");  
      delay(10);  
    }   
    if (command == 'k') {  
      digitalWrite(motorK, HIGH);  
      Serial.println("motor K is HIGH");  
      delay(10);  
    }   
    if (command == '0') { 
      digitalWrite(motorS, LOW); 
      digitalWrite(motorD, LOW); 
      digitalWrite(motorF, LOW); 
      digitalWrite(motorG, LOW); 
      digitalWrite(motorH, LOW); 
      digitalWrite(motorJ, LOW); 
      digitalWrite(motorK, LOW); 
      //wildcat - needs each motor to be set to low. maybe easier to update if motors in array? 
      delay(10); 
    } 
  }  

  delay(10);  // small delay for stability  

  for (int row = 0; row < numRows; row++) {   
    digitalWrite(rowPins[row], LOW);   

    for (int col = 0; col < numCols; col++) {   
      if (digitalRead(colPins[col]) == LOW) {  // Key is PRESSED, LOW = PRESSED   
        //Serial.print(keys[row][col]);   
        //Serial.print("\n");   
        Keyboard.press(keycodes[row][col]); 
        Keyboard.release(keycodes[row][col]); 
        //wildcat - probably doesn't need changing but related to other Keyboard stuff 

        while (digitalRead(colPins[col]) == LOW) {   
          delay(0);   
        }   

        delay(5);        
      }   
    }   

    digitalWrite(rowPins[row], HIGH);   

  }   
}  