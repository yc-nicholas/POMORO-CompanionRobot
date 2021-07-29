/************************************************************************************************************
 * Based on DistanceGP2Y0A21YK.h - Nicholas                    *
 ************************************************************************************************************ ***********************************************************************************************************/

/// <summary>
/// DistanceGP2Y0A02YK.cpp - Library for retrieving data from the GP2Y0A02YK IR Distance sensor.
/// For more information: variable declaration, changelog,... see DistanceGP2Y0A02YK.h
/// </summary>

#include <Arduino.h>
#include <DistanceGP2Y0A02YK.h>

/// <summary>
/// Constructor
/// </summary>
DistanceGP2Y0A02YK::DistanceGP2Y0A02YK()
{
}

/// <summary>
/// Begin function to set pins: distancePin = A0.
/// </summary>
void
DistanceGP2Y0A02YK::begin()
{
	begin (A0);
}

/// <summary>
/// Begin variables
/// - int _distancePin: number indicating the distance to an object: ANALOG IN
/// - int _transferFunctionLUT3V: Transfer function Lookup Table (for 3.3V reference value)
/// - int _transferFunctionLUT5V: Transfer function Lookup Table (for 5V reference value)
/// When you use begin() without variables standard values are loaded: A0
/// </summary>
void
DistanceGP2Y0A02YK::begin(int distancePin)
{
	pinMode(distancePin, INPUT);
	_distancePin=distancePin;
	setAveraging(100);		      //1: all samples passed to higher level
	setARefVoltage(5);		      // 5 default situation
		//setARefVoltage(3);  // 3.3V: put a wire between the AREF pin and the 3.3V VCC pin.
		//This increases accuracy (and uses a different LUT)
}

/// <summary>
/// setAveraging(int avg): Sets how many samples have to be averaged in getDistanceCentimeter, default value is 100.
/// </summary>
void DistanceGP2Y0A02YK::setAveraging(int avg)
{
	_average=avg;
}

/// <summary>
/// getDistanceRaw(): Returns the distance as a raw value: ADC output: 0 -> 1023
/// </summary>
int DistanceGP2Y0A02YK::getDistanceRaw()
{
	return (analogRead(_distancePin));
}

/// <summary>
/// getDistanceVolt(): Returns the distance as a Voltage: ADC Input: 0V -> 5V (or 0V -> 3.3V)
/// </summary>
int DistanceGP2Y0A02YK::getDistanceVolt()
{
	return _mapGP2Y0A02YK_V(getDistanceRaw());
}

/// <summary>
/// getDistanceCentimeter(): Returns the distance in centimeters
/// </summary>
int DistanceGP2Y0A02YK::getDistanceCentimeter()
{
	return _mapGP2Y0A02YK_CM();
}

/// <summary>
/// _mapGP2Y0A02YKV: calculates the input voltage of the ADC
/// </summary>
int DistanceGP2Y0A02YK::_mapGP2Y0A02YK_V(int value)
{
	if (_refVoltage==3)
	{
		return map(value,0,1023,0,3300);
	}
	else
	{
		return map(value,0,1023,0,5000);
	}
}

/// <summary>
/// _mapGP2Y0A02YK_CM: calculates the distance in centimeters using math equation
/// </summary>
int DistanceGP2Y0A02YK::_mapGP2Y0A02YK_CM()
{
	int distanceRaw;
	for(int i = 0; i < _average; i ++)
		distanceRaw += getDistanceRaw();
	distanceRaw = distanceRaw/_average;
	return 10650.08 * pow(distanceRaw,-0.935) - 10;
}

/// <summary>
/// setARefVoltage:set the ADC reference voltage: (default value: 5V, set to 3 for 3.3V)
/// </summary>
void DistanceGP2Y0A02YK::setARefVoltage(int refV)
{
	_refVoltage=refV;
	if (_refVoltage==3)
	{
		analogReference(EXTERNAL);
	}
}

/// <summary>
/// isCloser: check whether the distance to the detected object is smaller than a given threshold
/// </summary>
boolean DistanceGP2Y0A02YK::isCloser(int threshold)
{
	if (threshold>getDistanceCentimeter())
	{
		return (true);
	}
	else
	{
		return (false);
	}
}

/// <summary>
/// isFarther: check whether the distance to the detected object is smaller than a given threshold
/// </summary>
boolean DistanceGP2Y0A02YK::isFarther(int threshold)
{
	if (threshold<getDistanceCentimeter())
	{
		return true;
	}
	else
	{
		return false;
	}
}
