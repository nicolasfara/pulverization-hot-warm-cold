#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>

#define DEBUG
#define VERSION "0.1"

#define BUZZER_SERVICE_UUID "a2066d3e-d566-42bd-9d64-9bf3ae53b504"
#define COMMAND_CHAR_UUID   "e7adc44d-a04e-46b1-aa4b-ded61bc3b0a8"
#define RESPONSE_CHAR_UUID  "e7adc44d-a04e-46b1-aa4b-ded61bc3b0a9"

#define PIN_BUTTON_ARM 23
#define PIN_BUTTON_DISARM 22

BLEServer* pServer = NULL;
BLECharacteristic* pCommandCharacteristic = NULL;
BLECharacteristic* pResponseCharacteristic = NULL;
int deviceConnected = 0;
bool oldDeviceConnected = false;

bool armPressed = false;
bool disarmPressed = false;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) 
    {
        deviceConnected++;
        BLEDevice::startAdvertising();
        #ifdef DEBUG
        Serial.print("Device connected. Now there are ");
        Serial.print(deviceConnected);
        Serial.println(" devices connected.");
        #endif
    };

    void onDisconnect(BLEServer* pServer) 
    {
        deviceConnected--;
        pServer->startAdvertising(); // restart advertising
        #ifdef DEBUG
        Serial.print("Device disconnected. Now there are ");
        Serial.print(deviceConnected);
        Serial.println(" devices connected.");
        #endif
    };
};

class MyCharCallbacks: public BLECharacteristicCallbacks
{
    void onWrite(BLECharacteristic *pCharacteristic) 
    {
        std::string rxValue = pCharacteristic->getValue();
        Serial.print(">>> ");
        Serial.println(rxValue.c_str());

        if (rxValue.length() > 0) 
        {
            //TODO
        }

    }
};

void setup() 
{
    #ifdef DEBUG
    Serial.begin(115200);
    Serial.println("\n\n--------------");
    Serial.println("BUZZER HUB");
    Serial.println("--------------");
    Serial.print("VERSION: ");
    Serial.println(VERSION);
    Serial.println("--------------");
    #endif

    pinMode(PIN_BUTTON_ARM, INPUT_PULLUP);
    pinMode(PIN_BUTTON_DISARM, INPUT_PULLUP);

    BLEDevice::init("ESP32");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());

    BLEService *pService = pServer->createService(BUZZER_SERVICE_UUID);

    // Create a BLE Characteristic and a BLE Descriptor for sending the commands
    pCommandCharacteristic = pService->createCharacteristic(COMMAND_CHAR_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    pCommandCharacteristic->addDescriptor(new BLE2902());

    // Create a BLE Characteristic and a BLE Descriptor for receiving the responses
    pResponseCharacteristic = pService->createCharacteristic(RESPONSE_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    pResponseCharacteristic->addDescriptor(new BLE2902());
    pResponseCharacteristic->setCallbacks(new MyCharCallbacks());

    //Start the service
    pService->start();

    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(BUZZER_SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    BLEDevice::startAdvertising();

    #ifdef DEBUG
    Serial.println("BLE is now advertising");
    #endif
}

void loop() 
{
    if(digitalRead(PIN_BUTTON_ARM) == LOW)
    {
        if(!armPressed) //On pressing the button
        {
            delay(100); //Debounce
        }
        armPressed = true;
    }
    else
    {
        if(armPressed) //On releasing the button
        {
            delay(100); //Debounce
        }
        armPressed = false;
    }

    if(digitalRead(PIN_BUTTON_DISARM) == LOW)
    {
        if(!disarmPressed) //On pressing the button
        {
            delay(100); //Debounce
        }
        disarmPressed = true;
    }
    else
    {
        if(disarmPressed) //On releasing the button
        {
            delay(100); //Debounce
        }
        disarmPressed = false;
    }
}

void armBuzzer()
{
    if (deviceConnected) 
    {
        #ifdef DEBUG
        Serial.println("<<< ARM");
        #endif
        pCommandCharacteristic->setValue("ARM");
        pCommandCharacteristic->notify();
        //delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }
}

void disarmBuzzer()
{
    if (deviceConnected) 
    {
        #ifdef DEBUG
        Serial.println("<<< DISARM");
        #endif
        pCommandCharacteristic->setValue("DISARM");
        pCommandCharacteristic->notify();
        //delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }
}