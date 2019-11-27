package com.antoniooreany.currencyconverter;

import android.content.Context;
import android.util.Log;
import android.widget.Spinner;

import com.antoniooreany.currencyconverter.Activities.ExchangeRateDatabaseActivity;
import com.antoniooreany.currencyconverter.Currency.CurrencyElement;
import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.Update.A;
import com.antoniooreany.currencyconverter.Update.UpdateNotifier;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static java.lang.String.format;

public class Utils {
    private static final String SPEC = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final String FORMAT_STRING = "%%.%df";
    private static final int PLACES_AFTER_DECIMAL_POINT = 2;
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MILLIS_PER_SECOND = 1000;
    public static final int JOB_MIN_INTERVAL_MILLIS = MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY;

    public static String getRoundedNumber(double doubleResult) {
        return format(format(FORMAT_STRING, PLACES_AFTER_DECIMAL_POINT), doubleResult);
    }

    public static String getSelectedCurrency(Spinner spinner) {
        CurrencyElement selectedItemFrom = (CurrencyElement) (spinner.getSelectedItem());
        return selectedItemFrom.getCurrencyName();
    }

    public static ArrayList<CurrencyElement> getCurrencyElementArrayList(ExchangeRateDatabaseActivity activity) {
        ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();
        activity.setExchangeRateDatabase(exchangeRateDatabase);
        String[] currenciesList = exchangeRateDatabase.getCurrencies();

        ArrayList<CurrencyElement> currencyElementArrayList = new ArrayList<>();
        for (String currency : currenciesList) {
            CurrencyElement currencyElement = new CurrencyElement(currency);
            currencyElementArrayList.add(currencyElement);
        }
        //         TODO Try to simplify previous scope somehow like this:
//        ArrayList<CurrencyElement> data = new ArrayList<>(Arrays.asList(new CurrencyElement(currenciesList)));
        return currencyElementArrayList;
    }

    public static void update(Context context, A a) {
        UpdateNotifier updateNotifier = new UpdateNotifier(context);
        try {
            URL url = new URL(Utils.SPEC);
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
//                        new ExchangeRateDatabase().setExchangeRate(xmlPullParser.getAttributeValue(null, "currency"),
                        a.getExchangeRateDatabase().setExchangeRate(xmlPullParser.getAttributeValue(null, "currency"),
                                Double.parseDouble(xmlPullParser.getAttributeValue(null, "rate")));
//                        Log.e("xmlPullParser.getAtt = ", xmlPullParser.getAttributeValue(null, "currency").toString());
//                        Log.e("Double.parseDouble(x = ", String.valueOf(Double.parseDouble(xmlPullParser.getAttributeValue(null, "rate"))));

                    } catch (NumberFormatException e) {
                        Log.e("CurrencyConverter", "Entry doesn't exist");
                        e.printStackTrace();
                    }
                }
                eventType = xmlPullParser.next();
            }
            inputStream.close();
        } catch (Exception e) {
//            Log.e("a = ", a.toString());
//            Log.e("a.getExchangeRa() = ", a.getExchangeRateDatabase().toString());
//            Log.e("a = ", a.toString());
            //TODO catch each exception independently
            Log.e("CurrencyConverter", "Cannot query ECB!");
            e.printStackTrace();
        }

        updateNotifier.showNotification();
    }
}
