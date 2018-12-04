package br.ufc.quixada.controller;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Publisher implements Runnable{
	String msg;
	String topic;
	public Publisher(String topic, String msg) {
		// TODO Auto-generated constructor stub
		this.topic = topic;
		this.msg = msg;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        int qos             = 2;
        String broker       = "tcp://localhost:1883";
        String clientId     = "Publicador";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("savio_u_mqtt");
            connOpts.setPassword("mqtt_senha".toCharArray());
            connOpts.setCleanSession(true);
            //System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            //System.out.println("Connected");
            System.out.println("Publishing message: "+this.msg);
            MqttMessage message = new MqttMessage(this.msg.getBytes());
            message.setQos(qos);
            sampleClient.publish(this.topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            //System.out.println("Disconnected");
        } catch(MqttException me) {
            me.printStackTrace();
        }
	}
	
}
