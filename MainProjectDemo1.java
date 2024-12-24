package MainProject;

import org.firmata4j.I2CDevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.ssd1306.SSD1306;

public class MainProjectDemo1 {
    public static void main(String[] args) throws Exception {
        String myPort = "COM12"; // the port which board is connected to
        IODevice ArduinoBoard = new FirmataDevice(myPort);
        SSD1306 theOledObject = null; // creating the object for oled display
        int mositureLimitValue=550; // the limit which shows if the soil is wet or dry
        try {
            // initializing the board
            ArduinoBoard.start();
            ArduinoBoard.ensureInitializationIsDone();
            System.out.print("Board started");
            // initializing the oled display
            I2CDevice i2cObject = ArduinoBoard.getI2CDevice((byte) 0x3C);
            theOledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
            theOledObject.init();
            theOledObject.getCanvas().clear(); // clearing the oled display
            theOledObject.display();

            // the pins and theirs mode
            Pin moistureSensor = ArduinoBoard.getPin(15); // a pin value for the sensor
            Pin pump = ArduinoBoard.getPin(2); // the pin for water pump
            Pin LEDpin = ArduinoBoard.getPin(4); //  the pin for Led
            moistureSensor.setMode(Pin.Mode.ANALOG); // we want to read analog values of the sensor so the mode is analog
            pump.setMode(Pin.Mode.OUTPUT); // based on the situation the value of the pin will change so its mode is output
            LEDpin.setMode(Pin.Mode.OUTPUT); // based on the situation the value of the pin will change so its mode is output

            while(1==1) { // because we want to run this forever we create a never-ending loop
                long startTime = System.currentTimeMillis();
                long duration = 400000; // 40 sec
                while (System.currentTimeMillis() - startTime < duration) {// the code in this loop runs for 400 seconds
                    ArduinoBoard.start();
                    // measure moisture level
                    double moistureValue = moistureSensor.getValue();
                    String pumpStatus; // the status of the pump which is going to be shown on the oled display
                    if (moistureValue > mositureLimitValue) {
                        pump.setValue(1);
                        LEDpin.setValue(1);
                        pumpStatus = "ON";
                    } else { // if the value is less than mositureLimitValue then its wet enough and the pump and the LED should turn off
                        pump.setValue(0);
                        LEDpin.setValue(0);
                        pumpStatus = "OFF";
                    }

                    theOledObject.getCanvas().clear();
                    theOledObject.getCanvas().drawString(0, 0, "Sensor Voltage: " + moistureValue); // showing the sensor value
                    theOledObject.getCanvas().drawString(0, 10, "Pump Status: " + pumpStatus); // showing the pump status ON/OFF
                    theOledObject.getCanvas().drawString((int) ((mositureLimitValue/1023.0)*128-3), 30, ""+mositureLimitValue); // the limit shown on the visual value
                    theOledObject.getCanvas().drawString((int) ((mositureLimitValue/1023.0)*128), 40, "|");// the limit line

                    theOledObject.getCanvas().drawRect(0,40,128,5); // an empty rectangle which is the bar for sensor value
                    theOledObject.getCanvas().fillRect(0,40,(int)Math.floor(((moistureSensor.getValue())/1023.0)*128),5); // filling the rectangle based on sensor value
                    theOledObject.display();// display everything

                    Thread.sleep(100); // it waits 0.1 sec for taking the value of the sensor so in 400-second period it takes 4000 samples
                }


                pump.setValue(0); // after each 400 sec turns the pump off
                ArduinoBoard.stop();
                Thread.sleep(400000); // wait 400 sec and then start the 400-second period again and run the loop
            }
        } catch (Exception ex) {
            System.out.println("Couldn't connect to board.");
        } finally {
            theOledObject.getCanvas().clear(); // clears the oled display
            theOledObject.display();
        }
    }
}