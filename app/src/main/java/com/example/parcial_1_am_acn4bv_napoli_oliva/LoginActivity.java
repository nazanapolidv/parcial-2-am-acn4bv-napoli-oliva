package com.example.parcial_1_am_acn4bv_napoli_oliva;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView txtIrRegistro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // iniciar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // validar si el usuario esta registrado
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
            return;
        }

        // obtener datos de la interfaz
        editEmail = findViewById(R.id.editEmailLogin);
        editPassword = findViewById(R.id.editPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtIrRegistro = findViewById(R.id.txtIrRegistro);

        // login
        btnLogin.setOnClickListener(v -> loginUser());

        // registro
        txtIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // revisar si el usuario ya esta registrado
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
        }
    }

    private void loginUser (){
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase para iniciar sesion
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // login ok
                        Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        // login not ok
                        Toast.makeText(LoginActivity.this, "Error de autenticación: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ir al home
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}