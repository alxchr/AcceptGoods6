package ru.abch.acceptgoods6;

import com.bosphere.filelogger.FL;

import ru.abch.acceptgoods6.App;

/**
 * Created by CherepanovAB on 25.12.2017.
 */

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;
    private App app = null;

    public TopExceptionHandler(App app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (StackTraceElement traceElement : arr) {
            report += "    " + traceElement.toString() + "\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (StackTraceElement stackTraceElement : arr) {
                report += "    " + stackTraceElement.toString() + "\n";
            }
        }
        report += "-------------------------------\n";

        FL.d(report);

        defaultUEH.uncaughtException(t, e);
    }}
