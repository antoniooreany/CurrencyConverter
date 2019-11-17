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

    private static class UpdateAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
        private final JobService jobService;

        public UpdateAsyncTask(JobService service){
            this.jobService = service;
        }


        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            UpdateNotifier updateNotifier = new UpdateNotifier(this.jobService);
            ExchangeRateDatabase data = new ExchangeRateDatabase();
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
            SharedPreferences ShPref = this.jobService.getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = ShPref.edit();
            for (int i = 0; i < data.getCurrencies().length; i++) {
                editor.putString(data.getCurrencies()[i], Double.toString(data.getExchangeRate(data.getCurrencies()[i])));
            }editor.apply();

            updateNotifier.showNotification();
            sendMessage();
            return jobParameters[0];
        }

        private void sendMessage() {
            Log.d("sender", "Broadcasting message");
            Intent intent = new Intent("Currencies were updated");
            LocalBroadcastManager.getInstance(this.jobService).sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters){
            jobService.jobFinished(jobParameters,false);
        }
    }
}

