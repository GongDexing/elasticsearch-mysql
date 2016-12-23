package cn.net.communion.sync.main;

import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.net.communion.sync.elasticsearch.Client;
import cn.net.communion.sync.entity.JobInfo;
import cn.net.communion.sync.quartz.JobScheduler;

public class App {
    Logger logger = Logger.getLogger(App.class);
    ClassPathXmlApplicationContext context;

    public App() {
        this.context = new ClassPathXmlApplicationContext("/spring/root.xml");
    }

    public void start() {
        JobScheduler jobScheduler = JobScheduler.getInstance();
        Optional<Set<JobInfo>> optional = Optional.of((Set<JobInfo>) context.getBean("jobs"));
        optional.ifPresent(infos -> {
            try {

                jobScheduler.pushJobs(infos).start();
            } catch (SchedulerException e) {
                Client.shutdown();
                JobScheduler.getInstance().shutdown();
                System.exit(1);
            }
        });
    }

    public static void main(String[] args) {
        new App().start();
    }

}
