package org.slf4j;

/**
 * Giao dien Logger chuan SLF4J dung cho moi truong doc lap va Minecraft runtime.
 */
public interface Logger {
    void info(String msg);
    void info(String format, Object arg);
    void info(String format, Object arg1, Object arg2);
    void warn(String msg);
    void warn(String format, Object arg);
    void error(String msg);
    void error(String format, Object arg);
    void error(String msg, Throwable t);
}
