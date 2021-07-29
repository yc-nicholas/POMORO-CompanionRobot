
#include <Event.h>
#include <Timer.h>
#include <Encoders.h>
#include <DRV8833.h>
#include <Servo.h> 
#include <Wire.h>
#include "robot_actuator_module.h"

/* create motor driver object */
DRV8833 motor_driver = DRV8833();
/* create servo objects */
Servo servo_middle, servo_leftarm, servo_rightarm;
/* create wheel encoder object */
Encoders encoders;

/* variables for servo movement state */
ServoMovement_State servo_middle_state, servo_leftarm_state, servo_rightarm_state = IDLE;
byte servo_middle_angle, servo_leftarm_angle, servo_rightarm_angle= 0;
int servo_sweep_event;
Timer servo_sweep_timer;
/* variables for servo control */
byte cur_angle, start_angle = 0;
/* variables for robot movement state */
byte motor_speed = MOTOR_SPEED_NORMAL;
RobotMovement_State robot_state = STOP;
/* variables for motor control (PID and Dead Reckoning) */
float delta_distanceCM = 0, travel_distanceCM = 0;
float delta_heading = 0, pos_X = 0, pos_Y = 0;
int wheel_L = 0, wheel_R = 0, wheel_total = 0;
float rad, target;
/* variables for data comm */
char usb_incoming_data[2]; 
char received_data[2];


void setup() 
{ 
    // initialize led pins as output

    // initialize buzzer pin as output

    // initialize motors pins as output
    motor_driver.attachMotorA(MOTOR_A1_PIN, MOTOR_A2_PIN);
    motor_driver.attachMotorB(MOTOR_B1_PIN, MOTOR_B2_PIN);
    
    // initialize the encoders
    encoders.setup();
    
    // initialize servos
    servo_middle.attach(SRV_MIDDLE_PIN);
    servo_leftarm.attach(SRV_L_ARM_PIN);
    servo_rightarm.attach(SRV_R_ARM_PIN);
    
    // initialize serial for debugging   
    Serial.begin(9600);

    // join i2c bus with control board, address #8
    Wire.begin(8);
    // register i2c slave receiver event
    Wire.onReceive(i2cSlaveReceive); 

    // initialize servo position
    servoTurnFast(SRV_MIDDLE_PIN, SRV_ANGLE_CENTER);
    servoTurnFast(SRV_L_ARM_PIN, SRV_ANGLE_CENTER);
    servoTurnFast(SRV_R_ARM_PIN, SRV_ANGLE_CENTER);
    servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);

    robotStop();
} 

void loop()
{  
  // close loop motor pid control 
 
  // servo control 
  //SoftwareServo::refresh();
  servo_sweep_timer.update();
  
  // uncomment this section for debugging
  //debugRead();
}

/* ------ Sub-routines ------------------------------- */
/*************************************************/
void checkDistance(float targetDistCM)
{
  travel_distanceCM = 0;
  while(travel_distanceCM - targetDistCM > 0)
  {
    delta_distanceCM = (encoders.leftDistance() + encoders.rightDistance())/ 2;
    travel_distanceCM += delta_distanceCM;
  }
}
/*************************************************/


/* ------ Data Communication ------------------------- */
/*************************************************/
void i2cSlaveReceive(int howMany)
{
  while (1 < Wire.available()) // loop through all but the last
  {
    //SoftwareServo::refresh();
    received_data[0] = Wire.read();                                           // received byte 1 as a character
    received_data[1] = Wire.read();                                           // received byte 1 as a character
    parseCommand(received_data);
    //Serial.print("I2C received Byte 1:  0x");
    //Serial.println(received_data[0], HEX);                                    // print the character (debug)
    //Serial.print("I2C received Byte 2:  0x");
    //Serial.println(received_data[1], HEX);                                    // print the character (debug)

  }
  
}

void i2cSlaveSend(char dataOUT)
{
}

void debugRead()
{
    while (1 < Serial.available()) // loop through all but the last
  {
    usb_incoming_data[0] = Serial.read();                                           // received byte 1 as a character
    usb_incoming_data[1] = Serial.read();                                           // received byte 1 as a character
    Serial.print("Serial received Byte 1:  0x");
    Serial.println(received_data[0], HEX);                                    // print the character (debug)
    Serial.print("Serial received Byte 2:  0x");
    Serial.println(received_data[1], HEX);                                    // print the character (debug)
    if (Serial.read() != '\n') {
      Serial.println("[Warning] Incoming data doesn't contains NEWLINE");
    }
    parseCommand(usb_incoming_data);
  }
}

