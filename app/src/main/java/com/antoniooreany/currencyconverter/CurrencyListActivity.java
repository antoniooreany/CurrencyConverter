package com.antoniooreany.currencyconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class CurrencyListActivity extends AppCompatActivity{
    private CurrencyListAdapter currencyListAdapter;
    private ExchangeRateDatabase list = new ExchangeRateDatabase();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);
        currencyListAdapter = new CurrencyListAdapter(Arrays.asList(list.getMembers()));
        ListView listView = findViewById(R.id.currency_list_view);
        listView.setAdapter(currencyListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currency = currencyListAdapter.getItem(position);
                ExchangeRateDatabase data = new ExchangeRateDatabase();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + data.getCapital(currency)));
                startActivity(intent);
            }
        });
    }
}
