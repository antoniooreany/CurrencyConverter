package com.antoniooreany.currencyconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CurrencyListActivity extends AppCompatActivity implements ExchangeRateDatabaseActivity {

    private ExchangeRateDatabase exchangeRateDatabase;

    public void setExchangeRateDatabase(ExchangeRateDatabase exchangeRateDatabase) {
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        ArrayList<CurrencyElement> currencyElementArrayList = Utils.getCurrencyElementArrayList(this);

        CurrencyListAdapter currencyListAdapter = new CurrencyListAdapter(currencyElementArrayList);
        ListView currency_list_view = findViewById(R.id.currency_list_view);
        currency_list_view.setAdapter(currencyListAdapter);

        currency_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TextView currencyNameTextView = view.findViewById(R.id.currencyName);
                String currencyName = currencyNameTextView.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0`?q="
                        + exchangeRateDatabase.getCapital(currencyName)));
                startActivity(intent); //TODO Can't search
            }
        });
    }
}
