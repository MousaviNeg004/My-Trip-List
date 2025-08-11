package ir.shariaty.mytriplist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);  // اتصال به فایل XML

        // انیمیشن سه‌نقطه‌ای
        TextView dots = findViewById(R.id.dots);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int count = 0;
            String[] dotsArray = {".", "..", "..."};

            @Override
            public void run() {
                dots.setText(dotsArray[count % 3]);
                count++;
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnable);

        // پیکربندی ورود با گوگل
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // از google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // شروع ورود با گوگل
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Toast.makeText(this, "ورود با گوگل ناموفق بود: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // موفقیت‌آمیز: رفتن به ActivityMain2
                        startActivity(new Intent(GoogleSignInActivity.this, MainActivity2.class));
                        finish();
                    } else {
                        Toast.makeText(this, "احراز هویت Firebase شکست خورد", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}