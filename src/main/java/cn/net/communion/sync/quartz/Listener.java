package cn.net.communion.sync.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class Listener implements JobListener {


    @Override
    public String getName() {
        return "Job_Listener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().getName();
        boolean isCancel = JobScheduler.getInstance().isRunning(jobKey);
        context.getJobDetail().getJobDataMap().put("cancel", isCancel);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

    }
}
