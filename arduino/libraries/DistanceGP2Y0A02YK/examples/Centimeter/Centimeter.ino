#include <DistanceGP2Y0A02YK.h>

DistanceGP2Y0A02YK Dist;
int distance;

void setup()
{
  Serial.begin(9600);
  Dist.begin(A0);
}

void loop()
{
  distance = Dist.getDistanceCentimeter();
  Serial.print("\nDistance in centimers: ");
  Serial.print(distance);  
  delay(500); //make it readable
}
