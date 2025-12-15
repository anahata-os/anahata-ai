
package uno.anahata.ai.swing.internal;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwingTask<T> extends SwingWorker<T, Void> {
    private final String taskName;
    private final Callable<T> backgroundTask;
    private final Consumer<T> onDone;
    private final Consumer<Exception> onError;
    private final boolean showError;

    public SwingTask(String taskName, Callable<T> backgroundTask, Consumer<T> onDone, Consumer<Exception> onError, boolean showError) {
        this.taskName = taskName;
        this.backgroundTask = backgroundTask;
        this.onDone = onDone;
        this.onError = onError;
        this.showError = showError;
    }

    public SwingTask(String taskName, Callable<T> backgroundTask, Consumer<T> onDone, Consumer<Exception> onError) {
        this(taskName, backgroundTask, onDone, onError, true);
    }

    public SwingTask(String taskName, Callable<T> backgroundTask, Consumer<T> onDone) {
        this(taskName, backgroundTask, onDone, null, true);
    }

    public SwingTask(String taskName, Callable<T> backgroundTask) {
        this(taskName, backgroundTask, null, null, true);
    }

    @Override
    protected T doInBackground() throws Exception {
        return backgroundTask.call();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            if (onDone != null) {
                onDone.accept(result);
            }
        } catch (Exception e) {
            log.error("Error in background task", e);
            if (showError) {
                SwingUtils.showException(taskName, "An error occurred during a background task.", e);
            }
            if (onError != null) {
                onError.accept(e);
            }
        }
    }
}
