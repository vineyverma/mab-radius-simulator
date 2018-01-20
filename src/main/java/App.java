//~--- non-JDK imports --------------------------------------------------------

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------

public class App {

    private static final Logger logger = Logger.getLogger("App");

    public static void main(String[] args) {

        long start = System.nanoTime();
        int numRequests = 1000;
        // Create ExecutorService using the newCachedThreadPool() method
        // of the Executors class.
        ExecutorService executorService = (ExecutorService) Executors.newCachedThreadPool();

        // Create an array to store five ResultTask objects.
        ResultTask[] resultTasks = new ResultTask[numRequests];

        // Send the each ResultTask to the executor ResultTask
        // using the submit() method.
        for (int i = 0; i < numRequests; i++) {
            ExecutableTask executableTask = new ExecutableTask("Task" + i);
            resultTasks[i] = new ResultTask(executableTask);
            executorService.submit(resultTasks[i]);
        }

        // Put the main thread to sleep for 5 seconds.
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, ie.getLocalizedMessage());
            ie.printStackTrace(System.err);
        }

        // Cancel all the tasks you have sent to the executor.
        /*for (int i = 0; i < resultTasks.length; i++) {
            resultTasks[i].cancel(true);
        }
        // Write to the console the result of those tasks that haven't been
        // canceled using the get() method of the ResultTask objects.
        for (int i = 0; i < resultTasks.length; i++) {
            try {
                if (resultTasks[i].isCancelled()) {
                    logger.info("Task" + i + " was cancelled.");
                } else if (resultTasks[i].isDone()) {
                    logger.info(resultTasks[i].get());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage());
                e.printStackTrace(System.err);
            }
        }*/

        // Finish the executor using the shutdown() method.
        executorService.shutdown();

        long end = System.nanoTime();
        long diff = end - start;
        System.out.println ("Elapsed Time: " + diff);

        double timeInSecondsProfiler = diff / 1E9;
        System.out.println ("\n\n\n\t\tExecution_Time : " + diff +
            ":nano seconds " + timeInSecondsProfiler + " :seconds");


    }
}
