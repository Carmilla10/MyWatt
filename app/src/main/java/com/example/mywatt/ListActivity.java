package com.example.mywatt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private DBHelper dbHelper;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MyWatt – Saved Bills");
        setContentView(R.layout.activity_list);

        dbHelper = new DBHelper(this);
        listView = findViewById(R.id.listView);

        loadList();

        // Click -> open details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, android.view.View view, int pos, long id) {
                Intent i = new Intent(ListActivity.this, EditBillActivity.class);
                i.putExtra("bill_id", id);
                startActivity(i);
            }
        });

        // Long click -> delete option
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                showDeleteDialog(id);
                return true;
            }
        });
    }

    private void loadList() {
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT id AS _id, month, final_cost FROM " + DBHelper.TABLE_NAME + " ORDER BY id DESC", null);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No saved bills found", Toast.LENGTH_SHORT).show();
        }

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item,
                cursor,
                new String[]{"month", "final_cost"},
                new int[]{R.id.tvMonth, R.id.tvFinal},
                0
        ) {
            @Override
            public void setViewText(TextView v, String text) {
                if (v.getId() == R.id.tvFinal) {
                    try {
                        double val = Double.parseDouble(text);
                        v.setText(String.format("RM %.2f", val));
                    } catch (NumberFormatException e) {
                        v.setText(text);
                    }
                } else {
                    super.setViewText(v, text);
                }
            }
        };

        listView.setAdapter(adapter);
    }

    private void showDeleteDialog(final long id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.getWritableDatabase().execSQL(
                                "DELETE FROM " + DBHelper.TABLE_NAME + " WHERE id = ?",
                                new Object[]{id}
                        );
                        Toast.makeText(ListActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                        loadList(); // Refresh list
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }
}