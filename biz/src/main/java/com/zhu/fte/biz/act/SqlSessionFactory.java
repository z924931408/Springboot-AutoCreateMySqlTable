package com.zhu.fte.biz.act;

import com.zhu.fte.biz.utils.IoUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.*;

/**
 * TODO
 *
 * @author zhujiqian
 * @date 2021/3/13 15:02
 */
@Slf4j
public class SqlSessionFactory{


    private Connection connection ;

    public SqlSessionFactory(Connection connection) {
        this.connection = connection;
    }

    public void schemaOperationsBuild(String type) {
        switch (type){
            case "drop":
                this.dbSchemaDrop();break;
            case "create":
                this.dbSchemaCreate();break;
        }
    }

    /**
     * 删除数据库表
     */
    public void dbSchemaDrop() {
        if (this.isTablePresent()) {
            log.info("开始执行drop");
            this.executeResource("drop", "act");
            log.info("执行drop完成");
        }
    }

    /**
     * 新增数据库表
     */
    public void dbSchemaCreate() {

        if (!this.isTablePresent()) {
            log.info("开始执行create操作");
            this.executeResource("create", "act");
            log.info("执行create完成");
        }

    }

    /**
     * 判断是否已经创建act系列表
     * @return
     */
    public boolean isTablePresent(){
        boolean flag=false;
        try {
            Connection connection = this.connection;
            DatabaseMetaData metaData = null;
            metaData = connection.getMetaData();
            /**
             * 该方法是查找数据库里是否已经有相应查询的表
             *
             * catalog：数据库名，mysql版本驱动可填null
             * schemaPatter:数据库访问用户
             * tableNamePatter:数据库表名，可填具体表名
             * new String[]{"TABLE"}代表数据库表
             */
            ResultSet tableRet = metaData.getTables(null, "%", "%",
                    new String[]{"TABLE"});
            //判断数据库中的表是否已经创建，若已创建则无法再进行创建
            while (tableRet.next()) {
                String tableName = (String) tableRet.getObject("TABLE_NAME");
                if(tableName.contains("ACT")||tableName.contains("act")){
                    flag=true;
                }
            }
            return  flag;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return flag;
    }


    public void executeResource(String operation, String component) {
        this.executeSchemaResource(operation, component, this.getDbResource(operation, operation, component), false);
    }

    /**
     * 获取sql脚本所在路径
     * @param directory
     * @param operation
     * @param component
     * @return
     */
    public String getDbResource(String directory, String operation, String component) {
        return "static/db/" + directory + "/mysql." + operation + "." + component + ".sql";
    }

    public void executeSchemaResource(String operation, String component, String resourceName, boolean isOptional) {
        InputStream inputStream = null;

        try {
            //读取sql脚本数据
            inputStream = IoUtil.getResourceAsStream(resourceName);
            if (inputStream == null) {
                if (!isOptional) {
                    log.error("resource '" + resourceName + "' is not available");
                    return;
                }

            } else {
                this.executeSchemaResource(operation, component, resourceName, inputStream);
            }
        } finally {
            IoUtil.closeSilently(inputStream);
        }

    }


    /**
     * 执行sql脚本
     * @param operation
     * @param component
     * @param resourceName
     * @param inputStream
     */
    private void executeSchemaResource(String operation, String component, String resourceName, InputStream inputStream) {
        //sql语句拼接字符串
        String sqlStatement = null;
        Object exceptionSqlStatement = null;

        try {
            /**
             * 1.jdbc连接mysql数据库
             */
            Connection connection = this.connection;

            Exception exception = null;
            /**
             * 2、分行读取"static/db/create/mysql.create.act.sql"里的sql脚本数据
             */
            byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
            /**
             * 3.将sql文件里数据分行转换成字符串，换行的地方，用转义符“\n”来代替
             */
            String ddlStatements = new String(bytes);
            /**
             * 4.以字符流形式读取字符串数据
             */
            BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
            /**
             * 5.根据字符串中的转义符“\n”分行读取
             */
            String line = IoUtil.readNextTrimmedLine(reader);
            /**
             * 6.循环读取的每一行
             */
            for(boolean inOraclePlsqlBlock = false; line != null; line = IoUtil.readNextTrimmedLine(reader)) {
                /**
                 * 7.若下一行line还有数据，证明还没有全部读取，仍可执行读取
                 */
                if (line.length() > 0) {
                    /**
                     8.在没有拼接够一个完整建表语句时，！line.endsWith(";")会为true，
                     即一直循环进行拼接，当遇到";"就跳出该if语句
                    **/
                   if ((!line.endsWith(";") || inOraclePlsqlBlock) && (!line.startsWith("/") || !inOraclePlsqlBlock)) {
                        sqlStatement = this.addSqlStatementPiece(sqlStatement, line);
                    } else {
                       /**
                        9.循环拼接中若遇到符号";"，就意味着，已经拼接形成一个完整的sql建表语句，例如
                        create table ACT_GE_PROPERTY (
                        NAME_ varchar(64),
                        VALUE_ varchar(300),
                        REV_ integer,
                        primary key (NAME_)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin
                        这样，就可以先通过代码来将该建表语句执行到数据库中，实现如下：
                        **/
                        if (inOraclePlsqlBlock) {
                            inOraclePlsqlBlock = false;
                        } else {
                            sqlStatement = this.addSqlStatementPiece(sqlStatement, line.substring(0, line.length() - 1));
                        }
                       /**
                        * 10.将建表语句字符串包装成Statement对象
                        */
                        Statement jdbcStatement = connection.createStatement();

                        try {
                            /**
                             * 11.最后，执行建表语句到数据库中
                             */
                            log.info("SQL: {}", sqlStatement);
                            jdbcStatement.execute(sqlStatement);
                            jdbcStatement.close();
                        } catch (Exception var27) {
                            log.error("problem during schema {}, statement {}", new Object[]{operation, sqlStatement, var27});
                        } finally {
                            /**
                             * 12.到这一步，意味着上一条sql建表语句已经执行结束，
                             * 若没有出现错误话，这时已经证明第一个数据库表结构已经创建完成，
                             * 可以开始拼接下一条建表语句，
                             */
                            sqlStatement = null;
                        }
                    }
                }
            }

            if (exception != null) {
                throw exception;
            } else {
                log.debug(" db schema {} for component {} successful", operation, component);
            }
        } catch (Exception var29) {
            log.error("couldn't " + operation + " db schema: " + exceptionSqlStatement, var29);
        }
    }






    protected String addSqlStatementPiece(String sqlStatement, String line) {
        return sqlStatement == null ? line : sqlStatement + " \n" + line;
    }



}
