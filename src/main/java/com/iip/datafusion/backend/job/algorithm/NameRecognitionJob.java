package com.iip.datafusion.backend.job.algorithm;

import com.iip.datafusion.util.jsonutil.Result;

/**
 * @Author Junnor.G
 * @Date 2018/2/1 下午4:18
 */
public class NameRecognitionJob {
    private String corpusPath;
    private String tableName;
    private Result result;
    private String dataSourceId;

    public NameRecognitionJob(){
        corpusPath = "";
    }

    public NameRecognitionJob(String corpusPath){
        this.corpusPath = corpusPath;
    }

    public String getCorpusPath() {
        return corpusPath;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
