package ir.shariaty.mytriplist;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        setContentView(R.layout.activity_alarm);

        String tripTitle = getIntent().getStringExtra("tripTitle");
        if (tripTitle == null || tripTitle.isEmpty()) tripTitle = "Trip";

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvMsg   = findViewById(R.id.tvMsg);
        ImageView bell   = findViewById(R.id.imgBell);

        tvTitle.setText("Reminder: " + tripTitle);
        tvMsg.setText("You have a trip tomorrow! Did you pack everything?");

        ObjectAnimator wobble = ObjectAnimator.ofFloat(bell, "rotation", -15f, 15f);
        wobble.setDuration(300);
        wobble.setRepeatMode(ValueAnimator.REVERSE);
        wobble.setRepeatCount(ValueAnimator.INFINITE);
        wobble.start();

        findViewById(R.id.btnDismiss).setOnClickListener(v -> finish());

        findViewById(R.id.btnOpen).setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }
}