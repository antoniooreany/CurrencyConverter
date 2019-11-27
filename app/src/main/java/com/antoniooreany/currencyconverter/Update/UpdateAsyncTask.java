package com.antoniooreany.currencyconverter.Update;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;
import com.antoniooreany.currencyconverter.Utils;

public class UpdateAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> implements A {
    private final JobService jobService;
    private ExchangeRateDatabase exchangeRateDatabase;  //TODO Here or ... ?

    public UpdateAsyncTask(JobService jobService) {
        this.jobService = jobService;
        this.exchangeRateDatabase = new ExchangeRateDatabase();
    }

    @Override
    protected JobParameters doInBackground(JobParameters... jobParameters) {
//        exchangeRateDatabase = new ExchangeRateDatabase();  //TODO Here or ... ?

        Utils.update(jobService, this);

        SharedPreferences sharedPreferences = this.jobService.getSharedPreferences("Updated Currencies", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (String currency : exchangeRateDatabase.getCurrencies()) {
            editor.putString(currency, Double.toString(exchangeRateDatabase.getExchangeRate(currency)));
        }
        editor.apply();

        sendMessage();
        return jobParameters[0];
    }

    private void sendMessage() {
//        Log.d("sender", "Broadcasting message");
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