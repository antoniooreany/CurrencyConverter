package com.antoniooreany.currencyconverter.Update;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateJobService extends JobService {

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
}

