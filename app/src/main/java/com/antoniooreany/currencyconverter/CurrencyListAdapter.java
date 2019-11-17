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

    private List<CurrencyElement> currencyElementList;

    public CurrencyListAdapter(List<CurrencyElement> currencyElementList) {
        this.currencyElementList = currencyElementList;
    }

    @Override
    public int getCount() {
        return currencyElementList.size();
    }

    @Override
    public Object getItem(int position) {
        return currencyElementList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        CurrencyElement currencyElement = currencyElementList.get(position);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.currency_element_layout, null, false);
        }
        int imageId = context.getResources().getIdentifier(
                "flag_" + currencyElement.getCurrencyName().toLowerCase(), "drawable", context.getPackageName());
        ImageView imageView = view.findViewById(R.id.flagId);
        imageView.setImageResource(imageId);
        TextView currencyNameTextView = view.findViewById(R.id.currencyName);
        TextView rateForOneEuroTextView = view.findViewById(R.id.rateForOneEuro);
        currencyNameTextView.setText(currencyElement.getCurrencyName());
        double rateForOneEuro = currencyElement.getRateForOneEuro();
        String rateForOneEuroRoundedString = Utils.getRoundNumber(rateForOneEuro);
        rateForOneEuroTextView.setText(rateForOneEuroRoundedString);
        return view;
    }
}
