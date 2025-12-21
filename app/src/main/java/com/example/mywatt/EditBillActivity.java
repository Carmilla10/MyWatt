package com.example.mywatt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EditBillActivity extends AppCompatActivity {

    private LinearLayout layoutDetails, layoutEdit;
    private TextView tvTitle, tvMonthDetail, tvUnitDetail, tvTotalDetail, tvRebateDetail, tvFinalDetail;
    private EditText etEditUnits;
    private RadioGroup radioGroupEditRebate; 
    private TextView tvEditTotalCharges, tvEditFinalCost;
    private Button btnEdit, btnUpdate, btnDelete, btnCancel;
    private DBHelper dbHelper;
    private long billId;
    private String month;
    private int currentUnits;
    private double currentRebate, currentTotal, currentFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);
        setTitle("Bill Details");

        dbHelper = new DBHelper(this);
        billId = getIntent().getLongExtra("bill_id", -1);

        // Find views for details section
        tvTitle = findViewById(R.id.tvTitle);
        layoutDetails = findViewById(R.id.layoutDetails);
        layoutEdit = findViewById(R.id.layoutEdit);
        tvMonthDetail = findViewById(R.id.tvMonthDetail);
        tvUnitDetail = findViewById(R.id.tvUnitDetail);
        tvTotalDetail = findViewById(R.id.tvTotalDetail);
        tvRebateDetail = findViewById(R.id.tvRebateDetail);
        tvFinalDetail = findViewById(R.id.tvFinalDetail);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        // Find views for edit section
        etEditUnits = findViewById(R.id.etEditUnits);
        radioGroupEditRebate = findViewById(R.id.radioGroupEditRebate); // Changed from Spinner
        tvEditTotalCharges = findViewById(R.id.tvEditTotalCharges);
        tvEditFinalCost = findViewById(R.id.tvEditFinalCost);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        loadBillData();

        // Initially show details, hide edit form
        layoutDetails.setVisibility(View.VISIBLE);
        layoutEdit.setVisibility(View.GONE);
        tvTitle.setText("Bill Details");

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDetails.setVisibility(View.GONE);
                layoutEdit.setVisibility(View.VISIBLE);

                tvTitle.setText("Edit Bill");
                setTitle("Edit Bill");

                // Pre-fill edit form with current values
                etEditUnits.setText(String.valueOf(currentUnits));

                // Set radio button to current rebate
                int rebateInt = (int) currentRebate;
                int radioId = getRadioButtonIdForRebate(rebateInt);
                if (radioId != -1) {
                    radioGroupEditRebate.check(radioId);
                }

                // Show current calculations
                tvEditTotalCharges.setText(String.format("RM %.2f", currentTotal));
                tvEditFinalCost.setText(String.format("RM %.2f", currentFinal));
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBill();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch back to details view
                layoutDetails.setVisibility(View.VISIBLE);
                layoutEdit.setVisibility(View.GONE);

                tvTitle.setText("Bill Details");
                setTitle("Bill Details");
            }
        });

        // Real-time calculation when units change OR rebate changes
        etEditUnits.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    calculateEditBill();
                }
            }
        });

        // Also calculate when rebate changes
        radioGroupEditRebate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                calculateEditBill();
            }
        });
    }

    private void loadBillData() {
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT month, unit, total_charges, rebate, final_cost FROM " +
                        DBHelper.TABLE_NAME + " WHERE id = ?",
                new String[]{String.valueOf(billId)}
        );

        if (cursor.moveToFirst()) {
            month = cursor.getString(0);
            currentUnits = cursor.getInt(1);
            currentTotal = cursor.getDouble(2);
            currentRebate = cursor.getDouble(3);
            currentFinal = cursor.getDouble(4);

            // Display details
            tvMonthDetail.setText(month.toUpperCase());
            tvUnitDetail.setText(currentUnits + " kWh");
            tvTotalDetail.setText("RM " + String.format("%.2f", currentTotal));
            tvRebateDetail.setText((int)currentRebate + "%");
            tvFinalDetail.setText("RM " + String.format("%.2f", currentFinal));
        }
        cursor.close();
    }

    private void calculateEditBill() {
        String unitsStr = etEditUnits.getText().toString().trim();
        if (!unitsStr.isEmpty()) {
            try {
                int units = Integer.parseInt(unitsStr);
                if (units >= 1 && units <= 1000) {
                    // Get rebate from radio buttons
                    int selectedRadioId = radioGroupEditRebate.getCheckedRadioButtonId();
                    RadioButton selectedRadio = findViewById(selectedRadioId);
                    if (selectedRadio != null) {
                        String rebateText = selectedRadio.getText().toString(); // e.g., "3%"
                        double rebate = Double.parseDouble(rebateText.replace("%", ""));

                        double totalCharges = computeCharges(units);
                        double finalCost = totalCharges - (totalCharges * rebate / 100.0);

                        tvEditTotalCharges.setText(String.format("RM %.2f", totalCharges));
                        tvEditFinalCost.setText(String.format("RM %.2f", finalCost));
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private void updateBill() {
        String unitsStr = etEditUnits.getText().toString().trim();

        if (unitsStr.isEmpty()) {
            etEditUnits.setError("Please enter electricity units");
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units < 1 || units > 1000) {
                etEditUnits.setError("Units must be between 1-1000 kWh");
                return;
            }
        } catch (Exception e) {
            etEditUnits.setError("Invalid number");
            return;
        }

        // Get rebate from radio buttons
        int selectedRadioId = radioGroupEditRebate.getCheckedRadioButtonId();
        RadioButton selectedRadio = findViewById(selectedRadioId);
        if (selectedRadio == null) {
            Toast.makeText(this, "Please select a rebate", Toast.LENGTH_SHORT).show();
            return;
        }

        String rebateText = selectedRadio.getText().toString();
        double rebate = Double.parseDouble(rebateText.replace("%", ""));

        double totalCharges = computeCharges(units);
        double finalCost = totalCharges - (totalCharges * rebate / 100.0);

        // Update in database
        dbHelper.getWritableDatabase().execSQL(
                "UPDATE " + DBHelper.TABLE_NAME + " SET " +
                        "unit = ?, total_charges = ?, rebate = ?, final_cost = ? WHERE id = ?",
                new Object[]{units, totalCharges, rebate, finalCost, billId}
        );

        Toast.makeText(this, "Bill updated successfully", Toast.LENGTH_SHORT).show();

        // Reload data and switch back to details view
        loadBillData();
        layoutDetails.setVisibility(View.VISIBLE);
        layoutEdit.setVisibility(View.GONE);

        tvTitle.setText("Bill Details");
        setTitle("Bill Details");
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.getWritableDatabase().execSQL(
                                "DELETE FROM " + DBHelper.TABLE_NAME + " WHERE id = ?",
                                new Object[]{billId}
                        );
                        Toast.makeText(EditBillActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private double computeCharges(int units) {
        double total = 0.0;

        if (units > 600) {
            total += 200 * 0.218;
            total += 100 * 0.334;
            total += 300 * 0.516;
            total += (units - 600) * 0.546;
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

    private int getRadioButtonIdForRebate(int rebate) {
        switch (rebate) {
            case 0: return R.id.editRadio0;
            case 1: return R.id.editRadio1;
            case 2: return R.id.editRadio2;
            case 3: return R.id.editRadio3;
            case 4: return R.id.editRadio4;
            case 5: return R.id.editRadio5;
            default: return -1;
        }
    }
}