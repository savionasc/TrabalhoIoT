package br.quixada.controle_iuminacao;

import android.app.NotificationManager;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;


public class MainActivity extends AppCompatActivity {

    static String MQTTHOST = "tcp://";
    MqttAndroidClient client;
    MqttConnectOptions options;
    String quarto = "";
    final String clientId = MqttClient.generateClientId();
    NotificationCompat.Builder mBuilder;
    String usuario = "";
    String topic = "mobile";

    void notificacao(String titulo, String texto){
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(titulo)
                .setContentText(texto);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent in = getIntent();
        Bundle b = in.getExtras();
        usuario = b.getString("usuario");
        final String host = b.getString("host");
        MQTTHOST += host+":1883";
        options = new MqttConnectOptions();
        options.setUserName("savio_u_mqtt");
        options.setPassword("mqtt_senha".toCharArray());
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    setSubscription();
                    logar();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Falha ao conectar!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        callback();
    }

    private String sFinal(String inicio, String payload){
        String saida = ".";
        if(!payload.endsWith("/")){
            saida = " de "+payload.substring(inicio.length())+".";
            if(!inicio.equals("chamado_em_andamento")){
                this.quarto = payload.substring(inicio.length());
            }
        }
        return saida;
    }


    private void callback() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                Toast.makeText(MainActivity.this, payload, Toast.LENGTH_LONG).show();
                if(payload.startsWith("solicitar_chamado/")){
                    quarto = payload.substring("solicitar_chamado/".length());
                    Toast.makeText(MainActivity.this, "Há chamados solicitados", Toast.LENGTH_SHORT).show();
                    notificacao("Chamados", "Há chamados solicitados");
                    TextView t = findViewById(R.id.textView2);
                    t.setText("Há chamados");
                    Button b = findViewById(R.id.button3);
                    b.setText("Aceitar chamados");
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            encaminharChamado(view);
                        }
                    });


                }else if(payload.startsWith("confirmado_chamado/")){
                    quarto = payload.substring("solicitar_chamado/".length());
                    Toast.makeText(MainActivity.this, "Chamado confirmado "+sFinal("confirmado_chamado/", payload), Toast.LENGTH_SHORT).show();
                    TextView t = findViewById(R.id.textView2);
                    t.setText("Atendendo no quarto de "+quarto);
                    Button b = findViewById(R.id.button3);
                    b.setText("Finalizar chamado");
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finalizarChamado(view);
                        }
                    });
                }else if(payload.startsWith("chamado_finalizado/")){
                    Toast.makeText(MainActivity.this, "Chamado finalizado de :"+sFinal("chamado_finalizado/", payload), Toast.LENGTH_SHORT).show();
                    TextView t = findViewById(R.id.textView2);
                    t.setText("");
                    quarto = "";
                    Button b = findViewById(R.id.button3);
                    b.setText("");
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
                }else if(payload.startsWith("avaliado_chamado/")){
                    String avaliacao = sFinal("avaliado_chamado/", payload);
                    Toast.makeText(MainActivity.this, "Seu serviço foi avaliado como "+avaliacao, Toast.LENGTH_SHORT).show();
                    notificacao("Avaliação", "Seu serviço foi avaliado como "+avaliacao);
                }

                else if(payload.startsWith("funcionario_aceito/")){
                    if(payload.substring("funcionario_aceito/".length()).equals(usuario))
                        Toast.makeText(MainActivity.this, "Login realizado como "+usuario, Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("funcionario_recusado/")){
                    if(payload.substring("funcionario_recusado/".length()).equals(usuario))
                        Toast.makeText(MainActivity.this, "Funcionário já logado!", Toast.LENGTH_SHORT).show();
                }



                else if(payload.startsWith("chamado_desconhecido/")){
                    Toast.makeText(MainActivity.this, "Erro com um chamado"+sFinal("chamado_desconhecido/", payload), Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("chamado_em_andamento/")){
                    Toast.makeText(MainActivity.this, "Erro ao adicionar um chamado de "+sFinal("chamado_em_andamento/", payload), Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("confirmado_chamado/")){
                    Toast.makeText(MainActivity.this, "Confirmado o chamado no quarto de "+sFinal("confirmado_chamado/", payload), Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("chamado_encaminhado/")){
                    Toast.makeText(MainActivity.this, "Encaminhado o chamado no quarto de "+sFinal("chamado_encaminhado/", payload), Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("chamado_finalizado/")){
                    Toast.makeText(MainActivity.this, "Confirmado finalizado no quarto de "+sFinal("chamado_finalizado/", payload), Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("ffuncionario_retirado/")){
                    Toast.makeText(MainActivity.this, "Logout realizado!", Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("funcionario_nao_encontrado/")){
                    Toast.makeText(MainActivity.this, "Falha ao remover funcionário!", Toast.LENGTH_SHORT).show();
                }else if(payload.startsWith("avaliado_chamado/")){
                    Toast.makeText(MainActivity.this, "Seu ultimo serviço foi avaliado como: "+payload.substring("avaliado_chamado/".length()), Toast.LENGTH_SHORT).show();
                }/*else if(payload.equals("Amarelo: Satisfeito")){
                    Toast.makeText(MainActivity.this, "Parabéns o cliente está satisfeito!", Toast.LENGTH_SHORT).show();
                }else if(payload.equals("Vermelho: Insatisfeito")){
                    Toast.makeText(MainActivity.this, "O cliente considerou seu serviço insatisfeito!", Toast.LENGTH_SHORT).show();
                }*/else{
                    mBuilder.setContentText(payload);
                    int mNotificationId = 001;
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void setSubscription() {
        try {
            client.subscribe(topic, 0);
            Toast.makeText(MainActivity.this, topic, Toast.LENGTH_SHORT).show();
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void logar(){
        try{
            String c = "mobile/login/"+usuario;
            client.publish("servidor", c.getBytes(), 0, false);
        }catch (MqttException e){

        }
    }

    public void deslogar(){
        try{
            String c = "mobile/logout/"+usuario;
            client.publish("servidor", c.getBytes(), 0, false);
        }catch (MqttException e){

        }
    }

    public void encaminharChamado(View view){
        try{
            String c = "mobile/encaminhar_chamado/"+quarto; //beatriz/
            client.publish("servidor", c.getBytes(), 0, false);
            Toast.makeText(MainActivity.this, "Chamado encaminhado!", Toast.LENGTH_LONG).show();
        }catch (MqttException e){

        }

        TextView t = findViewById(R.id.textView2);
        t.setText("");
        Button b = findViewById(R.id.button3);
        b.setText("Confirmar chamado");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarChamado(view);
            }
        });
    }

    public void confirmarChamado(View view) {
        try{
            String c = "mobile/confirmar_chamado/"+quarto;
            client.publish("servidor", c.getBytes(), 0, false);
            Toast.makeText(MainActivity.this, "Chamado confirmado!", Toast.LENGTH_LONG).show();
            TextView t = findViewById(R.id.textView2);
            t.setText("Atendendo: "+quarto);
        }catch (MqttException e){

        }
    }
    /*
    public void encaminharChamado(View view) {
        try{
            String c = "chamado/encaminhado";
            client.publish("servidor", c.getBytes(), 0, false);
            //Toast.makeText(MainActivity.this, "Chamado encaminhado!", Toast.LENGTH_LONG).show();
        }catch (MqttException e){
        }
    }
    */

    public void finalizarChamado(View view) {
        try{
            String c = "mobile/finalizar_chamado/"+quarto;
            client.publish("servidor", c.getBytes(), 0, false);
            Toast.makeText(MainActivity.this, "Chamado finalizado!", Toast.LENGTH_LONG).show();
        }catch (MqttException e){
        }
    }
}
