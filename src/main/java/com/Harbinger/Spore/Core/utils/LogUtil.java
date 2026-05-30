package com.Harbinger.Spore.Core.utils;


import java.io.File;
import java.io.PrintStream;

/**
 * @author karywoodOyo
 */
public class LogUtil {
    static final File logFile = new File("spore.log");
    static final File errorLogFile = new File("spore_error.log");
    public static PrintStream out;
    public static PrintStream err;
    // 3. 静态代码块：注册JVM关闭钩子，确保程序退出时自动调用close()
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(LogUtil::close, "LogUtil-ShutdownHook"));
    }

    // 4. 初始化流的私有工具方法（避免重复代码）
    private static PrintStream initOutStream() {
        if (out == null) {
            out = new Log4j2PrintStream(logFile, Log4j2PrintStream.LogLevel.INFO);
        }
        return out;
    }

    private static PrintStream initErrStream() {
        if (err == null) {
            err = new Log4j2PrintStream(errorLogFile, Log4j2PrintStream.LogLevel.ERROR);
        }
        return err;
    }

    // 公开的日志方法
    public static void log(String message) {
        initOutStream().println(message);
    }

    public static void logf(String format, Object... args) {
        PrintStream stream = initOutStream();
        stream.printf(format, args);
        stream.println();
    }

    public static void error(String message) {
        initErrStream().println(message);
    }

    public static void errorf(String format, Object... args) {
        PrintStream stream = initErrStream();
        stream.printf(format, args);
        stream.println();
    }

    public static void flush() {
        if (out != null) {
            out.flush();
        }
        if (err != null) {
            err.flush();
        }
    }

    // 优化close()：增加判空和异常处理，避免空指针/关闭失败导致程序崩溃
    public static void close() {
        // 关闭普通日志流
        if (out != null) {
            try {
                out.flush(); // 关闭前先刷新缓冲区
                out.close();
            } catch (Exception e) {
                // 记录关闭失败的日志，但不抛出异常（避免影响程序退出）
                System.err.println("关闭日志输出流失败：" + e.getMessage());
            } finally {
                out = null; // 置空，避免重复关闭
            }
        }

        // 关闭错误日志流
        if (err != null) {
            try {
                err.flush();
                err.close();
            } catch (Exception e) {
                System.err.println("关闭错误日志输出流失败：" + e.getMessage());
            } finally {
                err = null;
            }
        }
    }

    public static void printStackTrace(Throwable t) {
        t.printStackTrace(initErrStream());
    }

    // 私有化构造方法，避免实例化
    private LogUtil() {}
}
