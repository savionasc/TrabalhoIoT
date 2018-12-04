#include <SPI.h>
#include <Ethernet.h>
#include <PubSubClient.h>
#include <string.h>
#define RED 9
#define YELLOW 8
#define GREEN 7
#define BLUE 6
#define BEATRIZ 0
#define JESCA 1
#define SEGUNDO 2
#define VINICIUS 3
#define YURI 4

int buttonPin = 5;

byte mac[] = { 0xDE, 0xED, 0xBA, 0xFE, 0xFE, 0xED };
IPAddress ip(172, 18, 104, 182);
IPAddress server(172, 18, 104, 79);

void callback(char* topic, byte* payload, unsigned int length);

EthernetClient ethClient;
PubSubClient client(server, 1883, callback, ethClient);
int resp = 0;
int flag = 0;

int quartos[] = {BEATRIZ, JESCA, SEGUNDO, VINICIUS, YURI};

int avaliar(){
  while(resp == 0){
    digitalWrite(RED, HIGH);
    delay(600);
    if (digitalRead(buttonPin) == HIGH) {
      client.publish("servidor", "arduino/avaliar_chamado/insatisfeito");
      digitalWrite(RED, LOW);
      digitalWrite(BLUE, HIGH);
      resp = 0;
      delay(600);
      digitalWrite(BLUE, LOW);
      digitalWrite(RED, HIGH);
      delay(300);
      digitalWrite(RED, LOW);
      return 0;
    }
    digitalWrite(RED, LOW);
    digitalWrite(YELLOW, HIGH);
    delay(600);
    if (digitalRead(buttonPin) == HIGH) { 
      client.publish("servidor", "arduino/avaliar_chamado/satisfeito");
      resp = 2;
    }
    digitalWrite(YELLOW, LOW);
  }
  digitalWrite(RED, LOW);
  digitalWrite(YELLOW, LOW);
  digitalWrite(BLUE, HIGH);
  resp = 0;
  delay(500);
  digitalWrite(BLUE, LOW);
  digitalWrite(YELLOW, HIGH);
  delay(300);
  digitalWrite(YELLOW, LOW);
  return 0;
}

void callback(char* topic, byte* payload, unsigned int length) {
    digitalWrite(RED, HIGH);
  
   char str[80] = "confirmado_chamado/";
   char str2[80] = "chamado_finalizado/";
   if(memcmp("chamado_agendado/",payload,length) == 0 && flag == 1){
    digitalWrite(RED, HIGH);
    flag = 2;
   }else if(memcmp("chamado_espera/",payload,length) == 0 && flag == 2){
    digitalWrite(YELLOW, HIGH);
    flag = 3;
   }else if(memcmp(strcat(str, "beatriz"),payload,length) == 0 && flag == 3){
    digitalWrite(GREEN, HIGH);
    flag = 4;
   }else if(memcmp(strcat(str2, "beatriz"),payload,length) == 0 && flag == 4){
    digitalWrite(RED, LOW);
    digitalWrite(YELLOW, LOW);
    digitalWrite(GREEN, LOW);
    avaliar();
    flag = 0;
   }
   
  if(memcmp("acender/green",payload,length) == 0){
    digitalWrite(GREEN, HIGH);
    client.publish("servidor", "Acendeu o GREEN");
  }else if(memcmp("acender/yellow",payload,length) == 0){
    digitalWrite(YELLOW, HIGH);
    client.publish("servidor", "Acendeu o YELLOW");
  }else if(memcmp("acender/red",payload,length) == 0){
    digitalWrite(RED, HIGH);
    client.publish("servidor", "Acendeu o RED");
  }else if(memcmp("apagar/green",payload,length) == 0){
    digitalWrite(GREEN, LOW);
    client.publish("servidor", "Apagou o GREEN");
  }else if(memcmp("apagar/yellow",payload,length) == 0){
    digitalWrite(YELLOW, LOW);
    client.publish("servidor", "Apagou o YELLOW");
  }else if(memcmp("apagar/red",payload,length) == 0){
    digitalWrite(RED, LOW);
    client.publish("servidor", "Apagou o RED");
  }


   /*else if(memcmp("chamado/confirmado",payload,length) == 0 && flag == 1){
    digitalWrite(YELLOW, HIGH);
    client.publish("servidor", "Recebido o confirmamento!");
    flag = 2;
  }else if(memcmp("chamado/encaminhado",payload,length) == 0 && flag == 2){
    digitalWrite(GREEN, HIGH);
    client.publish("servidor", "Recebido o encaminhamento!");
    flag = 3;
  }else if(memcmp("chamado/finalizado",payload,length) == 0 && flag == 3){
    digitalWrite(RED, LOW);
    digitalWrite(YELLOW, LOW);
    digitalWrite(GREEN, LOW);
    client.publish("servidor", "Chamado finalizado!");
    flag = 4;
  }else if(memcmp("chamado/avaliacao",payload,length) == 0 && flag == 4){
    flag = avaliar();
  }*/
  
/*
  if(memcmp("acender/green",payload,length) == 0){
    digitalWrite(GREEN, HIGH);
    client.publish("servidor", "Acendeu o GREEN");
  }else if(memcmp("acender/yellow",payload,length) == 0){
    digitalWrite(YELLOW, HIGH);
    client.publish("servidor", "Acendeu o YELLOW");
  }else if(memcmp("acender/red",payload,length) == 0){
    digitalWrite(RED, HIGH);
    client.publish("servidor", "Acendeu o RED");
  }else if(memcmp("apagar/green",payload,length) == 0){
    digitalWrite(GREEN, LOW);
    client.publish("servidor", "Apagou o GREEN");
  }else if(memcmp("apagar/yellow",payload,length) == 0){
    digitalWrite(YELLOW, LOW);
    client.publish("servidor", "Apagou o YELLOW");
  }else if(memcmp("apagar/red",payload,length) == 0){
    digitalWrite(RED, LOW);
    client.publish("servidor", "Apagou o RED");
  }

*/  
}

void setup(){
  pinMode(RED, OUTPUT);
  pinMode(YELLOW, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  pinMode(buttonPin, INPUT);

  Ethernet.begin(mac, ip);
  if (client.connect("servidor", "savio_u_mqtt", "mqtt_senha")) {
    client.publish("servidor","Arduino conectado!");
    char str[80] = "arduino";
    client.subscribe(str, 1);
  }
}
char str[80];
void loop(){
  client.loop();
  int i = digitalRead(buttonPin);
  if(i == HIGH && flag == 0){ 
      char str[80] = "arduino/solicitar_chamado/";
      client.publish("servidor", strcat(str, "beatriz"));
      //avaliar();
      flag=1;
  }
  delay(150);
}
