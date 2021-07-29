
/* define sonar sensor reading threshold */
#define MIN_DISTANCE 20
#define MAX_DISTANCE 500
#define OFF_LIMIT 0
#define THRESHOLD 15

/* define system modes */
typedef enum System_Mode 
{ 
  SLEEP, 
  OBSTACLE, 
  REMOTE, 
  ANDROID 
};

/* define PING sensor pins */
const byte SONAR_MIDDLE_TRIGGER_PIN = 5;
const byte SONAR_MIDDLE_ECHO_PIN = 4;
const byte SONAR_RIGHT_TRIGGER_PIN = 8;
const byte SONAR_RIGHT_ECHO_PIN = 7;
const byte SONAR_LEFT_TRIGGER_PIN = 10;
const byte SONAR_LEFT_ECHO_PIN = 9;
/* define IR rangefinder(long) pin */
const byte IRRANGE_PIN = A5;
