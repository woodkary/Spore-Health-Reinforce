package com.Harbinger.Spore.Core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 隔离版Log4j2PrintStream：完全隔离Logger，避免日志串流
 * 核心：专属Logger + 关闭Additivity + 独立配置上下文
 */
public class Log4j2PrintStream extends PrintStream {
    // 默认字符集
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 默认日志格式
    private static final String DEFAULT_LOG_PATTERN = "%msg%n"; // 仅输出内容（兼容PrintStream原生格式）
    // 专属Logger（全局唯一，避免复用）
    private final Logger exclusiveLogger;
    // 日志级别
    private final LogLevel logLevel;
    // 行缓冲区
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    // 自动刷新标记
    private final AtomicBoolean autoFlush;
    // 字符集
    private final Charset charset;
    // 关联的文件
    private final File associatedFile;
    // 专属Logger名称（全局唯一）
    private final String exclusiveLoggerName;
    // 是否追加模式（false=启动时清空文件）
    private final boolean append;
    // 标记是否已初始化文件（避免重复清空）
    private volatile boolean fileInited = false;

    // 日志级别枚举
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    // ======================== 核心构造函数（修复日志隔离问题） ========================
    /**
     * 基于File对象构造（完全隔离版）
     * @param file 输出文件
     * @param logLevel 日志级别
     * @param autoFlush 是否自动刷新
     * @param charset 字符集
     */
    public Log4j2PrintStream(File file, LogLevel logLevel, boolean autoFlush, Charset charset, boolean append) {
        super(new OutputStream() {
            @Override
            public void write(int b) {}
        }, autoFlush, charset);
        this.associatedFile = file;
        this.logLevel = logLevel == null ? LogLevel.INFO : logLevel;
        this.autoFlush = new AtomicBoolean(autoFlush);
        this.charset = charset;

        // 关键1：生成全局唯一的Logger名称（杜绝复用）
        this.exclusiveLoggerName = "IsolatedPrintStream-" + file.getAbsolutePath().replace(File.separator, "_") + "-" + UUID.randomUUID().toString().replace("-", "");
        // 关键2：初始化完全隔离的Logger（无继承、无共享）
        this.exclusiveLogger = initIsolatedLogger();
        this.append=append;
    }

    /**
     * 基于文件名构造（隔离版）
     */
    public Log4j2PrintStream(String fileName, LogLevel logLevel, boolean autoFlush, Charset charset, boolean append) {
        this(new File(fileName), logLevel, autoFlush, charset,append);
    }

    /**
     * 基于FileOutputStream构造（隔离版）
     */
    public Log4j2PrintStream(FileOutputStream fos, LogLevel logLevel, boolean autoFlush, Charset charset, boolean append) {
        super(new OutputStream() {
            @Override
            public void write(int b) {}
        }, autoFlush, charset);
        this.associatedFile = getFileFromFos(fos);
        this.logLevel = logLevel == null ? LogLevel.INFO : logLevel;
        this.autoFlush = new AtomicBoolean(autoFlush);
        this.charset = charset;

        // 全局唯一Logger名称
        this.exclusiveLoggerName = "IsolatedPrintStream-FOS-" + UUID.randomUUID().toString().replace("-", "");
        this.exclusiveLogger = initIsolatedLogger();
        this.append=append;
    }

    /**
     * 简化构造（默认参数）
     */
    public Log4j2PrintStream(String fileName, boolean append) {
        this(fileName, LogLevel.INFO, true, DEFAULT_CHARSET,append);
    }

    public Log4j2PrintStream(File file, boolean append) {
        this(file, LogLevel.INFO, true, DEFAULT_CHARSET,append);
    }
    public Log4j2PrintStream(File file) {
        this(file, LogLevel.INFO, true, DEFAULT_CHARSET,false);
    }
    public Log4j2PrintStream(File file, LogLevel logLevel, boolean append) {
        this(file, logLevel, true, DEFAULT_CHARSET,append);
    }
    public Log4j2PrintStream(File file, LogLevel logLevel) {
        this(file, logLevel, true, DEFAULT_CHARSET,false);
    }

