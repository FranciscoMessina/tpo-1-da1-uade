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

public class OtpActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);

        String email = getIntent().getStringExtra(EXTRA_EMAIL);

        TextView textEmail = findViewById(R.id.textOtpEmail);
        textEmail.setText("Ingresá el código enviado a " + email);

        EditText editOtp = findViewById(R.id.editOtp);
        Button buttonConfirm = findViewById(R.id.buttonConfirmOtp);
        Button buttonResend = findViewById(R.id.buttonResendOtp);

        buttonConfirm.setOnClickListener(view -> {

            String code = editOtp.getText().toString();

            if (code.isEmpty()) {
                Toast.makeText(this, "Ingresá el código recibido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sesión creada con " + email, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(OtpActivity.this, MainActivity.class);
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
