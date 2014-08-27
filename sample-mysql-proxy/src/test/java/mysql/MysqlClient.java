/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mysql;

import junit.framework.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created with IntelliJ IDEA.
 * User: saikat
 * Date: 05/11/13
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MysqlClient {

    Connection conn = null;

    @Test
    public void mysqlTest() throws Exception {


        String dburl = "jdbc:mysql://localhost:8080/mydb?characterEncoding=utf8&user=root&password=";
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        conn = DriverManager.getConnection(dburl);
        String sql1 = "Insert into mytable(t_title,t_author,submission_date) values('mysql_proxy','saikat',now())";
        System.out.println("sql1 "+ sql1);
        PreparedStatement ps1 = conn.prepareStatement(sql1);
        ps1.execute();

        String sql2 = "select * from mytable where t_author='saikat'";
        System.out.println("sql2 "+sql2);
        PreparedStatement ps2 = conn.prepareStatement(sql2);
        ResultSet rs = ps2.executeQuery();

        if (rs.next()) {
            Assert.assertEquals("mysql_proxy", rs.getString(1));
            Assert.assertEquals("saikat", rs.getString(2));
            Assert.assertNotNull(rs.getString(3));
           // System.out.println("INSERT OUTPUT - t_author : " + rs.getString(1));

        }
        rs.close();
        System.out.println();
        String sql3 = "update mytable set t_author='dummy' where t_author='saikat'";
        System.out.println("sql3 "+sql3);

        PreparedStatement ps3 = conn.prepareStatement(sql3);
        ps3.execute();

        String sql4 = "select * from mytable where t_author='dummy'";
        System.out.println(sql4);

        PreparedStatement ps4 = conn.prepareStatement(sql4);
        ResultSet rs2 = ps4.executeQuery();

        while (rs2.next()) {
            Assert.assertEquals("mysql_proxy", rs2.getString(1));
            Assert.assertEquals("dummy", rs2.getString(2));
            Assert.assertNotNull(rs2.getString(3));
           // System.out.println("UPDATE OUTPUT - t_author : " + rs2.getString(2));

        }
        rs2.close();

        String sql5 = "delete from mytable";
        System.out.println(sql5);

        PreparedStatement ps5 = conn.prepareStatement(sql5);
        ps5.execute();
        ps5.close();
        conn.close();

    }


}



