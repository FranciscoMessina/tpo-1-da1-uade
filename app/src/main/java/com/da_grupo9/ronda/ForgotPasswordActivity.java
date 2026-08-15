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

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        EditText editEmail = findViewById(R.id.editForgotEmail);
        Button buttonSend = findViewById(R.id.buttonSendRecoveryCode);

        buttonSend.setOnClickListener(view -> {

            String email = editEmail.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresá tu email para recuperar el acceso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Código de recuperación enviado a " + email, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra(ResetPasswordActivity.EXTRA_EMAIL, email);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