void debugWrite()
{
}

void parseCommand(char data[2])
{
  switch(data[0])
  {
    case 0x00:
      robotStop();
      break;
    case 0x01:
      robotRun(data[1], FORWARD);
      break;
    case 0x04:
      robotPointTurn(LEFT);
      break;
    case 0x02:
      robotRun(data[1], REVERSE);
      break;
    case 0x08:
      robotPointTurn(RIGHT);
      break;
    case 0x05:
      robotSwingTurn(FW_LEFT);
      break;
    case 0x09:
      robotSwingTurn(FW_RIGHT);
      break;
    case 0x06:
      robotSwingTurn(RV_LEFT);
      break;
    case 0x0A:
      robotSwingTurn(RV_RIGHT);
      break;
    case 0x14:
      robotPointTurn(data[1], LEFT);
      break;
    case 0x18:
      robotPointTurn(data[1], RIGHT);
      break;
    case 0x40:
      servo_middle_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      break;
    case 0x41:
      servo_middle_state = UP;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x42:
      servo_middle_state = DOWN;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x44:
      servo_middle_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnFast(SRV_MIDDLE_PIN, data[1]);
      break;
    case 0x45:
      servo_middle_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnSlow(SRV_MIDDLE_PIN, data[1]);
      break;
    case 0x48:
      servo_leftarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      break;
    case 0x49:
      servo_leftarm_state = UP;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x4A:
      servo_leftarm_state = DOWN;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x4C:
      servo_leftarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnFast(SRV_L_ARM_PIN, data[1]);
      break;
    case 0x4D:
      servo_leftarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnSlow(SRV_L_ARM_PIN, data[1]);
      break;
    case 0x60:
      servo_rightarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      break;
    case 0x61:
      servo_rightarm_state = UP;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x62:
      servo_rightarm_state = DOWN;
      //servo_sweep_timer.stop(servo_sweep_event);
      //servo_sweep_event = servo_sweep_timer.every(SRV_SPEED_SWEEP,servoSweep);
      break;
    case 0x64:
      servo_rightarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnFast(SRV_R_ARM_PIN, data[1]);
      break;
    case 0x65:
      servo_rightarm_state = IDLE;
      //servo_sweep_timer.stop(servo_sweep_event);
      servoTurnSlow(SRV_R_ARM_PIN, data[1]);
      break;
    default:
      // inform user of non existing command
      Serial.println("invalid");
  }
}
/*************************************************/



/* ------ Servo Control -------------------- */
/*************************************************/
void servoSweep()
{
  if(servo_middle_state == UP)
  {
    cur_angle = servo_middle.read();
    if(cur_angle < SRV_ANGLE_MAX)
    {
      cur_angle ++;
      servo_middle.write(cur_angle);
    }
  }
  if(servo_middle_state == DOWN)
  {
    cur_angle = servo_middle.read();
    if(cur_angle > SRV_ANGLE_LOWER_LIMIT)
    {
      cur_angle --;
      servo_middle.write(cur_angle);
    }
  }
  if(servo_leftarm_state == UP)
  {
    cur_angle = servo_leftarm.read();
    if(cur_angle < SRV_ANGLE_MAX)
    {
      cur_angle ++;
      servo_leftarm.write(cur_angle);
    }
  }
  if(servo_leftarm_state == DOWN)
  {
    cur_angle = servo_leftarm.read();
    if(cur_angle > SRV_ANGLE_LOWER_LIMIT)
    {
      cur_angle --;
      servo_leftarm.write(cur_angle);
    }
  }
  if(servo_rightarm_state == UP)
  {
    cur_angle = servo_rightarm.read();
    if(cur_angle < SRV_ANGLE_MAX)
    {
      cur_angle ++;
      servo_rightarm.write(cur_angle);
    }
  }
  if(servo_rightarm_state == DOWN)
  {
    cur_angle = servo_rightarm.read();
    if(cur_angle > SRV_ANGLE_LOWER_LIMIT)
    {
      cur_angle --;
      servo_rightarm.write(cur_angle);
    }
  }
}

byte servoTurnFast(byte servo, byte target_angle)  // 180 - max left, 0 - max right, 90 center
{
  if (servo == SRV_MIDDLE_PIN)
  {
    servo_middle.write(target_angle);
  }

  if (servo == SRV_L_ARM_PIN)
  {
    servo_leftarm.write(target_angle);
  }

  if (servo == SRV_R_ARM_PIN)
  {
    servo_rightarm.write(target_angle);
  }
  
  return target_angle;
}

