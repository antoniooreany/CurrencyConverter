package com.antoniooreany.currencyconverter;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int JOB_ID = 101;
    private static final String UPDATED_CURRENCIES = "Updated Currencies";
    private static final String CONVERT_AMOUNT = "Convert amount";
    private static final String CONVERT_FROM = "Convert from";
    private static final String CONVERT_TO = "Convert to";
    private TextView textViewOutput;
    private EditText editTextInput;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();
    private CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(Arrays.asList(exchangeRateDatabase.getMembers()));
    private ShareActionProvider shareActionProvider;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currencyListAdapter.notifyDataSetChanged();
            Toast toast = Toast.makeText(MainActivity.this, "Currency update finished!", Toast.LENGTH_SHORT);
            toast.show();
            Log.d("receiver", "Got message: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewOutput = findViewById(R.id.textViewOutput);
        editTextInput = findViewById(R.id.editTextInput);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);

        spinnerFrom.setAdapter(currencyListAdapter);
        spinnerTo.setAdapter(currencyListAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerService();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("Currencies were updated"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); //TODO not "menu", but "currencies_list" ?
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);
        return true;
    }

    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent); //TODO Throws NPE => shareActionProvider == null
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currencies_list: // TODO Or "menu" instead of "currencies_list"
                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh_rates: // TODO
                ExchangeRateUpdateRunnable runnable = new ExchangeRateUpdateRunnable(exchangeRateDatabase, this);
                new Thread(runnable).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onClick(View view) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        String editTextInputString = editTextInput.getText().toString();
        double editTextInputValue = editTextInputString.matches("") ? 0.0 : Double.parseDouble(editTextInputString);

        String currencyFromString = spinnerFrom.getSelectedItem().toString();
        String currencyToString = spinnerTo.getSelectedItem().toString();

        double textViewOutputValue = exchangeRateDatabase.convert(editTextInputValue, currencyFromString, currencyToString);
        String textViewOutputString = decimalFormat.format(textViewOutputValue);
        textViewOutput.setText(textViewOutputString);

        setShareText("Currency Converter says: "
                + editTextInputString + " "
                + currencyFromString + " are "
                + textViewOutputString + " "
                + currencyToString);
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences(UPDATED_CURRENCIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        EditText editTextInput = findViewById(R.id.editTextInput);
        String editTextInputString = editTextInput.getText().toString();
        int selectedItemPositionFrom = spinnerFrom.getSelectedItemPosition();
        int selectedItemPositionTo = spinnerTo.getSelectedItemPosition();
        preferencesEditor.putString(CONVERT_AMOUNT, editTextInputString);
        preferencesEditor.putInt(CONVERT_FROM, selectedItemPositionFrom);
        preferencesEditor.putInt(CONVERT_TO, selectedItemPositionTo);
        for (String currency : exchangeRateDatabase.getCurrencies()) {
            sharedPreferencesEditor.putString(currency, Double.toString(exchangeRateDatabase.getExchangeRate(currency)));
        }
        preferencesEditor.apply();
        sharedPreferencesEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences(UPDATED_CURRENCIES, Context.MODE_PRIVATE);
        String preferencesString = preferences.getString(CONVERT_AMOUNT, "");
        int selectedItemPositionFrom = preferences.getInt(CONVERT_FROM, 0);
        int selectedItemPositionTo = preferences.getInt(CONVERT_TO, 0);
        EditText editTextInput = findViewById(R.id.editTextInput);
        editTextInput.setText(preferencesString);
        spinnerFrom.setSelection(selectedItemPositionFrom);
        spinnerTo.setSelection(selectedItemPositionTo);
        for (String currency : exchangeRateDatabase.getCurrencies()) {
            double ShareExchangeRate = Double.parseDouble(Objects.requireNonNull(sharedPreferences.getString(currency,
                    Double.toString(exchangeRateDatabase.getExchangeRate(currency)))));
            exchangeRateDatabase.setExchangeRate(currency, ShareExchangeRate);
        }

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public void registerService() {
        ComponentName componentName = new ComponentName(this, UpdateJobService.class);
        JobInfo jobInfo;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .setPersisted(true)
                    .setPeriodic(86400000)  //TODO
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler.getPendingJob(JOB_ID) == null) {
                scheduler.schedule(jobInfo);
            }
        }
    }
}