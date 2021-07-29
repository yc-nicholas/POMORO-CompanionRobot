/************************************************************************************************************
 * modified based on DistanceGP2Y0A21YK.h - Nicholas                            *
 ***********************************************************************************************************/

#ifndef DistanceGP2Y0A02YK_h
#define DistanceGP2Y0A02YK_h
#include <Arduino.h>

class DistanceGP2Y0A02YK
{
	public:
		DistanceGP2Y0A02YK();
		void begin();
		void begin(int distancePin);

		int getDistanceRaw();
		int getDistanceVolt();
		int getDistanceCentimeter();

		boolean isCloser(int threshold);
		boolean isFarther(int threshold);

		void setAveraging(int avg);    
		void setARefVoltage(int _refV);

	private:
		int _mapGP2Y0A02YK_V(int value);
		int _mapGP2Y0A02YK_CM();
		int _distancePin;
		int _average;
		int _refVoltage;
};
#endif
