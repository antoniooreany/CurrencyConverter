package com.antoniooreany.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CurrencyListAdapter extends BaseAdapter {

    private List<ExchangeRate> exchangeRateList;

    public CurrencyListAdapter(List<ExchangeRate> list) {
        this.exchangeRateList = list;
    }

    @Override
    public int getCount() {
        return exchangeRateList.size();
    }

    @Override
    public String getItem(int position) {
        return exchangeRateList.get(position).getCurrencyName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.currency_element_layout, null, false);
        }

        ImageView imageViewFlagId = view.findViewById(R.id.flagId);
        String imageFlagName = "flag_" + exchangeRateList.get(position).getCurrencyName().toLowerCase();
        imageViewFlagId.setImageResource(context.getResources().getIdentifier(imageFlagName, "drawable", "com.antoniooreany.currencyconverter"));

        TextView textViewCurrencyName = view.findViewById(R.id.currencyName);
        textViewCurrencyName.setText(exchangeRateList.get(position).getCurrencyName());

        TextView textViewRateForOneEuro = view.findViewById(R.id.rateForOneEuro);
        double rateForOneEuro = exchangeRateList.get(position).getRateForOneEuro();
        textViewRateForOneEuro.setText(String.valueOf(rateForOneEuro));

        return view;

    }

}



