package mysql;

import org.junit.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by saikat on 27/08/14.
 */


    public class Worker implements Runnable{

        public void run(){

            try {

                String dburl = "jdbc:mysql://localhost:8080/mydb?characterEncoding=utf8&user=root&password=";
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                Connection conn = DriverManager.getConnection(dburl);

                PreparedStatement ps = conn.prepareStatement("Insert into mytable(t_title,t_author,submission_date) values('mysql_proxy','saikat',now())");
                ps.execute();
                ps = conn.prepareStatement("select * from mytable where t_author='saikat'");
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Assert.assertEquals("mysql_proxy", rs.getString(1));
                    Assert.assertEquals("saikat", rs.getString(2));
                    Assert.assertNotNull(rs.getString(3));
                    System.out.println("INSERT OUTPUT - t_author : " + rs.getString(2));

                }
                conn.close();

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }
