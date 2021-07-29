// Copyright (C) 2012 Mark R. Stevens
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

#ifndef ardadv_sensors_common_Pin_h
#define ardadv_sensors_common_Pin_h

#include <Arduino.h>

namespace ardadv
{
  namespace sensors
  {
    namespace common
    {

      //! @class Pin
      //!
      //! @brief An immutable class used to reduce error in passing
      //!        pin ids in methods
      //!
      template<uint8_t N>class Pin
      {
      public:

        //! @brief Constructor
        //!
        //! @param[in] iPinId The pin id
        //!
        inline Pin(uint8_t iId = NOT_A_PIN) : mId(iId)
        {
        }

        //! @brief Reset the pin id
        //!
        //! @param[in] iPinId  The pin id
        //! @param[in] iMode   The mode
        //!
        inline void reset(uint8_t iId, uint8_t iMode)
        {
          mId = iId;
          ::pinMode(iId, iMode);
        }

        //! @brief Set the mode
        //!
        //! @param[in] iMode   The mode
        //!
        inline void mode(uint8_t iMode) const
        {
          ::pinMode(mId, iMode);
        }

        //! @brief Digital write to the pin
        //!
        //! @param[in] iValue The value to write
        //!
        inline void digitalWrite(uint8_t iValue) const
        {
          ::digitalWrite(mId, iValue);
        }

        //! @brief Digital read from the pin
        //!
        //! @return the value
        //!
        inline int digitalRead() const
        {
          return ::digitalRead(mId);
        }

        //! @brief Get the pin id
        //!
        //! @return The pin id
        //!
        inline operator uint8_t () const
        {
          return mId;
        }

      private:

        //! @brief The pin id
        //!
        uint8_t mId;

      };
    }
  }
}
#endif