byte servoTurnSlow(byte servo, byte target_angle)  // 180 - max left, 0 - max right, 90 center
{
  if(servo == SRV_MIDDLE_PIN)
  {
    start_angle = servo_middle.read();
  }
  if (servo == SRV_L_ARM_PIN)
  {
    start_angle = servo_leftarm.read();
  }
  if (servo == SRV_R_ARM_PIN)
  {
    start_angle = servo_rightarm.read();
  }
  if (target_angle > start_angle)
  {
    while(start_angle < target_angle)
    {
        start_angle ++;
        if(servo == SRV_MIDDLE_PIN)
        {
          servo_middle.write(start_angle);
        }
        if (servo == SRV_L_ARM_PIN)
        {
          servo_leftarm.write(start_angle);
        }
        if (servo == SRV_R_ARM_PIN)
        {
          servo_rightarm.write(start_angle);
        }
        //SoftwareServo::refresh();
        delay(SRV_SPEED_TURN);
    }
  }
  else
  {
     while(start_angle > target_angle)
    {
        start_angle --;
        if(servo == SRV_MIDDLE_PIN)
        {
          servo_middle.write(start_angle);
        }
        if (servo == SRV_L_ARM_PIN)
        {
          servo_leftarm.write(start_angle);
        }
        if (servo == SRV_R_ARM_PIN)
        {
          servo_rightarm.write(start_angle);
        }
        //SoftwareServo::refresh();
        delay(SRV_SPEED_TURN);
    }
  }
  return target_angle;
}

/*************************************************/

/* ------ Motor Control -------------------- */
/*************************************************/
void robotRun(int value, RobotMovement_State direction)
{
    switch(direction)
    {
    case FORWARD:  motor_driver.motorAForward(value);
                motor_driver.motorBForward(value);
                robot_state = direction;    
                break;
    case REVERSE: motor_driver.motorAReverse(value);   
                motor_driver.motorBReverse(value);   
                robot_state = direction;    
                break;
    default: return;
    }
}

void robotStop()
{      
    motor_driver.motorAStop();
    motor_driver.motorBStop();
    robot_state = STOP;  
}

void robotPointTurn(RobotMovement_State heading)
{
  switch(heading)
  {
    case LEFT:  motor_driver.motorAReverse(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBForward(MOTOR_SPEED_NORMAL);
                robot_state = heading;    
                break;
    case RIGHT: motor_driver.motorAForward(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBReverse(MOTOR_SPEED_NORMAL);   
                robot_state = heading;    
                break;
    default: return;
  }
}

void robotPointTurn(int deg, RobotMovement_State heading)
{  
  if(deg > 58)
    rad = (deg * 71) / 4068;
  else
    rad = deg * 0.0174532923;
    
  target = rad * 2 / RAD_PER_COUNT;
  
  robotStop();
  
  switch(heading)
  {
    case LEFT:  motor_driver.motorAReverse(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBForward(MOTOR_SPEED_NORMAL);
                robot_state = heading;     
                break;
    case RIGHT: motor_driver.motorAForward(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBReverse(MOTOR_SPEED_NORMAL);
                robot_state = heading;     
                break;
    default: return;
  }
  
  encoders.leftCounts(); encoders.rightCounts();
  
  while(target - wheel_total > 0)
  {
    wheel_L += encoders.leftCounts();
    wheel_R += encoders.rightCounts();
    wheel_total = wheel_L + wheel_R;
  }
  
  encoders.leftCounts(); encoders.rightCounts();
  robotStop();
}

void robotSwingTurn(RobotMovement_State heading)
{
  Serial.println("Swing");
  switch(heading)
  {
    case FW_LEFT:    
                motor_driver.motorAReverse(0);   
                motor_driver.motorBForward(MOTOR_SPEED_NORMAL);
                robot_state = heading;      
                break;
    case FW_RIGHT:  
                motor_driver.motorAForward(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBReverse(0);
                robot_state = heading;      
                break;
    case RV_LEFT:
                motor_driver.motorAForward(0);   
                motor_driver.motorBReverse(MOTOR_SPEED_NORMAL);
                robot_state = heading;      
                break;    
    case RV_RIGHT:  
                motor_driver.motorAReverse(MOTOR_SPEED_NORMAL);   
                motor_driver.motorBForward(0);
                robot_state = heading;      
                break;    
    default: return;
  }
}
