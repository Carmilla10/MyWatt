package com.example.mywatt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerRebate;
    private EditText etUnits;
    private Button btnCalculate, btnViewList, btnAbout;
    private TextView tvTotalCharges, tvFinalCost;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("MyWatt");

        dbHelper = new DBHelper(this);


        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerRebate = findViewById(R.id.spinnerRebate);
        etUnits = findViewById(R.id.etUnits);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnViewList = findViewById(R.id.btnViewList);
        btnAbout = findViewById(R.id.btnAbout);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);


        String[] rebates = {"0%", "1%", "2%", "3%", "4%", "5%"};
        ArrayAdapter<String> rebateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rebates);
        rebateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRebate.setAdapter(rebateAdapter);

        // Button click listeners
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAndSave();
            }
        });

        btnViewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });
    }

    private void calculateAndSave() {
        String month = spinnerMonth.getSelectedItem().toString();

        // Check units
        String unitsStr = etUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            etUnits.setError("Please enter electricity units");
            Toast.makeText(this, "Please enter electricity units", Toast.LENGTH_SHORT).show();
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units < 1 || units > 1000) {
                etUnits.setError("Units must be between 1-1000 kWh");
                Toast.makeText(this, "Units must be between 1-1000 kWh", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            etUnits.setError("Invalid units value");
            Toast.makeText(this, "Invalid units value", Toast.LENGTH_SHORT).show();
            return;
        }


        String rebateStr = spinnerRebate.getSelectedItem().toString();
        double rebate = Double.parseDouble(rebateStr.replace("%", ""));

        // Calculate
        double totalCharges = computeCharges(units);
        double finalCost = totalCharges - (totalCharges * rebate / 100.0);

        // Show results
        tvTotalCharges.setText(String.format("RM %.2f", totalCharges));
        tvFinalCost.setText(String.format("RM %.2f", finalCost));

        // Save to database
        long id = dbHelper.insertBill(month, units, totalCharges, rebate, finalCost);
        if (id > 0) {
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
            // Clear input
            etUnits.setText("");
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private double computeCharges(int units) {
        double total = 0.0;

        if (units > 600) {
            total += 200 * 0.218;  // First 200 kWh
            total += 100 * 0.334;  // Next 100 kWh
            total += 300 * 0.516;  // Next 300 kWh
            total += (units - 600) * 0.546;  // Beyond 600 kWh
        } else if (units > 300) {
            total += 200 * 0.218;
            total += 100 * 0.334;
            total += (units - 300) * 0.516;
        } else if (units > 200) {
            total += 200 * 0.218;
            total += (units - 200) * 0.334;
        } else {
            total += units * 0.218;
        }

        return total;
    }
}