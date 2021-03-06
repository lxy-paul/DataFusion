package com.iip.datafusion.backend.executor;

import com.iip.datafusion.backend.channel.ChannelManager;
import com.iip.datafusion.backend.common.AbstractTerminatableThread;
import com.iip.datafusion.backend.common.TerminationToken;
import com.iip.datafusion.backend.jdbchelper.JDBCHelper;
import com.iip.datafusion.backend.job.accuracy.AccuracyJob;
import com.iip.datafusion.dgs.model.accuracy.*;
import com.iip.datafusion.util.dbutil.DataSourceRouterManager;
import com.iip.datafusion.util.jsonutil.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class AccuracyJobExecutor extends AbstractTerminatableThread implements JobExecutor<AccuracyJob> {
    private final BlockingQueue<AccuracyJob> workQueue;

    private final AccuracyCheckUnit accuracyCheckUnit = AccuracyCheckHelper.getAccuracyCheckUnit();

    public AccuracyJobExecutor(TerminationToken token, BlockingQueue<AccuracyJob> workQueue){
        super(token);
        this.workQueue = workQueue;
    }

    @Override
    protected void doRun() throws Exception {
        AccuracyJob accuracyJob = ChannelManager.getInstance().getAccuracyChannel().take(workQueue);

        try{
            doJob(accuracyJob);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            terminationToken.reservations.decrementAndGet();
        }
    }

    @Override
    public void doJob(AccuracyJob job) throws Exception {
        String dataSourceId = job.getDataSourceId();
        String tableName = job.getTableName();
        List<Param> paramList = job.getParamList();

        DataSourceRouterManager.setCurrentDataSourceKey(dataSourceId);

        Result result = new Result();
        for(Param pm : paramList){
            switch (pm.getType())
            {
                case 1:
                    result = accuracyCheckUnit.doCheck(tableName,(FormulaParam) pm);
                    System.out.println(result.toString());
                    break;
                case 2:
                    result = accuracyCheckUnit.doCheck(tableName,(ConditionParam) pm);
                    System.out.println(result.toString());
                    break;
                case 3:
                    result = accuracyCheckUnit.doCheck(tableName,(LengthParam) pm);
                    System.out.println(result.toString());
                    break;
                case 4:
                    result = accuracyCheckUnit.doCheck(tableName,(RangeParam) pm);
                    System.out.println(result.toString());
                    break;
                case 5:
                    result = accuracyCheckUnit.doCheck(tableName,(EmailParam) pm);
                    System.out.println(result.toString());
                    break;
            }
        }
    }
}
