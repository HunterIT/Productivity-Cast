package com.kennyhunter.tvtrack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Update extends AppCompatActivity {


    AlertDialog alertDialog;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Reset Week
        final Button resetWeek = (Button) findViewById(R.id.reset_week);
        resetWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(Update.this)
                        .setTitle("Reset Week")
                        .setMessage("Are you sure you want to reset the week?. ALL DATA WILL BE LOST")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                resetWeek();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        // Reset Day Alert message
        final Button resetDay = (Button) findViewById(R.id.reset_day);
        resetDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(Update.this)
                        .setTitle("Reset Day")
                        .setMessage("Are you sure you want to reset the day?. ALL DAILY DATA WILL BE LOST")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                resetDay();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        final Button updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText input = (EditText) findViewById(R.id.target_text);
                setTarget(input.getText().toString());


            }
        });
    }

    public void resetWeek() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("week_achieved", 0);
        editor.putInt("count1_daily_achieved", 0);
        editor.putInt("count1_total_achieved", 0);
        editor.putInt("count2_daily_achieved", 0);
        editor.putInt("count2_total_achieved", 0);
        editor.apply();
    }

    public void resetDay() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("count1_daily_achieved", 0);
        editor.putInt("count2_daily_achieved", 0);
        editor.apply();
    }

    public void setTarget(String target) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("target", Integer.parseInt(target));
        editor.apply();
    }
}
