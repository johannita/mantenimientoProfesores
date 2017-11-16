package com.example.joha.mantenimiento;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.AccessController;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class login_activity extends AppCompatActivity {

    /*VARIABLES GLOBALES*/
    EditText valContrasenna,valNombreUsuario;
    Button botonLogin;
    String nombreUsuario;
    CheckBox checkBox;
    private Conexion conexion = new Conexion();


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try{
            if(intent.getExtras()!= null){
                setIntent(intent);
                for (String key : getIntent().getExtras().keySet()) {
                    try{
                        Object value = getIntent().getExtras().get(key);
                        if(key.equals("mensaje")){
                            Global.idABuscar=Integer.valueOf((String) value);
                            enviarAOtraPaginaSegunPush();
                        }
                    }catch (Exception e){

                    }
                }
            }
        }
        catch (Exception e){

        }
    }


    public void enviarAOtraPaginaSegunPush(){
        try{
            Call<Reporte> call = conexion.getServidor().obtenerInfoReporte(Global.idABuscar);
            call.enqueue(new Callback<Reporte>() {
                @Override
                public void onResponse(Call<Reporte> call, Response<Reporte> response) {
                    Reporte reporte= response.body();
                    if(!Global.sharedPreferences_username.isEmpty() && !Global.sharedPreferences_password.isEmpty()){
                        Autentificacion.setNombreUsuarioConectado(Global.sharedPreferences_username);
                        if(reporte.getEstadoReporte().equals("informacion")){
                            Global.idSender= "MasInformacion";
                            Intent paginaPrincipal= new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(paginaPrincipal);
                        }
                        else{
                            Global.idSender= "info";
                            Intent paginaPrincipal= new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(paginaPrincipal);
                        }
                    }else{
                        if(reporte.getEstadoReporte().equals("informacion")){
                            Global.idSender= "MasInformacion";
                        }
                        else{
                            Global.idSender= "info";
                        }
                    }
                }

                @Override
                public void onFailure(Call<Reporte> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }
    }

    @Override
    protected void onResume() {
        valContrasenna= (EditText)findViewById(R.id.loginInputContrasenna);
        valNombreUsuario= (EditText)findViewById(R.id.loginInputNombreUsuario);
        checkBox= (CheckBox)findViewById(R.id.checkBoxUsuario);
        botonLogin= (Button)findViewById(R.id.loginBotonIniciar);
        Global.sharedPreferences =  getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        Global.sharedPreferences_username = Global.sharedPreferences.getString("username","");
        Global.sharedPreferences_password = Global.sharedPreferences.getString("password","");

        valNombreUsuario.setText(Global.sharedPreferences_username);
        valContrasenna.setText(Global.sharedPreferences_password);

        if(!valNombreUsuario.getText().toString().equals("")){
            checkBox.setChecked(true);
        }
        accionarBotonIniciar();
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null)
        {
            for (String key : getIntent().getExtras().keySet()) {
                try{
                    Object value = getIntent().getExtras().get(key);
                    if(key.equals("mensaje")){
                        Global.idABuscar=Integer.valueOf((String) value);
                        enviarAOtraPaginaSegunPush();
                    }
                }catch (Exception e){

                }
            }
            String desdeBarra =  getIntent().getExtras().getString("message","");
            if(!desdeBarra.equals("Back")){
                iniciarService();
            }
            boolean mostrarToast = getIntent().getExtras().getBoolean("Barra", false);
            if (mostrarToast)
            {
                iniciarService();
            }
        }else

            iniciarService();
            setContentView(R.layout.activity_login_activity);
    }

    public void iniciarService(){
        startService(new Intent(getApplicationContext(), MyService.class));
    }


    private void accionarBotonIniciar(){
        /*Parámetros:
        * Descripción: Loguea al usuario dentro del sistema
        * */
        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            final String contraseña= valContrasenna.getText().toString();
            nombreUsuario= valNombreUsuario.getText().toString();

            if (contraseña.equals("") || nombreUsuario.equals("")){
                Toast.makeText(getApplication(),Global.errorEspacioVacio,Toast.LENGTH_LONG).show();
            }
            else{
                try{
                    char ultimo = nombreUsuario.charAt(nombreUsuario.length()-1);
                    if(ultimo== ' '){
                        nombreUsuario= nombreUsuario.substring(0,nombreUsuario.length()-1);
                    }
                    Call<Usuario> call = conexion.getServidor().get(nombreUsuario,contraseña);
                    call.enqueue(new Callback<Usuario>() {
                        @Override
                        public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                            if (checkBox.isChecked()){
                                SharedPreferences.Editor editor = Global.sharedPreferences.edit();
                                editor.putString("username",nombreUsuario);
                                editor.putString("password",contraseña);
                                editor.putString("button","active");
                                editor.apply();
                            }
                            Autentificacion.setNombreUsuarioConectado(nombreUsuario);
                            Intent paginaPrincipal= new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(paginaPrincipal);
                        }
                        @Override
                        public void onFailure(Call<Usuario> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),Global.errorUsuarioInvalido,Toast.LENGTH_LONG).show();
                        }
                    });
                }
                catch (Exception e){
                    Toast.makeText(getApplication(),Global.errorConexion,Toast.LENGTH_LONG).show();
                }
            }
            }
        });
    }
}