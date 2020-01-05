//Library Import
#include <SoftwareSerial.h>                   
#include "DHT.h"
DHT dht;

//Pin number to whitch we connected the sensor
#define DHT11_PIN 2
// Adres czujnika

int alarm_threshold = 0; 
int temperature = 0;
int a;
//Declaring the port for bluetooth
SoftwareSerial Bluetooth(10, 11);             //create Bluetooth instance for 10-RX 11-TX
int buffer_in[200];
int i=0;
int BluetoothDane;                            //Recieved data will be stored here


void setup() {
  pinMode(5, OUTPUT);
  while(!Serial);    //start the transmission with the terminal
   Serial.begin(9600);
  dht.setup(DHT11_PIN);
  Bluetooth.begin(9600);      //start SerialSoftware with 9600 baud speed
  Serial.println("Polaczyles sie  z modulem Bluetooth HC-05");
}

void loop() {
    a  = dht.getTemperature();//untill the temperature will be checked
    //the getStatusString funcition will always return TIMEOUT 
     
    if(dht.getStatusString() == "OK"){
    temperature = dht.getTemperature();
    }
    
    if (temperature>alarm_threshold) 
       {
        digitalWrite(5, HIGH);
       }
     else if(temperature<alarm_threshold)
     {
      digitalWrite(5, LOW);
     }
  
      
        if (Bluetooth.available())                  //If there is data
        {              
        i=0;
        while (Bluetooth.available()>0)  //Read data from HC-05
         {
          buffer_in[i]=Bluetooth.read();           //copy the data to the buffer
          i++;
         }
        Serial.println(i);
        for(int j=0;j<i-2;j++)
         Serial.println(buffer_in[j]);
        if (buffer_in[0]==1)   //command 1 sets the alarm threshold
          {
           if (i>1)
           { 
            if (buffer_in[1]!=0)
             {
              alarm_threshold=buffer_in[1];
              Serial.print("Ustawiono alarm na : ");
              Serial.println(alarm_threshold);       //print info on serial monitor
             } else
              Serial.println("Wartość progu nie może być równa 0."); 
           }else
            Serial.println("Błąd polecenia: za mała liczba bajtów");
           }
       else if (buffer_in[0]==2) //command 2 asks for current alarm threshold
          {
            Bluetooth.println(alarm_threshold);  //send anwser
            Serial.println("Wysłano alarm : ");
            Serial.print(alarm_threshold);       //print info on serial monitor
          }
       else if (buffer_in[0]==3) //command 3 asks for current temperature
       {
            Serial.println("Temperatura: ");
            Serial.println(temperature);
            Bluetooth.println(temperature); 
       }
      } 
delay(100);                                   //wait 100ms 
  }  
