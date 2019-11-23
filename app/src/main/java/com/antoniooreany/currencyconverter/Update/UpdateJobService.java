package com.antoniooreany.currencyconverter.Update;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateJobService extends JobService  {

    UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this);

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        updateAsyncTask.execute(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private static class UpdateAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> implements A {
        private final JobService jobService;
        private ExchangeRateDatabase exchangeRateDatabase;

        public UpdateAsyncTask(JobService jobService) {
            this.jobService = jobService;
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            exchangeRateDatabase = new ExchangeRateDatabase();

            UpdateNotifier updateNotifier = new UpdateNotifier(jobService);
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
                Log.e("CurrencyConverter", "Cannot query ECB!");
                e.printStackTrace();
            }

            updateNotifier.showNotification();

            SharedPreferences sharedPreferences = this.jobService.getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
//            for (int i = 0; i < exchangeRateDatabase.getCurrencies().length; i++) {
//                editor.putString(exchangeRateDatabase.getCurrencies()[i],
//                        Double.toString(exchangeRateDatabase.getExchangeRate(exchangeRateDatabase.getCurrencies()[i])));
//            }
            for (String currency : exchangeRateDatabase.getCurrencies()) {
                editor.putString(currency, Double.toString(exchangeRateDatabase.getExchangeRate(currency)));
            }
            editor.apply();

            sendMessage();
            return jobParameters[0];
        }

        private void sendMessage() {
            Log.d("sender", "Broadcasting message");
            Intent intent = new Intent("Currencies were updated");
            LocalBroadcastManager.getInstance(this.jobService).sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobService.jobFinished(jobParameters, false);
        }

        @Override
        public ExchangeRateDatabase getExchangeRateDatabase() {
            return exchangeRateDatabase;
        }
    }
}

