package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: saikat
 * Date: 05/11/13
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MysqlClient  {

    Connection conn = null;

    @Test
    public void MysqlTest() throws Exception{


        String dburl = "jdbc:mysql://localhost:8080/mydb?characterEncoding=utf8&user=root&password=";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(dburl);
        PreparedStatement ps = conn.prepareStatement("Insert into mytable(t_title,t_author,submission_date) values('mysql_proxy','saikat',now())");
        ps.execute();
        ps = conn.prepareStatement("select * from mytable where t_author='saikat'");
        ResultSet rs = ps.executeQuery();

                if(rs.next()){
                    Assert.assertEquals("mysql_proxy",rs.getString(2));
                    Assert.assertEquals("saikat",rs.getString(3));
                    Assert.assertNotNull(rs.getString(4));
                    System.out.println("INSERT OUTPUT - t_author : "+rs.getString(3));

                }

                ps = conn.prepareStatement("update mytable set t_author='dummy' where t_author='saikat'");
                ps.execute();

                ps = conn.prepareStatement("select * from mytable where t_author='dummy'");
                rs = ps.executeQuery();

                while(rs.next()){
                        Assert.assertEquals("mysql_proxy",rs.getString(2));
                        Assert.assertEquals("dummy",rs.getString(3));
                        Assert.assertNotNull(rs.getString(4));
                        System.out.println("UPDATE OUTPUT - t_author : "+rs.getString(3));

                }
                rs.close();
                ps = conn.prepareStatement("delete from mytable");
                ps.execute();


        ps.close();


                conn.close();

        }
    }



