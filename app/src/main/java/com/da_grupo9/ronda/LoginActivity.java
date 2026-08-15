package com.da_grupo9.ronda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText editPassword = findViewById(R.id.editPassword);
        EditText editEmail = findViewById(R.id.editEmail);

        Button button = findViewById(R.id.buttonLogin);

        button.setOnClickListener(view -> {

            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "Los campos son obligatorios ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Felicitaciones te logeaste con " + email, Toast.LENGTH_LONG).show();
            }

        });

        Button buttonLoginOtp = findViewById(R.id.buttonLoginOtp);

        buttonLoginOtp.setOnClickListener(view -> {

            String email = editEmail.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresá tu email para recibir el código", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Código enviado a " + email, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                intent.putExtra(OtpActivity.EXTRA_EMAIL, email);
                startActivity(intent);
            }
        });

        Button buttonForgotPassword = findViewById(R.id.buttonForgotPassword);

        buttonForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}