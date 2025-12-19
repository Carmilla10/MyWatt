package com.example.mywatt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("About Developer");

        // Remove these lines - no back button needed
        // Button btnBack = findViewById(R.id.btnBack);
        // btnBack.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         finish();
        //     }
        // });

        TextView tvGithubUrl = findViewById(R.id.tvGithubUrl);
        tvGithubUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl = "https://github.com/Carmilla10/MyWatt";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
                startActivity(browserIntent);
            }
        });

        TextView tvInstructions = findViewById(R.id.tvInstructions);
        tvInstructions.setText(
                "1. Select month from dropdown.\n" +
                        "2. Enter electricity units (1-1000 kWh).\n" +
                        "3. Select rebate percentage (0-5%).\n" +
                        "4. Click \"CALCULATE & SAVE\".\n" +
                        "5. View saved bills in \"VIEW SAVED BILLS\".\n" +
                        "6. Long press any bill to delete.\n" +
                        "7. Tap any bill to view the bill details.\n" +
                        "8. Tap \"Edit Bill\" to update or \"Cancel\" to go back.\n" +
                        "Note: All data is saved locally on your device.");
    }
}