package cn.net.communion.sync.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.net.communion.sync.elasticsearch.Client;
import cn.net.communion.sync.entity.JobInfo;
import cn.net.communion.sync.helper.SysProps;
import cn.net.communion.sync.service.JdbcConnector;

public class Task implements Job {
    Logger logger = Logger.getLogger(Task.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        JobInfo info = (JobInfo) data.get("jobInfo");
        if (data.getBoolean("cancel")) {
            logger.info(info.getName() + " new task just be canceled, this job is in running");
            return;
        }
        String[] params = info.getParams();
        int[] paramTypes = info.getParamTypes();
        long sum = 0;
        String time = LocalDateTime.now().toString();
        while (true) {
            String sql = info.getSql() + limitAddStep(sum, info.getStep());
            List<Map<String, Object>> result =
                    (info.getSql().indexOf("?") > 0 && params != null && paramTypes != null)
                            ? JdbcConnector.query(sql, parseSysParams(params), paramTypes)
                            : JdbcConnector.query(sql);
            sum += result.size();
            if (result.size() > 0 && Client.bulkIndex(info.getIndex(), info.getType(), result)) {
                logger.info(info.getName() + " has already sync " + sum + " records successfully");
            }
            if (result.size() < 10000) {
                logger.info(info.getName() + " sync end, this job total records: " + sum);
                break;
            }
        }
        updateSysParams(info.getParams(), time);
    }

    private String limitAddStep(long start, long step) {
        return new StringBuilder(" limit ").append(start).append(",").append(step).toString();
    }

    private Object[] parseSysParams(String[] params) {
        List<Object> list = new ArrayList<Object>();
        for (int index = 0; index < params.length; index++) {
            list.add(SysProps.get(params[index], ""));
        }
        return list.toArray();
    }

    private void updateSysParams(String[] params, String value) {
        for (int index = 0; index < params.length; index++) {
            SysProps.update(params[index], value);
        }
    }
}
