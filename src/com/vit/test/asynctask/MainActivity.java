package com.vit.test.asynctask;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity {

    // the number of tasks to be run in a batch
    private EditText numberOfTasksEditText;
    
    // the duration of payload job for a single task 
    private EditText singleTaskDurationEditText;
    
    // switches between 2 different modes (with/without parallel execution)
    private CheckBox enableParallelExecutionCheckbox;
    
    // each task increments this count as soon as it enters its doInBackground()
    private AtomicInteger executedTasksCount;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        numberOfTasksEditText           = (EditText) findViewById(R.id.tasks_number);
        singleTaskDurationEditText      = (EditText) findViewById(R.id.task_payload_duration);
        enableParallelExecutionCheckbox = (CheckBox) findViewById(R.id.enable_parallel_execution);
    }

    public void onStartTestClicked(View v) {
        executedTasksCount = new AtomicInteger(0); // reset this count
        
        final long numberOfTasks = getIntFromTextView(numberOfTasksEditText);
        final long taskDuration  = getIntFromTextView(singleTaskDurationEditText);
        
        log("number of tasks to run = " + numberOfTasks);
        log("task payload duration = " + taskDuration + " ms");
        
        final boolean useParallelExecution = enableParallelExecutionCheckbox.isChecked();
        
        log("use parallel execution = " + useParallelExecution);
        
        for (int i = 0; i < numberOfTasks; i++) {
            
            int taskId = i + 1;
            
            TestTask task = new TestTask(taskId, taskDuration);
            
            if (useParallelExecution) {
                // this type of executor uses the following params:
                //
                // private static final int CORE_POOL_SIZE = 5;
                // private static final int MAXIMUM_POOL_SIZE = 128;
                // private static final int KEEP_ALIVE = 1;
                //
                // private static final ThreadFactory sThreadFactory = new ThreadFactory() {
                //     private final AtomicInteger mCount = new AtomicInteger(1);
                //
                //     public Thread newThread(Runnable r) {
                //         return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
                //     }
                // };
                //
                // private static final BlockingQueue<Runnable> sPoolWorkQueue =
                //        new LinkedBlockingQueue<Runnable>(10);
                
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                
            } else {
                // this is the same as calling t.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                task.execute();
            }
        }
    }

    private long getIntFromTextView(EditText v) {
        long result = 0;
        try {
            result = Long.parseLong(v.getText().toString());
        } catch (NumberFormatException ignored) {}
        return result;
    }
    
    private void log(String msg) {
        Log.d("MainActivity", msg);
    }
    
    private class TestTask extends AsyncTask<Void, Void, Void> /* Params, Progress, Result */ {

        private final long id;
        private final long duration;
        
        TestTask(long id, long duration) {
            this.id       = id;
            this.duration = duration;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            int taskExecutionNumber = executedTasksCount.incrementAndGet();
            log("doInBackground: entered, taskExecutionNumber = " + taskExecutionNumber);
            SystemClock.sleep(duration); // emulates some job
            log("doInBackground: is about to finish, taskExecutionNumber = " + taskExecutionNumber);
            return null;
        }
        
        private void log(String msg) {
            Log.d("TestTask #" + id, msg);
        }
    }
}
