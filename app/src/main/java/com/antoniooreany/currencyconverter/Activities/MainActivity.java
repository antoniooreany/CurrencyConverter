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
import com.antoniooreany.currencyconverter.Update.ExchangeRateUpdateRunnable;
import com.antoniooreany.currencyconverter.R;
import com.antoniooreany.currencyconverter.Update.UpdateJobService;
import com.antoniooreany.currencyconverter.Utils;

import java.util.ArrayList;
import java.util.Objects;

//import android.widget.Toolbar;
//import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements ExchangeRateDatabaseActivity {
    private static final int JOB_ID = 101;
    private TextView textViewOutput;
    private EditText editTextInput;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private ExchangeRateDatabase exchangeRateDatabase;
//    private CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(Arrays.asList(exchangeRateDatabase.getMembers()));
    private CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(Utils.getCurrencyElementArrayList(this));
    private ShareActionProvider shareActionProvider;
    private ExchangeRateUpdateRunnable runnable;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currencyListAdapter.notifyDataSetChanged();
            Toast toast = Toast.makeText(MainActivity.this, "Currencies update finished!", Toast.LENGTH_SHORT);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currencies_list: // TODO Or "menu" instead of "currencies_list"
                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh_rates: // TODO
                runnable = new ExchangeRateUpdateRunnable(exchangeRateDatabase, this);
                new Thread(runnable).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        SharedPreferences ShPref = getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorShared = ShPref.edit();
        String value = ((EditText) findViewById(R.id.editTextInput)).getText().toString();
        int position1 = spinnerFrom.getSelectedItemPosition();
        int position2 = spinnerTo.getSelectedItemPosition();
        editor.putString("Convert amount", value);
        editor.putInt("Convert From", position1);
        editor.putInt("Convert to", position2);
        for (int i = 0; i < exchangeRateDatabase.getCurrencies().length; i++) {
            editorShared.putString(exchangeRateDatabase.getCurrencies()[i], Double.toString(exchangeRateDatabase.getExchangeRate(exchangeRateDatabase.getCurrencies()[i])));
        }
        editor.apply();
        editorShared.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences ShPref = getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
        String value = pref.getString("Convert amount", "");
        int position1 = pref.getInt("Convert From", 0);
        int position2 = pref.getInt("Convert to", 0);
        ((EditText) findViewById(R.id.editTextInput)).setText(value);
        spinnerFrom.setSelection(position1);
        spinnerTo.setSelection(position2);
        for (int i = 0; i < exchangeRateDatabase.getCurrencies().length; i++) {
            double ShareExchangeRate = Double.parseDouble(Objects.requireNonNull(ShPref.getString(exchangeRateDatabase.getCurrencies()[i],
                    Double.toString(exchangeRateDatabase.getExchangeRate(exchangeRateDatabase.getCurrencies()[i])))));
            exchangeRateDatabase.setExchangeRate(exchangeRateDatabase.getCurrencies()[i], ShareExchangeRate);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(JOB_ID, serviceName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false).setRequiresCharging(false).setPersisted(true).setPeriodic(86400000).build();
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler.getPendingJob(JOB_ID) == null) {
                scheduler.schedule(jobInfo); //TODO No such service ComponentInfo{com.antoniooreany.currencyconverter/com.antoniooreany.currencyconverter.Update.UpdateJobService}
            }
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
            stringResult = Utils.getRoundNumber(doubleResult);

            String textToShare = "Currency Converter says: " // TODO Is it the right place to initialize textToShare ?
                    + getDoubleFrom()
                    + " "
                    + getCurrencyFrom()
                    + " are "
                    + getStringResult()
                    + " "
                    + getCurrencyTo();
            setShareText(textToShare);    //TODO When uncommented, the App falls down

            return this;
        }
    }
}
