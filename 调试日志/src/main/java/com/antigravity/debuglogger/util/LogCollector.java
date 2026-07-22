package com.antigravity.debuglogger.util;

import com.antigravity.debuglogger.DebugLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LogCollector {
    public static final AtomicInteger INTERCEPTED_CRASHED_ASSERTIONS = new AtomicInteger(0);

    /** 最多保留的最新诊断报告文件数量上限 */
    private static final int MAX_REPORT_FILES = 30;
    /** 拦截日志明细文件的单文件大小上限 (10MB) */
    private static final long MAX_SINGLE_LOG_SIZE = 10 * 1024 * 1024;

    private static Component createLiteralComponent(String text) {
        try {
            return Component.literal(text);
        } catch (NoSuchMethodError e) {
            try {
                java.lang.reflect.Method m = Component.class.getMethod("m_237113_", String.class);
                return (Component) m.invoke(null, text);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    public static String exportDevReport(Player player) {
        return exportDevReport(player, "手动按 F9 触发");
    }

    public static String exportDevReport(Player player, String triggerReason) {
        try {
            File logsDir = new File("logs/dev_reports");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // 容量限制清理：超出上限自动删除最早的旧日志，保持磁盘干净
            cleanOldReports(logsDir);

            // 文件名直接采用当前 年月日_时分秒 命名
            String fileNameTime = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒").format(new Date());
            File reportFile = new File(logsDir, fileNameTime + ".log");

            // 获取 JVM 内存状态
            Runtime runtime = Runtime.getRuntime();
            long maxMemMB = runtime.maxMemory() / (1024 * 1024);
            long totalMemMB = runtime.totalMemory() / (1024 * 1024);
            long freeMemMB = runtime.freeMemory() / (1024 * 1024);
            long usedMemMB = totalMemMB - freeMemMB;
            long usedPercent = maxMemMB > 0 ? (usedMemMB * 100 / maxMemMB) : 0;

            // 倒序提取 latest.log 中最近的 15 条 ERROR 级异常日志
            List<String> recentErrors = extractRecentErrorLogs();

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportFile), StandardCharsets.UTF_8))) {
                writer.println("================================================================================");
                writer.println("               [调试日志 DevDebugLogger] 开发者完整运行诊断报告               ");
                writer.println("================================================================================");
                writer.println("日志报告文件名: " + reportFile.getName());
                writer.println("触发原因: " + (triggerReason != null ? triggerReason : "自动记录"));
                writer.println("记录时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                writer.println("拦截的潜在断言崩溃总次数: " + INTERCEPTED_CRASHED_ASSERTIONS.get());
                writer.println("--------------------------------------------------------------------------------");
                writer.println("Java SDK 版本: " + System.getProperty("java.version"));
                writer.println("操作系统: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
                writer.println("内存状态: 已用 " + usedMemMB + "MB / 最大 " + maxMemMB + "MB (" + usedPercent + "% 已分配使用)");
                writer.println("================================================================================");
                writer.println("最近捕获的 ERROR/Exception 报错摘要 (最多显示最新 15 条):");
                if (recentErrors.isEmpty()) {
                    writer.println("  [良好] 未在近期控制台日志中检测到严重 ERROR 或崩溃异常。");
                } else {
                    for (String errLine : recentErrors) {
                        writer.println("  " + errLine);
                    }
                }
                writer.println("================================================================================");
                writer.println("完整实时控制台日志请参阅 logs/latest.log 与 logs/debug.log");
                writer.println("================================================================================");
            }

            String path = reportFile.getAbsolutePath();
            DebugLogger.LOGGER.info("[调试日志] 开发者诊断报告已保存: {}", path);

            if (player != null) {
                try {
                    Component msg = createLiteralComponent("§a[调试日志] §f已保存调试日志报告: §e" + reportFile.getName());
                    if (msg != null) {
                        player.sendSystemMessage(msg);
                    }
                } catch (Throwable ignored) {
                }
            }
            return path;
        } catch (Throwable t) {
            DebugLogger.LOGGER.error("[调试日志] 保存开发者诊断报告失败", t);
            return null;
        }
    }

    public static void recordInterceptedAssertion(String detail) {
        INTERCEPTED_CRASHED_ASSERTIONS.incrementAndGet();
        try {
            File logsDir = new File("logs/dev_reports");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }
            File crashLogFile = new File(logsDir, "拦截崩溃断言明细.log");

            // 超过 10MB 自动滚动轮换备份
            if (crashLogFile.exists() && crashLogFile.length() > MAX_SINGLE_LOG_SIZE) {
                File backup = new File(logsDir, "拦截崩溃断言明细_bak_" + System.currentTimeMillis() + ".log");
                crashLogFile.renameTo(backup);
                crashLogFile = new File(logsDir, "拦截崩溃断言明细.log");
            }

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(crashLogFile, true), StandardCharsets.UTF_8))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.println("[" + timestamp + "] [CRASH-HUNTER] " + detail);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 从 logs/latest.log 中提取近期 ERROR / Exception 日志
     */
    private static List<String> extractRecentErrorLogs() {
        List<String> errorLines = new ArrayList<>();
        File latestLog = new File("logs/latest.log");
        if (!latestLog.exists()) return errorLines;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(latestLog), StandardCharsets.UTF_8))) {
            String line;
            LinkedList<String> buffer = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                if (line.contains("/ERROR]") || line.contains("Exception") || line.contains("Error:")) {
                    buffer.add(line);
                    if (buffer.size() > 15) {
                        buffer.removeFirst();
                    }
                }
            }
            errorLines.addAll(buffer);
        } catch (Throwable ignored) {
        }
        return errorLines;
    }

    /**
     * 自动容量管理：保留最新的 MAX_REPORT_FILES 份文件，超出自动清理最早的旧报告
     */
    private static void cleanOldReports(File logsDir) {
        try {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".log") && !name.contains("明细"));
            if (files != null && files.length >= MAX_REPORT_FILES) {
                // 按修改时间由旧到新排序
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                int filesToDelete = files.length - MAX_REPORT_FILES + 1;
                for (int i = 0; i < filesToDelete; i++) {
                    files[i].delete();
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
