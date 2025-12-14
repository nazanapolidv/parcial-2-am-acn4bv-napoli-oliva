package com.example.parcial_1_am_acn4bv_napoli_oliva;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class RegistroActivity extends AppCompatActivity{
    private EditText editEmail, editPassword, editConfirmPassword;
    private Button btnRegistro;
    private TextView txtIrLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editMailRegistro);
        editPassword = findViewById(R.id.editPasswordRegistro);
        editConfirmPassword = findViewById(R.id.editConfirmPasswordRegistro);
        btnRegistro = findViewById(R.id.btnRegistro);
        txtIrLogin = findViewById(R.id.txtIrLogin);

        btnRegistro.setOnClickListener(v -> registerUser());

        // listener para volver al Login
        txtIrLogin.setOnClickListener (v -> {
            finish();
        });
    }

    private void registerUser (){
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // validar - campos vacios
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // validar - contraseñas iguales
        if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return;
        }

        // validar - contraseña mas de 6 caracteres
        if (password.length() < 6){
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // login ok
                        Toast.makeText(RegistroActivity.this, "Registro exitoso. ¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        // login not ok
                        Toast.makeText(RegistroActivity.this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // se cierra RegistroActivity para que no pueda volver con el botón atrás
    }

}
