
/* define motor parameters */
#define MOTOR_SPEED_MIN 130
#define MOTOR_SPEED_MAX 255
#define MOTOR_SPEED_NORMAL 180
/* define servo parameters */
#define SRV_ANGLE_MAX 180
#define SRV_ANGLE_CENTER 90
#define SRV_ANGLE_MIN 0
#define SRV_ANGLE_LOWER_LIMIT 65
#define SRV_SPEED_SWEEP 10
#define SRV_SPEED_TURN 5
/* define robot physical geometry */
#define WHEEL_DIAMETER 6.35
#define PPR 20
#define TRACK_WIDTH 10.5
#define CENTRE_TO_WHEEL_LENGTH 30

/* define robot movement states */
typedef enum RobotMovement_State 
{ 
  STOP, 
  FORWARD, 
  REVERSE, 
  LEFT, 
  RIGHT, 
  FW_LEFT, 
  FW_RIGHT, 
  RV_LEFT, 
  RV_RIGHT 
};
  
/* define servo arm movement states */
typedef enum ServoMovement_State 
{ 
  IDLE, 
  UP, 
  DOWN 
};

/* define motor pins */
const byte MOTOR_A1_PIN = 11; 
const byte MOTOR_A2_PIN = 6;
const byte MOTOR_B1_PIN = 5; 
const byte MOTOR_B2_PIN = 3;
/* define servo pins */
const byte SRV_MIDDLE_PIN = 10; // servo that controls phone holder
const byte SRV_L_ARM_PIN = 12; // servo that controls left arm
const byte SRV_R_ARM_PIN = 13;  // servo that controls right arm
/* define encoder constants*/
const float DISTANCE_PER_COUNT = PI * WHEEL_DIAMETER / PPR;
const float COUNTS_PER_ROTATION = PI * CENTRE_TO_WHEEL_LENGTH / DISTANCE_PER_COUNT;
const float RAD_PER_COUNT = 2 * PI/COUNTS_PER_ROTATION;
