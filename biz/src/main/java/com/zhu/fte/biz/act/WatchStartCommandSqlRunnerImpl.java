package com.zhu.fte.biz.act;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * TODO
 *
 * @author zhujiqian
 * @date 2021/3/13 17:09
 */
@Slf4j
@Component
public class WatchStartCommandSqlRunnerImpl implements CommandLineRunner {



    private static final String DRIVER="com.mysql.jdbc.Driver";


    private static final String PRE_URL="jdbc:mysql://127.0.0.1:3306/";

    //老版本的mysql-connector-java可用以下模式：
    //private static final String UNICODE="?serverTimezone=UTC";

   //新版mysql-connector-java8.0版本请用以下模式，否则会驱动加载有问题
    private static final String UNICODE="?useUnicode=true&characterEncoding=UTF-8&ueSSL=false&serverTimezone=GMT%2B8";

    private static final String DEFAULT_BASE="mysql";

    private static final String NEW_BASE="fte";

    private static final String USER_NAME="root";


    private static final String PASSWORD="root924931408";
    @Override
    public void run(String... args) throws Exception {
        Class.forName(DRIVER);
        String url=PRE_URL+DEFAULT_BASE+UNICODE;
        Connection conn= DriverManager.getConnection(url,USER_NAME,PASSWORD);
        Statement stat=conn.createStatement();
        ResultSet resultSet=stat.executeQuery("show databases like \"fte\"");
        if(resultSet.next()){
            log.info("数据库已经存在,开始创建数表");
            createTable(conn,stat);
        }else {
            log.info("数据库未存在");
            if(stat.executeUpdate("create database fte")==1){
                log.info("新建数据库成功，开始创建表");
                createTable(conn,stat);
            }
        }
    }

    public void createTable(Connection conn,Statement stat) throws SQLException {
        try {
            String url=PRE_URL+NEW_BASE+UNICODE;
            conn=DriverManager.getConnection(url,USER_NAME,PASSWORD);
            SqlSessionFactory sqlSessionFactory=new SqlSessionFactory(conn);
            sqlSessionFactory.schemaOperationsBuild("create");
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            stat.close();
            conn.close();
        }
    }

}
