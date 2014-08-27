package mysql;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by saikat on 27/08/14.
 */
public class MysqlParallelConnectionTest {

    public static void main(String args[]) throws Exception{

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i=0; i < 10; i++) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executorService.execute(new Worker());
        }
        executorService.shutdown();

    }

}
