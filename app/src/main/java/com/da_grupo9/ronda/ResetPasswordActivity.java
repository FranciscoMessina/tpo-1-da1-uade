package com.da_grupo9.ronda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResetPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        String email = getIntent().getStringExtra(EXTRA_EMAIL);

        TextView textEmail = findViewById(R.id.textResetEmail);
        textEmail.setText("Ingresá el código enviado a " + email + " y elegí tu nueva contraseña");

        EditText editCode = findViewById(R.id.editResetCode);
        EditText editNewPassword = findViewById(R.id.editNewPassword);
        EditText editConfirmPassword = findViewById(R.id.editConfirmPassword);
        Button buttonReset = findViewById(R.id.buttonResetPassword);
        Button buttonResend = findViewById(R.id.buttonResendRecoveryCode);

        buttonReset.setOnClickListener(view -> {

            String code = editCode.getText().toString();
            String newPassword = editNewPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();

            if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        buttonResend.setOnClickListener(view ->
                Toast.makeText(this, "Código reenviado a " + email, Toast.LENGTH_SHORT).show());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
