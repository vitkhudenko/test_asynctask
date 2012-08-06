package com.vit.test.asynctask;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/*
 * Disclaimer: this code is created as a quick prototype.
 * Please don't use it in production, because it neither handles 
 * device configuration changes nor the Activity lifecycle changes.
 */
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
        
        final int numberOfTasks = getIntFromTextView(numberOfTasksEditText);
        final int taskDuration  = getIntFromTextView(singleTaskDurationEditText);
        
        log("number of tasks to run = " + numberOfTasks);
        log("task payload duration = " + taskDuration + " ms");
        
        final boolean useParallelExecution = enableParallelExecutionCheckbox.isChecked();
        
        log("use parallel execution = " + useParallelExecution);
        
        Toast toast = Toast.makeText(this, "Please watch the LogCat output", Toast.LENGTH_SHORT);
        toast.setGravity((Gravity.CENTER_HORIZONTAL | Gravity.TOP), toast.getXOffset(), toast.getYOffset());
        toast.show();
        
        for (int i = 0; i < numberOfTasks; i++) {
            int taskId = i + 1;
            startTask(taskId, taskDuration, useParallelExecution);
        }
    }
    
    private void startTask(int taskId, int taskDuration, boolean useParallelExecution) {
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

    private int getIntFromTextView(EditText v) {
        int result = 0;
        try {
            result = Integer.parseInt(v.getText().toString());
        } catch (NumberFormatException ignored) {}
        return result;
    }
    
    private void log(String msg) {
        Log.d("MainActivity", msg);
    }
    
    private class TestTask extends AsyncTask<Void, Void, Void> /* Params, Progress, Result */ {

        private final int id;
        private final int duration;
        
        TestTask(int id, int duration) {
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
