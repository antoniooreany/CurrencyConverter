package com.antoniooreany.currencyconverter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateRunnable implements Runnable {
    private ExchangeRateDatabase data;
    private Context ctx;

    public ExchangeRateUpdateRunnable(ExchangeRateDatabase data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
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
        UpdateNotifier updateNotifier = new UpdateNotifier(ctx);
        String queryString = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        try{
            URL url  = new URL(queryString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(is, urlConnection.getContentEncoding());
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG &&
                        "Cube".equals(parser.getName())
                        && parser.getAttributeCount()==2) {
                    try {
                        data.setExchangeRate(parser.getAttributeValue(null, "currency"), Double.parseDouble(parser.getAttributeValue(null, "rate")));
                    }catch (Exception ex){
                        Log.e("CurrencyConverter", "Entry doesn't exist");
                        ex.printStackTrace();
                    }
                }
                eventType = parser.next();
            }
        }catch (Exception ex){
            Log.e("CurrencyConverter", "Can't query ECB!");
            ex.printStackTrace();
        }
        updateNotifier.showNotification();
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("Currencies were updated");
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }


}
