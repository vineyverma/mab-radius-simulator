//~--- non-JDK imports --------------------------------------------------------

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------

//Extends the FutureTask class parameterized with the String class.
public class ResultTask extends FutureTask<String> {
    private static final Logger logger = Logger.getLogger("ResultTask");

    // It will store the name of the task.
    private String name;

    // Implement the constructor of the class.
    // It has to receive a Callable object as a parameter.
    public ResultTask(Callable<String> callable) {
        super(callable);
        this.name = ((ExecutableTask) callable).getName();
    }

    @Override
    protected void done() {
        if (this.isCancelled()) {
            logger.info(this.name + ": has been canceled");
        } else if (this.isDone()) {
            logger.info(this.name + ": has finished");
        }
    }
}
