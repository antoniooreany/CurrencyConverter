package com.antoniooreany.currencyconverter;

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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateJobService extends JobService {

    UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask(this);

    @Override
    public boolean onStartJob(JobParameters params) {
        updateAsyncTask.execute(params);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

//    private static class UpdateAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
//        private static final String QUERY_STRING = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
//        private final JobService jobService;
//
//        public UpdateAsyncTask(JobService jobService) {
//            this.jobService = jobService;
//        }
//
//        @Override
//        protected JobParameters doInBackground(JobParameters... jobParameters) {
//            UpdateNotifier updateNotifier = new UpdateNotifier(this.jobService);
//            ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();
//            try {
//                URL url = new URL(QUERY_STRING);
//                URLConnection urlConnection = url.openConnection();
//                InputStream inputStream = urlConnection.getInputStream();
//                XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
//                xmlPullParser.setInput(inputStream, urlConnection.getContentEncoding());
//                int eventType = xmlPullParser.getEventType();
//                while (eventType != XmlPullParser.END_DOCUMENT) {
//                    if (eventType == XmlPullParser.START_TAG &&
//                            "Cube".equals(xmlPullParser.getName())
//                            && xmlPullParser.getAttributeCount() == 2) {
//                        try {
//                            exchangeRateDatabase.setExchangeRate(xmlPullParser.getAttributeValue(null, "currency"), Double.parseDouble(xmlPullParser.getAttributeValue(null, "rate")));
//                        } catch (Exception e) {
//                            Log.e("CurrencyConverter", "Entry doesn't exist");
//                            e.printStackTrace();
//                        }
//                    }
//                    eventType = xmlPullParser.next();
//                }
//            } catch (Exception e) {
//                Log.e("CurrencyConverter", "Can't query ECB!");
//                e.printStackTrace();
//            }
//            SharedPreferences sharedPreferences = jobService.getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            for (String currency : exchangeRateDatabase.getCurrencies()) {
//                editor.putString(currency, Double.toString(exchangeRateDatabase.getExchangeRate(currency)));
//            }
//            editor.apply();
//            updateNotifier.showNotification();
//            sendMessage();
//            return jobParameters[0];
//        }
//
//        private void sendMessage() {
//            Log.d("sender", "Broadcasting message");
//            Intent intent = new Intent("Currencies were updated");
//            LocalBroadcastManager.getInstance(this.jobService).sendBroadcast(intent);
//        }
//
//        @Override
//        protected void onPostExecute(JobParameters jobParameters) {
//            jobService.jobFinished(jobParameters, false);
//        }
//    }
}

