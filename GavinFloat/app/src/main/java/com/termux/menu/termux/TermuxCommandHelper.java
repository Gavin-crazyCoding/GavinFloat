package com.termux.menu.termux;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;

/**
 * Termux 命令助手
 * 增强版：命令发送到前台可见会话，用户可看到执行过程
 */
public class TermuxCommandHelper {
    private static final String TAG = "TermuxCmdHelper";

    private static final String TERMUX_PKG = "com.termux";
    private static final String TERMUX_SERVICE_CLASS = "com.termux.app.TermuxService";

    private static final String ACTION_SERVICE_EXECUTE = "com.termux.service_execute";
    private static final String EXTRA_ARGUMENTS = "com.termux.execute.arguments";
    private static final String EXTRA_WORKDIR = "com.termux.execute.cwd";
    private static final String EXTRA_BACKGROUND = "com.termux.execute.background";
    private static final String EXTRA_RUNNER = "com.termux.execute.runner";
    private static final String EXTRA_SESSION_ACTION = "com.termux.execute.session_action";

    private static final String BASH_PATH = "/data/data/com.termux/files/usr/bin/bash";
    private static final String HOME_DIR = "/data/data/com.termux/files/home";
    private static final String RUNNER_TERMINAL_SESSION = "terminal-session";

    // Session actions: 0=keep, 1=switch, 2=close, 3=no-open
    private static final int SESSION_ACTION_SWITCH = 1;
    private static final int SESSION_ACTION_CLOSE = 2;
    private static final int SESSION_ACTION_NO_OPEN = 3;

    private static TermuxCommandHelper sInstance;
    private Context mContext;

    public static TermuxCommandHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TermuxCommandHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private TermuxCommandHelper(Context context) {
        mContext = context;
    }

    public void sendCommandToTerminal(String command) {
        if (command == null || command.isEmpty()) return;
        try {
            String trimmed = command.endsWith("\n") ? command.substring(0, command.length() - 1) : command;
            executeInForeground(trimmed);
        } catch (Exception e) {
            android.util.Log.e("GavinFloat", "sendCommand failed: " + e.getMessage());
        }
    }

    public void executeInForeground(String command) {
        if (command == null || command.isEmpty()) return;
        try {
            File scriptFile = new File(HOME_DIR + "/.termux/.gavinfloat_cmd.sh");
            File parentDir = scriptFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
            FileWriter writer = new FileWriter(scriptFile);
            writer.write("#!/data/data/com.termux/files/usr/bin/bash\n");
            writer.write("echo '━━━ GavinFloat ━━━'\n");
            writer.write(command + "\n");
            writer.write("echo ''\n");
            writer.write("echo '━━━ 命令执行完毕 ━━━'\n");
            writer.write("echo '按回车键关闭窗口...'\n");
            writer.write("read\n");
            writer.close();
            try { Runtime.getRuntime().exec("chmod 700 " + scriptFile.getAbsolutePath()); } catch (Exception ignored) {}
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName("com.termux", "com.termux.app.TermuxService"));
            intent.setAction(ACTION_SERVICE_EXECUTE);
            intent.setData(Uri.parse("com.termux.file" + ":" + scriptFile.getAbsolutePath()));
            intent.putExtra(EXTRA_WORKDIR, HOME_DIR);
            intent.putExtra(EXTRA_RUNNER, RUNNER_TERMINAL_SESSION);
            intent.putExtra(EXTRA_SESSION_ACTION, String.valueOf(SESSION_ACTION_SWITCH));
            intent.putExtra(EXTRA_BACKGROUND, false);
            mContext.startService(intent);
        } catch (Exception e) {
            android.util.Log.e("GavinFloat", "foreground exec failed: " + e.getMessage());
        }
    }

    public void executeInBackground(String command) {
        if (command == null || command.isEmpty()) return;
        try {
            Intent intent = buildServiceIntent();
            intent.setAction(ACTION_SERVICE_EXECUTE);
            intent.setData(Uri.parse("com.termux.file" + ":" + BASH_PATH));
            intent.putExtra(EXTRA_ARGUMENTS, new String[]{"-c", command});
            intent.putExtra(EXTRA_WORKDIR, HOME_DIR);
            intent.putExtra(EXTRA_BACKGROUND, true);
            intent.putExtra(EXTRA_RUNNER, "app-shell");
            intent.putExtra(EXTRA_SESSION_ACTION, String.valueOf(SESSION_ACTION_NO_OPEN));
            mContext.startService(intent);
        } catch (Exception e) {
            android.util.Log.e("GavinFloat", "bg exec failed: " + e.getMessage());
        }
    }

    public void executeAndCapture(String command, OutputCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                String result;
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{BASH_PATH, "-c", command});
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                    java.io.BufferedReader errReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                    while ((line = errReader.readLine()) != null) sb.append(line).append("\n");
                    process.waitFor();
                    result = sb.toString();
                    if (result.isEmpty()) result = "(no output)";
                } catch (Exception e) {
                    result = "Error: " + e.getMessage();
                }
                if (callback != null) {
                    final String finalResult = result;
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(
                        new Runnable() { public void run() { callback.onOutput(finalResult); }});
                }
            }
        }).start();
    }

    public interface OutputCallback {
        void onOutput(String output);
    }

    public boolean isTermuxInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(TERMUX_PKG, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Intent buildServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(TERMUX_PKG, TERMUX_SERVICE_CLASS));
        return intent;
    }
}
