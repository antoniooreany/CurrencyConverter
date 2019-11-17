package com.antoniooreany.currencyconverter.Update;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateRunnable implements Runnable {
    private ExchangeRateDatabase exchangeRateDatabase;
    private Context context;

    public ExchangeRateUpdateRunnable(ExchangeRateDatabase exchangeRateDatabase, Context context) {
        this.exchangeRateDatabase = exchangeRateDatabase;
        this.context = context;
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
        UpdateNotifier updateNotifier = new UpdateNotifier(context);
        String spec = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        try {
            URL url = new URL(spec);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlPullParser.setInput(inputStream, urlConnection.getContentEncoding());
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG &&
                        "Cube".equals(xmlPullParser.getName())
                        && xmlPullParser.getAttributeCount() == 2) {
                    try {
                        exchangeRateDatabase.setExchangeRate(xmlPullParser.getAttributeValue(null, "currency"),
                                Double.parseDouble(xmlPullParser.getAttributeValue(null, "rate")));
                    } catch (NumberFormatException e) {
                        Log.e("CurrencyConverter", "Entry doesn't exist");
                        e.printStackTrace();
                    }
                }
                eventType = xmlPullParser.next();
            }
            inputStream.close();
        } catch (Exception e) {    //TODO catch each exception independently
            Log.e("CurrencyConverter", "Can't query ECB!");
            e.printStackTrace();
        }

        updateNotifier.showNotification();
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("Currencies were updated");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
