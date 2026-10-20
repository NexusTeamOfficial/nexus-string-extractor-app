package com.nexusteam.framework.xml.string.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class Logger {
    private static final java.util.logging.Logger JUL = java.util.logging.Logger.getLogger("NexusStringExtractor");
    static {
        JUL.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter() {
            @Override public synchronized String format(LogRecord record) {
                return String.format("[%s] %s%n", record.getLevel().getName(), record.getMessage());
            }
        });
        JUL.addHandler(handler);
        JUL.setLevel(Level.INFO);
    }
    private Logger() {}
    public static void setVerbose(boolean verbose) {
        Level level = verbose ? Level.FINE : Level.INFO;
        JUL.setLevel(level);
        for (java.util.logging.Handler h : JUL.getHandlers()) h.setLevel(level);
    }
    public static void info(String msg) { JUL.info(msg); }
    public static void fine(String msg) { JUL.fine(msg); }
    public static void warning(String msg) { JUL.warning(msg); }
    public static void severe(String msg) { JUL.severe(msg); }
    public static void severe(String msg, Throwable t) { JUL.log(Level.SEVERE, msg, t); }
}
