#include <Wire.h>
#include "paj7620.h"

#define GES_REACTION_TIME		600
#define GES_QUIT_TIME			1000

void setup()
{
  Serial.begin(115200);
  if (paj7620Init()) while (true);
}

void loop()
{
  uint8_t data = 0, data1 = 0;

  if (!paj7620ReadReg(0x43, 1, &data))
  {
    switch (data)
    {
      case GES_RIGHT_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        
        if (data == GES_LEFT_FLAG)Serial.write(6); // "Right-Left"
        
        else if (data == GES_FORWARD_FLAG)
        {
          Serial.write(8); //"Forward"
          delay(GES_QUIT_TIME);
        }
        else if (data == GES_BACKWARD_FLAG)
        {
          Serial.write(7); //"Backward"
          delay(GES_QUIT_TIME);
        }
        else Serial.write(4); //"Right"

        break;
      case GES_LEFT_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        if (data == GES_RIGHT_FLAG)Serial.write(5); //"Left-Right"
        
        else if (data == GES_FORWARD_FLAG)
        {
          Serial.write(8); //"Forward"
          delay(GES_QUIT_TIME);
        }
        else if (data == GES_BACKWARD_FLAG)
        {
          Serial.write(7); //"Backward"
          delay(GES_QUIT_TIME);
        }
        else Serial.write(3); //"Left"
        
        break;
      case GES_UP_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        
        if (data == GES_DOWN_FLAG)Serial.write(9); //"Up-Down"
        
        else if (data == GES_FORWARD_FLAG)
        {
          Serial.write(8); //"Forward"
          delay(GES_QUIT_TIME);
        }
        else if (data == GES_BACKWARD_FLAG)
        {
          Serial.write(7); //"Backward"
          delay(GES_QUIT_TIME);
        }
        else Serial.write(1); //"Up"
        
        break;
      case GES_DOWN_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        if (data == GES_UP_FLAG) Serial.write(10); //"Down-Up"

        else if (data == GES_FORWARD_FLAG)
        {
          Serial.write(8); //"Forward"
          delay(GES_QUIT_TIME);
        }
        else if (data == GES_BACKWARD_FLAG)
        {
          Serial.write(7); //"Backward"
          delay(GES_QUIT_TIME);
        }
        else Serial.write(2); //"Down"
        
        break;
      case GES_FORWARD_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        if (data == GES_BACKWARD_FLAG)
        {
          Serial.write(11); //"Forward-Backward"
          delay(GES_QUIT_TIME);
        }
        else
        {
          Serial.write(8); //"Forward"
          delay(GES_QUIT_TIME);
        }
        break;
      case GES_BACKWARD_FLAG:
        delay(GES_REACTION_TIME);
        paj7620ReadReg(0x43, 1, &data);
        if (data == GES_FORWARD_FLAG)
        {
          Serial.write(12); //"Backward-Forward"
          delay(GES_QUIT_TIME);
        }
        else
        {
          Serial.write(7); //"Backward"
          delay(GES_QUIT_TIME);
        }
        break;
      case GES_CLOCKWISE_FLAG:
        Serial.write(13); //"Clockwise"
        break;
      case GES_COUNT_CLOCKWISE_FLAG:
        Serial.write(14); //"anti-clockwise"
        break;
      default:
        paj7620ReadReg(0x44, 1, &data1);
        if (data1 == GES_WAVE_FLAG)
        {
          Serial.write(15); //"wave"
        }
        break;
    }
  }
  delay(100);
}

