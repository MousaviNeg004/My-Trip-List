package ir.shariaty.mytriplist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // اتصال به فایل XML

        // دسترسی به دکمه‌ها
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // دکمه ثبت‌نام (هدایت به صفحه ثبت‌نام)
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class); // صفحه جداگانه برای ثبت‌نام
            startActivity(intent);
        });

        // دکمه ورود (هدایت به صفحه ورود)
        btnLogin.setOnClickListener(v -> {
            // هدایت به صفحه ورود
            Intent intent = new Intent(MainActivity.this, LoginActivity.class); // صفحه جداگانه برای ورود
            startActivity(intent);
        });

        // دکمه ورود با گوگل
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
        });
    }


    private void signInWithGoogle() {
    }
}
