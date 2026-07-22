package com.antigravity.debuglogger.util;

import com.antigravity.debuglogger.DebugLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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

            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("================================================================================");
                writer.println("               [调试日志 DevDebugLogger] 开发者完整运行诊断报告               ");
                writer.println("================================================================================");
                writer.println("日志报告文件名: " + reportFile.getName());
                writer.println("触发原因: " + (triggerReason != null ? triggerReason : "自动记录"));
                writer.println("记录时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                writer.println("拦截的潜在断言崩溃总次数: " + INTERCEPTED_CRASHED_ASSERTIONS.get());
                writer.println("Java 版本: " + System.getProperty("java.version"));
                writer.println("操作系统: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
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

            try (PrintWriter writer = new PrintWriter(new FileWriter(crashLogFile, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.println("[" + timestamp + "] [CRASH-HUNTER] " + detail);
            }
        } catch (Throwable ignored) {
        }
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
