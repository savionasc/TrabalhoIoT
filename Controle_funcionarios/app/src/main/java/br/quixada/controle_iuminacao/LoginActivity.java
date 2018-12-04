package br.quixada.controle_iuminacao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class LoginActivity extends Activity {

    EditText usuarioED;
    EditText hostED;
    String usuario;
    String host;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuarioED = findViewById(R.id.editText);
        hostED = findViewById(R.id.editText2);

    }

    public void click(View view){
        getDataFromForm();
        if (usuario.length() == 0 || host.length() == 0) {

            Toast.makeText(getApplicationContext(), "Preencha os campos", Toast.LENGTH_SHORT).show();
        } else {
            Bundle b = new Bundle();
            b.putString("usuario", usuario);
            b.putString("host", host);

            Intent in = new Intent(LoginActivity.this, MainActivity.class);
            in.putExtras(b);
            startActivity(in);
        }
    }

    public void getDataFromForm() {
        usuario = usuarioED.getText().toString();
        host = hostED.getText().toString();
     }
}
