package org.softcatala.corrector;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String steps = getResources().getString(R.string.install_steps);
        TextView textView = findViewById(R.id.textViewSteps);
        textView.setText(steps);

        String limitations = getResources().getString(R.string.app_limitations);
        textView = findViewById(R.id.textViewLimitations);
        textView.setText(limitations);
    }
}
