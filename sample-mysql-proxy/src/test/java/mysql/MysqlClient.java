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
    public void MysqlTest() throws Exception {


        String dburl = "jdbc:mysql://localhost:8080/mydb?characterEncoding=utf8&user=root&password=";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(dburl);
        PreparedStatement ps = conn.prepareStatement("Insert into mytable(t_title,t_author,submission_date) values('mysql_proxy','saikat',now())");
        ps.execute();
        ps = conn.prepareStatement("select * from mytable where t_author='saikat'");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Assert.assertEquals("mysql_proxy", rs.getString(1));
            Assert.assertEquals("saikat", rs.getString(2));
            Assert.assertNotNull(rs.getString(3));
            System.out.println("INSERT OUTPUT - t_author : " + rs.getString(1));

        }
        ps = conn.prepareStatement("update mytable set t_author='dummy' where t_author='saikat'");
        ps.execute();
        ps = conn.prepareStatement("select * from mytable where t_author='dummy'");
        rs = ps.executeQuery();

        while (rs.next()) {
            Assert.assertEquals("mysql_proxy", rs.getString(1));
            Assert.assertEquals("dummy", rs.getString(2));
            Assert.assertNotNull(rs.getString(3));
            System.out.println("UPDATE OUTPUT - t_author : " + rs.getString(1));

        }
        rs.close();
        ps = conn.prepareStatement("delete from mytable");
        ps.execute();
        ps.close();
        conn.close();

    }
}