    public Log4j2PrintStream(FileOutputStream fos, boolean append) {
        this(fos, LogLevel.INFO, true, DEFAULT_CHARSET,append);
    }

    // ======================== 核心修复：初始化完全隔离的Logger ========================
    /**
     * 初始化隔离Logger：
     * 1. 专属File Appender（仅关联当前文件）
     * 2. 关闭Additivity（不转发到父Logger/root）
     * 3. 仅绑定当前Appender，不继承全局配置
     */
    private Logger initIsolatedLogger() {
        try {
            // 关键1：首次初始化时清空文件（仅当append=false时）
            if (!append && !fileInited) {
                synchronized (associatedFile) {
                    if (!fileInited) { // 双重检查锁，避免多线程重复清空
                        clearFile(associatedFile);
                        fileInited = true;
                    }
                }
            }
            // 1. 获取Log4j2上下文（非全局上下文，避免污染）
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();

            // 2. 创建专属File Appender（仅输出到当前文件）
            PatternLayout layout = PatternLayout.newBuilder()
                    .withPattern(DEFAULT_LOG_PATTERN) // 仅输出日志内容，兼容PrintStream原生格式
                    .withConfiguration(config)
                    .build();

            FileAppender exclusiveAppender = FileAppender.newBuilder()
                    .setName("ExclusiveAppender-" + exclusiveLoggerName) // 专属Appender名称
                    .withFileName(associatedFile.getAbsolutePath())
                    .withAppend(true) // 兼容追加模式
                    .setLayout(layout)
                    .setConfiguration(config)
                    .setBufferedIo(true) // 开启缓冲
                    .setBufferSize(8192) // 8KB缓冲区
                    .setImmediateFlush(autoFlush.get()) // 对齐autoFlush配置
                    .build();
            exclusiveAppender.start();
            config.addAppender(exclusiveAppender);

            // 3. 创建专属LoggerConfig（核心：关闭Additivity）
            LoggerConfig loggerConfig = new LoggerConfig(
                    exclusiveLoggerName,
                    org.apache.logging.log4j.Level.toLevel(logLevel.name()),
                    false // 关键3：additivity=false，不转发到父Logger/root
            );
            // 仅绑定专属Appender，不继承任何全局Appender
            loggerConfig.addAppender(exclusiveAppender, null, null);
            // 将专属LoggerConfig注册到上下文（覆盖默认继承逻辑）
            config.addLogger(exclusiveLoggerName, loggerConfig);

            // 4. 刷新配置，使隔离Logger生效
            context.updateLoggers(config);

            // 5. 获取专属Logger（仅关联当前Appender，无继承）
            return LogManager.getLogger(exclusiveLoggerName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to init isolated Logger for file: " + associatedFile.getAbsolutePath(), e);
        }
    }
    private void clearFile(File file) throws IOException {
        // 1. 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create parent directory: " + parentDir.getAbsolutePath());
            }
        }

        // 2. 清空文件（原子操作：覆盖空内容）
        if (file.exists()) {
            // 使用Files.write实现原子清空，避免文件句柄泄漏
            Files.write(file.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } else {
            // 文件不存在时创建空文件
            Files.createFile(file.toPath());
        }
    }

    // ======================== 工具方法（兼容逻辑） ========================
    private File getFileFromFos(FileOutputStream fos) {
        try {
            java.lang.reflect.Field fdField = FileOutputStream.class.getDeclaredField("fd");
            fdField.setAccessible(true);
            FileDescriptor fd = (FileDescriptor) fdField.get(fos);
            return new File(fos.toString().replaceAll(".*@", "") + ".log");
        } catch (Exception e) {
            return new File("isolated-temp-" + UUID.randomUUID() + ".log");
        }
    }

