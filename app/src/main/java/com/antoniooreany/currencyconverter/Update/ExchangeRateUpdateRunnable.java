package com.antoniooreany.currencyconverter.Update;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.Utils;

public class ExchangeRateUpdateRunnable implements Runnable, A {
    private ExchangeRateDatabase exchangeRateDatabase;
    private Context context;

    public ExchangeRateUpdateRunnable(ExchangeRateDatabase exchangeRateDatabase, Context context) {
        this.exchangeRateDatabase = exchangeRateDatabase;
//        this.exchangeRateDatabase = new ExchangeRateDatabase();
        this.context = context;
    }

    public ExchangeRateDatabase getExchangeRateDatabase() {
        return exchangeRateDatabase;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        synchronized (ExchangeRateUpdateRunnable.this) {
            updateCurrencies();
            sendMessage();
        }
    }

    synchronized private void updateCurrencies() {
        Utils.update(context, this);
    }

    private void sendMessage() {
        Intent intent = new Intent("Currencies were updated");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
