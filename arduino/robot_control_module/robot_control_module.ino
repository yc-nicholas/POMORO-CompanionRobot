#include <Event.h>
#include <Timer.h>
#include <NewPing.h>
#include <DistanceGP2Y0A02YK.h>
#include <Wire.h>

#include "robot_control_module_1.1.h"

/* create sonar objects */
NewPing sonar_middle(SONAR_MIDDLE_TRIGGER_PIN, SONAR_MIDDLE_ECHO_PIN, MAX_DISTANCE);
NewPing sonar_left(SONAR_LEFT_TRIGGER_PIN, SONAR_LEFT_ECHO_PIN, MAX_DISTANCE);
NewPing sonar_right(SONAR_RIGHT_TRIGGER_PIN, SONAR_RIGHT_ECHO_PIN, MAX_DISTANCE);
/* create IR rangefinder(long) object */
DistanceGP2Y0A02YK rangefinder;

/* variables for sonar reading */
int sonar_reading = 0;
/* variable for IR rangefinder reading */
int middleIRSensor_reading = 0;
/* variables for overall system control */
System_Mode system_mode = SLEEP;
/* variables for obstacle avoidance operation */
byte robot_reaction = B00000000;
/* variables for data comm */
char bluetooth_incoming_data, i2c_slave_data, usb_incoming_data; 


void setup() 
{ 
    // initialize the IR rangefinder
    rangefinder.begin(IRRANGE_PIN);
    
    // initialize serial for bluetooth connection 
    Serial1.begin(9600);

    // initialize i2c connection with actuator board
    Wire.begin(); 

    // initialize serial for debugging   
//    Serial.begin(9600);
//    while (!Serial) 
//    {
//      // wait for serial port to connect. Needed for Leonardo only
//    }
} 

void loop()
{
  // listen for processor instruction
  bluetoothListen();
  // sensor data acquisition
  // dispatch control instruction
  // debug
  //debugRead();
}


/* ------ PROCEDURE/ROUTINES ------------------------- */
/*************************************************/


byte detectObject()
{
  return 0;
}

void avoidObstacle()
{
  robot_reaction = detectObject();
 
  // decision making

}

/*************************************************/

/* ------ Data Communication ------------------------- */
/*************************************************/
void bluetoothListen()
{
  if(Serial1.available()>0)
  {
    bluetooth_incoming_data = Serial1.read();
    parseCommand(bluetooth_incoming_data);
  }
}

void bluetoothWrite(char dataOUT)
{
}

void i2cMasterReceive()
{

}

void i2cMasterSend(byte dataOUT1, byte dataOUT2)
{
  Wire.beginTransmission(8);  // transmit to device #8
  Wire.write(dataOUT1);       // sends first byte
  Wire.write(dataOUT2);       // sends second byte
  Wire.endTransmission();     // stop transmitting
}

void debugRead()
{
  if(Serial.available()>0)
  {
    usb_incoming_data = Serial.read();
    parseCommand(usb_incoming_data);
  }
}

void debugWrite()
{
}

void parseCommand(char data)
{
  switch(data)
    {
    case 's':
      //robot stop
      i2cMasterSend(0x00,0x00);
      i2cMasterSend(0x40,0x00);
      break;
    case 'f':
      //robot forward
      i2cMasterSend(0x01,255);
      break;
    case 'l':
      //robot point turn left
      i2cMasterSend(0x04,0x00);
      break;
    case 'b':
      //robot reverse
      i2cMasterSend(0x02,255);
      break;
    case 'r':
      //robot point turn right
      i2cMasterSend(0x08,0x00);
      break;
    case 'w':
      //robot swing left (forward)
      i2cMasterSend(0x05,0x00);
      break;
    case 'x':
      //robot swing right (forward)
      i2cMasterSend(0x09,0x00);
      break;
    case 'y':
      //robot swing left (reverse)
      i2cMasterSend(0x06,0x00);
      break;
    case 'z':
      //robot swing right (reverse)
      i2cMasterSend(0x0A,0x00);
      break;
    case 'S':
      //servoMove = SRV_MOVE_IDLE;
      i2cMasterSend(0x40,0x00);
      break;
    case 'L':
      //servoMove = SRV_MOVE_LEFT;
      break;
    case 'R':
      //servoMove = SRV_MOVE_RIGHT;
      break;
    case 'U':
      //servoMove = SRV_MOVE_UP;
      i2cMasterSend(0x41,0x00);
      break;
    case 'D':
      //servoMove = SRV_MOVE_DOWN;
      i2cMasterSend(0x42,0x00);
      break;
    //default:
      // inform user of non existing command
      //Serial1.println("Invalid"); 
    }
    // acknowledge
    //Serial1.print("ACK ");
    //Serial1.println(data); 
}
/*************************************************/


/* ------ SENSOR INPUT ACQUISITION -------------- */
/*************************************************/
void getIRSonarReadings()
{
  sonar_reading = sonar_middle.ping_cm();
  middleIRSensor_reading = rangefinder.getDistanceCentimeter();
}
/*************************************************/






