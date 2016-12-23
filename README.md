## elasticsearch-mysql简单实用的同步工具
简单实用的同步工具，实现mysql数据库中数据定期同步到elasticsearch，只需简单的配置，便能达到非凡的效果。

## 配置说明
主要配置在spring/xml目录下面的四个xml文件中
- **client.xml**：配置cluster.name(集群名称)，集群名称要和elasticsearch保持一致，可以参考elasticsearch.yaml文件
- **db.xml**：配置mysql数据库的连接方式，数据连接池使用的Druid，主要是设置url、username、password，其他采用默认配置即可
- **jobs.xml**：配置数据同步的任务，可以配置多个job，每个job包括：name(任务名称，不要出现重复)、index(elasticsearch的索引)、type(elasticsearch的type)、cron(任务的调度机制)、sql(查询mysql的sql语句)、step(分页查询的的每页数量limit x, **step**)、params(sql语句的参数，用于实现增量同步)、paramTypes(参数类型)
- **nodes.xml**：配置同步到的elasticsearch节点，可以配置多个，每个节点包括：ip(节点的ip地址)、port(节点的端口，一般为9300)

## 编译运行
> git clone git@192.168.1.244:hainan-bigdata/elasticsearch-mysql.git<br/>
> cd elasticsearch-mysql<br/>
> mvn clean package<br/>
> java -jar target/elasticsearch-mysql-0.0.1-SNAPSHOT.jar

## 测试
> $ curl -XGET "127.0.0.1:9200/data/comment/_search?pretty"<br/>

```js
 {
   "took" : 34,
   "timed_out" : false,
   "_shards" : {
     "total" : 5,
     "successful" : 5,
     "failed" : 0
   },
   "hits" : {
     "total" : 39,
     "max_score" : 1.0,
     "hits" : [
       {
         "_index" : "data",
         "_type" : "comment",
         ...
```

## 实现原理
### 增量同步
为了提供同步的效率和对数据库的压力，建议在配置是都采用增量同步的方式，前提是数据表中有设置与时间相关的字段，根据该字段每次同步只会同步新的数据，而不是把已经同步过的字段再同步一遍。
##### 举例说明
```
 <bean class="cn.net.communion.sync.entity.JobInfo"
  p:name="job2"
  p:index="data"
  p:type="comment"
  p:cron="0/10 * * * * ?"
  p:sql="select id,comment from tbl_comment where time > ?"
  p:params="sys.lastTime2"
  p:paramTypes="12"/>
```
在job2的配置中设置sql的条件为 **time > ?** ，而 **?** 指的是 **sys.lastTime2** ，这样每次同步都只会上次同步后更新的数据，**sys.lastTime2** 的名称可以随意更改，但是不要出现重复，每次同步完以后， 将开始同步的时间会为**value**， **sys.lastTime2** 作为 **key** 更新到sys.properties文件中。
> sys.properties文件内容举例

```
#last job finish at
#Wed Dec 21 15:08:10 CST 2016
sys.lastTime2=2016-12-21T15\:08\:10.003
sys.lastTime1=2016-12-21T15\:08\:10.002
```

因此，要想实现全量同步也非常简单，只需将sys.properties文件删除即可，  当程序没有检测到sys.properties文件，便会将所有的数据同步到elasticsearch中。
### 调度机制
采用quartz实现任务调度，最小的粒度可以到秒级，涉及quartz相关的代码主要在JobScheduler.java和Listener.java两个文件中
> JobScheduler部分代码片段

```java
public JobScheduler pushJobs(Collection<JobInfo> infos) {
    infos.forEach(info -> {
        JobDetail job = newJob(Task.class).withIdentity(info.getName(), "jobs").build();
        job.getJobDataMap().put("jobInfo", info);
        CronTrigger trigger = newTrigger().withIdentity(info.getName(), "triggers")
                .withSchedule(cronSchedule(info.getCron())).build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    });
    return this;
}

public boolean isRunning(String jobKey) {
    try {
        for (JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
            if (context.getJobDetail().getKey().getName().equals(jobKey)) {
                return true;
            }
        }
    } catch (SchedulerException e) {
        logger.info("get jobs status failed");
        e.printStackTrace();
    }
    return false;
}

public void start() throws SchedulerException {
    scheduler.start();
}
```

代码说明：
- **pushJobs()** 实现将jobs.xml配置的job添加进quartz的scheduler中
- **isRunning()** 根据job.name检测某个任务是否处于运行状态，对于调度间隔比较短或者同步时间的比较的任务，可能出现新的任务已经开始然而上个任务还未执行完成，这时新的任务会直接被取消执行
- **start()** 启动任务调度


> Listener部分代码片段

```java
@Override
public void jobToBeExecuted(JobExecutionContext context) {
    String jobKey = context.getJobDetail().getKey().getName();
    boolean isCancel = JobScheduler.getInstance().isRunning(jobKey);
    context.getJobDetail().getJobDataMap().put("cancel", isCancel);
}
```
代码说明:
- 每一个任务执行之前均会调用 **jobToBeExecuted()** ，在该方法中调用 **JobScheduler** 的 **isRunning** 并且设置 **isCancel** 值，如果 **isCancel** 为 **true** 便会取消任务的执行。
