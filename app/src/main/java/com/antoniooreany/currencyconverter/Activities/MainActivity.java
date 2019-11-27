package com.antoniooreany.currencyconverter.Activities;

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
import android.widget.Button;
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

import com.antoniooreany.currencyconverter.Currency.CurrencyElement;
import com.antoniooreany.currencyconverter.Currency.CurrencyListAdapter;
import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.R;
import com.antoniooreany.currencyconverter.Update.ExchangeRateUpdateRunnable;
import com.antoniooreany.currencyconverter.Update.UpdateJobService;
import com.antoniooreany.currencyconverter.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ExchangeRateDatabaseActivity {
    private static final int JOB_ID = 101;
    private TextView textViewOutput;
    private EditText editTextInput;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private ExchangeRateDatabase exchangeRateDatabase;
    private CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(Utils.getCurrencyElementArrayList(this));
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

    public void setExchangeRateDatabase(ExchangeRateDatabase exchangeRateDatabase) {
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

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
        Button buttonCalculate = findViewById(R.id.buttonCalculate);

        ArrayList<CurrencyElement> currencyElementArrayList = Utils.getCurrencyElementArrayList(this);

        CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(currencyElementArrayList);
        spinnerFrom.setAdapter(currencyListAdapter);
        spinnerTo.setAdapter(currencyListAdapter);

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedData sharedData = new SharedData().invoke();
                String stringResult = sharedData.getStringResult();
                textViewOutput.setText(stringResult);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) registerService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("Currencies were updated"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
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
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currencies_list:
                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh_rates:
                ExchangeRateUpdateRunnable runnable = new ExchangeRateUpdateRunnable(exchangeRateDatabase, this);
                new Thread(runnable).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onClick(View view) {    //TODO Do I need this method ?

//        Spinner Spinner1 = (Spinner) findViewById(R.id.spinner1);
//        Spinner Spinner2 = (Spinner) findViewById(R.id.spinner2);
//        TextView textView = (TextView) findViewById(R.id.textView5);
//        EditText editText = (EditText) findViewById(R.id.editText);
        double exch;
        if (editTextInput.getText().toString().matches("")) {
            exch = 0.0;
        } else {
            exch = exchangeRateDatabase.convert(Double.parseDouble(editTextInput.getText().toString()), spinnerFrom.getSelectedItem().toString(), spinnerTo.getSelectedItem().toString());
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String value = "" + df.format(exch);
        textViewOutput.setText(value);
        double money;
        if (editTextInput.getText().toString().matches("")) {
            money = 0.0;
        } else {
            money = Double.parseDouble(editTextInput.getText().toString());
        }
        setShareText("Currency Converter says: " + money + " " + spinnerFrom.getSelectedItem().toString() + " are " + value + " " + spinnerTo.getSelectedItem().toString());

    }



    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences ShPref = getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorShared = ShPref.edit();
        String value = ((EditText) findViewById(R.id.editTextInput)).getText().toString();
        int spinnerFromSelectedItemPosition = spinnerFrom.getSelectedItemPosition();
        int spinnerToSelectedItemPosition = spinnerTo.getSelectedItemPosition();
        editor.putString("Convert amount", value);
        editor.putInt("Convert from", spinnerFromSelectedItemPosition);
        editor.putInt("Convert to", spinnerToSelectedItemPosition);
        for (String currency : exchangeRateDatabase.getCurrencies()) {
            editorShared.putString(currency, Double.toString(exchangeRateDatabase.getExchangeRate(currency)));
        }
        editor.apply();
        editorShared.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
        String preferencesString = preferences.getString("Convert amount", "");
        int convertFrom = preferences.getInt("Convert From", 0);
        int convertTo = preferences.getInt("Convert to", 0);
        ((EditText) findViewById(R.id.editTextInput)).setText(preferencesString);
        spinnerFrom.setSelection(convertFrom);
        spinnerTo.setSelection(convertTo);

        for (String currency : exchangeRateDatabase.getCurrencies()) {
            double ShareExchangeRate = Double.parseDouble(Objects.requireNonNull(sharedPreferences.getString(currency,
                    Double.toString(exchangeRateDatabase.getExchangeRate(currency)))));
            exchangeRateDatabase.setExchangeRate(currency, ShareExchangeRate);
        }

    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public void registerService() {
        ComponentName serviceName = new ComponentName(this, UpdateJobService.class);
        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(JOB_ID, serviceName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false).setRequiresCharging(false).setPersisted(true).setPeriodic(Utils.JOB_MIN_INTERVAL_MILLIS).build();
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler.getPendingJob(JOB_ID) == null) { jobScheduler.schedule(jobInfo); }
        }
    }

    private class SharedData {
        private double doubleFrom;
        private String currencyFrom;
        private String currencyTo;
        private String stringResult;

        double getDoubleFrom() {
            return doubleFrom;
        }

        String getCurrencyFrom() {
            return currencyFrom;
        }

        String getCurrencyTo() {
            return currencyTo;
        }

        String getStringResult() {
            return stringResult;
        }

        SharedData invoke() {
            try {
                doubleFrom = Double.valueOf(editTextInput.getText().toString());
            } catch (NumberFormatException e) {
                doubleFrom = 0;
            }
            currencyFrom = Utils.getSelectedCurrency(spinnerFrom);
            currencyTo = Utils.getSelectedCurrency(spinnerTo);
            double doubleResult = exchangeRateDatabase.convert(doubleFrom, currencyFrom, currencyTo);
            stringResult = Utils.getRoundedNumber(doubleResult);

            String textToShare = "Currency Converter says: "
                    + getDoubleFrom()
                    + " "
                    + getCurrencyFrom()
                    + " are "
                    + getStringResult()
                    + " "
                    + getCurrencyTo();
            setShareText(textToShare);
            return this;
        }
    }
}
