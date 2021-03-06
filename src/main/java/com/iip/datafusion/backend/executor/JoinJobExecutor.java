package com.iip.datafusion.backend.executor;

import com.iip.datafusion.backend.channel.ChannelManager;
import com.iip.datafusion.backend.common.AbstractTerminatableThread;
import com.iip.datafusion.backend.common.TerminationToken;
import com.iip.datafusion.backend.jdbchelper.JDBCHelper;
import com.iip.datafusion.backend.job.join.JoinJob;
import com.iip.datafusion.backend.job.join.SQLTask;
import com.iip.datafusion.util.dbutil.DataSourceRouterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class JoinJobExecutor extends AbstractTerminatableThread implements JobExecutor<JoinJob>{
    private final BlockingQueue<JoinJob> workQueue;

    private final JdbcTemplate jdbcTemplate = JDBCHelper.getJdbcTemplate();

    public JoinJobExecutor(TerminationToken token, BlockingQueue<JoinJob> workQueue){
        super(token);
        this.workQueue = workQueue;
    }

    @Override
    protected void doRun() throws Exception {
        JoinJob joinJob = ChannelManager.getInstance().getJoinChannel().take(workQueue);

        try{
            doJob(joinJob);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            terminationToken.reservations.decrementAndGet();
        }
    }

    @Override
    public void doJob(JoinJob job) throws Exception {
        // todo: 实现整合工作

        // 1. 获取sqltasks
        List<SQLTask> sqlTasks = job.getSQLTasks();
        // jdbcTemplate.query
        DataSourceRouterManager.setCurrentDataSourceKey(sqlTasks.get(0).getDatasourceID());
        List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(sqlTasks.get(0).getSql());

        for (Map<String, Object> instance : resultSet
             ) {
            for (int i = 1; i < sqlTasks.size(); i++) {
                Map<String, Object> tempResult;
                DataSourceRouterManager.setCurrentDataSourceKey(sqlTasks.get(i).getDatasourceID());
                String value = (String)instance.get(sqlTasks.get(i).getWhereFieldName());
                tempResult = jdbcTemplate.queryForMap(sqlTasks.get(i).getSql(), value);

                for (String key : tempResult.keySet()
                     ) {
                    instance.put(key, tempResult.get(key));
                }
            }
        }

        // insert
        List<Object[]> joinedResult = new ArrayList<Object[]>();
        Set<String> sourceFields = job.getS2tMap().keySet();
        DataSourceRouterManager.setCurrentDataSourceKey(job.getTargetDatasourceID());
        jdbcTemplate.batchUpdate(job.getInsertSQL(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                int j = 1;
                for (String key : sourceFields
                     ) {
                    preparedStatement.setString(j,(String) resultSet.get(i).get(key));
                }
            }

            @Override
            public int getBatchSize() {
                return resultSet.size();
            }
        });
    }
}
