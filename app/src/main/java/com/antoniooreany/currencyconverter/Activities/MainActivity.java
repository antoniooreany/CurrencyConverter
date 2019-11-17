package com.antoniooreany.currencyconverter.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.antoniooreany.currencyconverter.Currency.CurrencyElement;
import com.antoniooreany.currencyconverter.Currency.CurrencyListAdapter;
import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.R;
import com.antoniooreany.currencyconverter.Utils;

import java.util.ArrayList;

//import android.widget.Toolbar;
//import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements ExchangeRateDatabaseActivity {

    private TextView textViewOutput;
    private EditText editTextInput;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private ExchangeRateDatabase exchangeRateDatabase;
    private ShareActionProvider shareActionProvider;

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
//                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
//                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
