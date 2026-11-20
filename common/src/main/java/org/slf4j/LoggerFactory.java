package org.slf4j;

/**
 * Lop khoi tao LoggerFactory chuan SLF4J cung cap bo ghi nhat ky an toan.
 */
public final class LoggerFactory {

    private LoggerFactory() {
    }

    public static Logger getLogger(String name) {
        return new Logger() {
            @Override
            public void info(String msg) {
                System.out.println("[" + name + "] [INFO] " + msg);
            }

            @Override
            public void info(String format, Object arg) {
                System.out.println("[" + name + "] [INFO] " + formatMessage(format, arg));
            }

            @Override
            public void info(String format, Object arg1, Object arg2) {
                System.out.println("[" + name + "] [INFO] " + formatMessage(format, arg1, arg2));
            }

            @Override
            public void warn(String msg) {
                System.out.println("[" + name + "] [WARN] " + msg);
            }

            @Override
            public void warn(String format, Object arg) {
                System.out.println("[" + name + "] [WARN] " + formatMessage(format, arg));
            }

            @Override
            public void error(String msg) {
                System.err.println("[" + name + "] [ERROR] " + msg);
            }

            @Override
            public void error(String format, Object arg) {
                System.err.println("[" + name + "] [ERROR] " + formatMessage(format, arg));
            }

            @Override
            public void error(String msg, Throwable t) {
                System.err.println("[" + name + "] [ERROR] " + msg);
                if (t != null) {
                    t.printStackTrace();
                }
            }

            private String formatMessage(String format, Object... args) {
                if (format == null) {
                    return "";
                }
                String result = format;
                for (Object arg : args) {
                    result = result.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
                }
                return result;
            }
        };
    }
}