    // ======================== 核心输出方法（仅输出到专属Logger） ========================
    @Override
    public void write(int b) {
        lineBuffer.write(b);
        if (autoFlush.get() && b == '\n') {
            flushLine();
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (buf == null) throw new NullPointerException();
        if (off < 0 || len < 0 || off + len > buf.length) throw new IndexOutOfBoundsException();
        if (len == 0) return;

        lineBuffer.write(buf, off, len);
        if (autoFlush.get()) {
            for (int i = off; i < off + len; i++) {
                if (buf[i] == '\n') {
                    flushLine();
                    break;
                }
            }
        }
    }

    @Override
    public void flush() {
        flushLine();
    }

    @Override
    public void close() {
        flushLine();
        // 额外清理：移除专属Logger和Appender，避免内存泄漏
        cleanIsolatedLogger();
        super.close();
    }

    // ======================== 隔离日志核心逻辑 ========================
    /**
     * 刷出行缓冲区：仅输出到专属Logger，无任何转发
     */
    private void flushLine() {
        synchronized (lineBuffer) {
            if (lineBuffer.size() == 0) return;
            String content = lineBuffer.toString(charset);
            lineBuffer.reset();
            // 仅输出到专属Logger，无继承、无转发
            logToExclusiveLogger(content);
        }
    }

    /**
     * 仅输出到专属Logger，避免串流
     */
    private void logToExclusiveLogger(String content) {
        String logContent = content.replaceAll("\\n$", "");
        if (logContent.isEmpty()) return;

        // 仅使用专属Logger输出，不涉及任何全局Logger
        switch (logLevel) {
            case TRACE:
                exclusiveLogger.trace(logContent);
                break;
            case DEBUG:
                exclusiveLogger.debug(logContent);
                break;
            case INFO:
                exclusiveLogger.info(logContent);
                break;
            case WARN:
                exclusiveLogger.warn(logContent);
                break;
            case ERROR:
                exclusiveLogger.error(logContent);
                break;
        }
    }

    /**
     * 清理专属Logger：避免内存泄漏，彻底隔离
     */
    private void cleanIsolatedLogger() {
        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
            // 移除专属LoggerConfig
            config.removeLogger(exclusiveLoggerName);
            // 移除专属Appender
            config.getAppenders().values().stream()
                    .filter(appender -> appender.getName().contains(exclusiveLoggerName))
                    .forEach(LifeCycle::stop);
            context.updateLoggers(config);
        } catch (Exception e) {
            exclusiveLogger.error("Clean isolated logger failed", e);
        }
    }

    // ======================== PrintStream特有方法（重写兼容） ========================
    @Override
    public void print(boolean b) {
        print(String.valueOf(b));
    }

    @Override
    public void print(char c) {
        print(String.valueOf(c));
    }

    @Override
    public void print(int i) {
        print(String.valueOf(i));
    }

    @Override
    public void print(long l) {
        print(String.valueOf(l));
    }

    @Override
    public void print(float f) {
        print(String.valueOf(f));
    }

    @Override
    public void print(double d) {
        print(String.valueOf(d));
    }

    @Override
    public void print(char[] s) {
        print(s == null ? "null" : new String(s));
    }

    @Override
    public void print(String s) {
        String content = s == null ? "null" : s;
        try {
            lineBuffer.write(content.getBytes(charset));
        } catch (IOException e) {
            exclusiveLogger.error("Print string failed", e);
        }
    }

    @Override
    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    @Override
    public void println() {
        println("");
    }

    @Override
    public void println(boolean x) {
        print(x);
        println();
    }

    @Override
    public void println(char x) {
        print(x);
        println();
    }

    @Override
    public void println(int x) {
        print(x);
        println();
    }

    @Override
    public void println(long x) {
        print(x);
        println();
    }

    @Override
    public void println(float x) {
        print(x);
        println();
    }

    @Override
    public void println(double x) {
        print(x);
        println();
    }

    @Override
    public void println(char[] x) {
        print(x);
        println();
    }

    @Override
    public void println(String x) {
        print(x);
        lineBuffer.write('\n');
        flushLine();
    }

    @Override
    public void println(Object x) {
        println(String.valueOf(x));
    }

    // ======================== 辅助方法 ========================
    @Override
    public boolean checkError() {
        if (associatedFile != null && !associatedFile.canWrite()) {
            return true;
        }
        return ((org.apache.logging.log4j.core.Logger) exclusiveLogger).getAppenders().values().stream()
                .anyMatch(appender -> !appender.isStarted());
    }

    public File getAssociatedFile() {
        return associatedFile;
    }
}