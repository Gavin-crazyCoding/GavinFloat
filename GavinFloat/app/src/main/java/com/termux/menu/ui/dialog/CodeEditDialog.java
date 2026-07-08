package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditDialog extends Dialog {

    private static final Pattern[] HIGHLIGHT_PATTERNS;
    private static final int[] HIGHLIGHT_COLORS;
    static {
        HIGHLIGHT_PATTERNS = new Pattern[]{
            Pattern.compile("\\b(public|class|static|void|int|String|boolean|float|double|if|else|for|while|return|new|try|catch|throw|import|package|def|print|lambda|None|True|False|and|or|not|in|is|function|var|let|const|console|require|module|exports|echo|exit|source|alias|export|local|read|set|grep|awk|sed|def|end|begin|rescue|ensure|module)\\b"),
            Pattern.compile("\"[^\"]*\"|'[^']*'"),
            Pattern.compile("//[^\n]*|#[^\n]*"),
            Pattern.compile("\\b\\d+\\.?\\d*\\b"),
            Pattern.compile("@\\w+"),
        };
        HIGHLIGHT_COLORS = new int[]{0xFF569CD6, 0xFF6A9955, 0xFF808080, 0xFFCE9178, 0xFFC586C0};
    }

    private Context mCtx;
    private EditText mEditor;
    private TextView mTitleText;
    private String mFilePath;
    private LinearLayout mFindLayout;
    private EditText mFindInput;
    private int mLastFindPos = 0;
    private boolean mDirty = false;

    public CodeEditDialog(Context context, String filePath) {
        super(context, com.termux.menu.R.style.Theme_GavinFloat_Dialog);
        mCtx = context;
        mFilePath = filePath;
        saveRecentFile(filePath);
        try { init(); } catch (Exception e) {
            Toast.makeText(mCtx, "编辑器启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveRecentFile(String path) {
        try {
            android.content.SharedPreferences p = mCtx.getSharedPreferences("gavinfloat_editor", Context.MODE_PRIVATE);
            java.util.HashSet<String> recent = new java.util.HashSet<>(p.getStringSet("recent_files", new java.util.HashSet<String>()));
            recent.add(path);
            if (recent.size() > 20) {
                java.util.ArrayList<String> list = new java.util.ArrayList<>(recent);
                recent.clear();
                for (int i = list.size() - 20; i < list.size(); i++) recent.add(list.get(i));
            }
            p.edit().putStringSet("recent_files", recent).apply();
        } catch (Exception ignored) {}
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);

        LinearLayout root = new LinearLayout(mCtx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1E1E1E);

        // 标题栏 40dp 高
        LinearLayout header = new LinearLayout(mCtx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF2D2D30);
        header.setPadding(dp(8), dp(4), dp(8), dp(4));
        header.setMinimumHeight(dp(36));

        mTitleText = new TextView(mCtx);
        mTitleText.setText(new File(mFilePath).getName());
        mTitleText.setTextColor(0xFFFFFFFF);
        mTitleText.setTextSize(13);
        mTitleText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        header.addView(mTitleText);

        // 工具按钮 28dp 小按钮
        String[][] tools = {{"🔍", "查找"}, {"📂", "浏览"}, {"🕐", "最近"}, {"+", "新建"}, {"▶", "运行"}, {"🤖", "AI"}, {"💾", "保存"}, {"✕", "关闭"}};
        String[] actions = {"find", "browse", "recent", "new", "run", "ai", "save", "close"};
        for (int i = 0; i < tools.length; i++) {
            final String action = actions[i];
            Button btn = new Button(mCtx);
            btn.setText(tools[i][0]);
            btn.setContentDescription(tools[i][1]);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0x00000000);
            btn.setTextSize(12);
            btn.setPadding(dp(6), dp(2), dp(6), dp(2));
            btn.setMinimumWidth(0);
            btn.setMinWidth(0);
            btn.setMinimumHeight(0);
            btn.setMinHeight(0);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(28)));
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { handleAction(action); }
            });
            header.addView(btn);
        }
        root.addView(header);

        // 查找栏（默认隐藏）
        mFindLayout = new LinearLayout(mCtx);
        mFindLayout.setOrientation(LinearLayout.HORIZONTAL);
        mFindLayout.setBackgroundColor(0xFF2D2D30);
        mFindLayout.setPadding(dp(8), dp(4), dp(8), dp(4));
        mFindLayout.setVisibility(View.GONE);
        mFindInput = new EditText(mCtx);
        mFindInput.setHint("查找...");
        mFindInput.setTextColor(0xFFFFFFFF);
        mFindInput.setHintTextColor(0xFF888888);
        mFindInput.setBackgroundColor(0xFF3C3C3C);
        mFindInput.setPadding(dp(8), dp(4), dp(8), dp(4));
        mFindInput.setTextSize(12);
        mFindInput.setSingleLine(true);
        mFindInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        mFindLayout.addView(mFindInput);
        Button findNext = new Button(mCtx);
        findNext.setText("下一个");
        findNext.setTextColor(0xFFFFFFFF);
        findNext.setBackgroundColor(0xFF0E639C);
        findNext.setTextSize(11);
        findNext.setPadding(dp(6), dp(3), dp(6), dp(3));
        findNext.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { findNext(); } });
        mFindLayout.addView(findNext);
        Button findClose = new Button(mCtx);
        findClose.setText("✕");
        findClose.setTextColor(0xFF888888);
        findClose.setBackgroundColor(0x00000000);
        findClose.setTextSize(12);
        findClose.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { mFindLayout.setVisibility(View.GONE); } });
        mFindLayout.addView(findClose);
        root.addView(mFindLayout);

        // 信息行
        TextView info = new TextView(mCtx);
        info.setText(detectLanguage(mFilePath) + " | " + mFilePath);
        info.setTextColor(0xFF888888);
        info.setTextSize(10);
        info.setPadding(dp(8), dp(2), dp(8), dp(2));
        root.addView(info);

        // 编辑器
        ScrollView scroll = new ScrollView(mCtx);
        scroll.setFillViewport(true);
        mEditor = new EditText(mCtx);
        mEditor.setBackgroundColor(0xFF1E1E1E);
        mEditor.setTextColor(0xFFD4D4D4);
        mEditor.setHintTextColor(0xFF555555);
        mEditor.setTypeface(Typeface.MONOSPACE);
        mEditor.setTextSize(13);
        mEditor.setPadding(dp(8), dp(4), dp(8), dp(4));
        mEditor.setGravity(Gravity.TOP | Gravity.START);
        mEditor.setHorizontallyScrolling(true);
        mEditor.addTextChangedListener(new TextWatcher() {
            private android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
            private Runnable task;
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) {
                mDirty = true;
                updateTitle();
                if (task != null) h.removeCallbacks(task);
                task = new Runnable() { public void run() { applyHighlighting(s); }};
                h.postDelayed(task, 300);
            }
        });
        scroll.addView(mEditor);
        root.addView(scroll);

        setContentView(root);

        // 窗口
        try {
            Window w = getWindow();
            if (w != null) {
                w.setGravity(Gravity.BOTTOM);
                int scrH = mCtx.getResources().getDisplayMetrics().heightPixels;
                w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(scrH * 0.75));
                w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        } catch (Exception e) { android.util.Log.e("CodeEdit", "window", e); }

        loadFile();
    }

    private void handleAction(String action) {
        if ("save".equals(action)) { saveFile(); return; }
        if ("close".equals(action)) { dismiss(); return; }
        if ("find".equals(action)) {
            mFindLayout.setVisibility(mFindLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (mFindLayout.getVisibility() == View.VISIBLE) { mFindInput.requestFocus(); mLastFindPos = 0; }
            return;
        }
        if ("browse".equals(action)) { showFileBrowser(); return; }
        if ("new".equals(action)) { promptNewFile(); return; }
        if ("run".equals(action)) { runCode(); return; }
        if ("ai".equals(action)) { aiAssist(); return; }
        if ("recent".equals(action)) { showRecentFiles(); return; }
    }

    private void findNext() {
        String query = mFindInput.getText().toString();
        if (query.isEmpty()) return;
        String text = mEditor.getText().toString();
        int pos = text.indexOf(query, mLastFindPos);
        if (pos >= 0) {
            mEditor.setSelection(pos, pos + query.length());
            mLastFindPos = pos + 1;
        } else { mLastFindPos = 0; Toast.makeText(mCtx, "未找到更多匹配", Toast.LENGTH_SHORT).show(); }
    }

    private void promptNewFile() {
        try {
            final EditText input = new EditText(mCtx);
            input.setHint("文件名 (如 main.py)");
            input.setTextColor(0xFFFFFFFF);
            input.setHintTextColor(0xFF888888);
            input.setBackgroundColor(0x22FFFFFF);
            input.setPadding(dp(12), dp(8), dp(12), dp(8));
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(mCtx, com.termux.menu.R.style.Theme_GavinFloat_Dialog);
            b.setTitle("新建文件");
            b.setView(input);
            b.setPositiveButton("创建", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(mCtx, "输入文件名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File dir = new File(mFilePath).getParentFile();
                    if (dir == null) dir = new File("/data/data/com.termux/files/home");
                    try {
                        File nf = new File(dir, name);
                        nf.createNewFile();
                        new CodeEditDialog(mCtx, nf.getAbsolutePath()).show();
                        dismiss();
                    } catch (Exception e) {
                        Toast.makeText(mCtx, "创建失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            b.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) { d.dismiss(); }
            });
            android.app.AlertDialog d = b.create();
            if (d.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else d.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            d.show();
        } catch (Exception e) {
            Toast.makeText(mCtx, "新建窗口失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showFileBrowser() {
        browseDir(new File(mFilePath).getParentFile());
    }

    private void browseDir(final File startDir) {
        try {
            if (startDir == null || !startDir.exists() || !startDir.isDirectory()) return;
            final File[] files = startDir.listFiles();
            if (files == null) return;
            LinearLayout ll = new LinearLayout(mCtx);
            ll.setOrientation(LinearLayout.VERTICAL);

            // 返回上级目录
            if (startDir.getParentFile() != null) {
                TextView up = new TextView(mCtx);
                up.setText("📁 ..");
                up.setTextColor(0xFF48BAF3);
                up.setTextSize(13);
                up.setPadding(dp(12), dp(8), dp(12), dp(8));
                up.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { browseDir(startDir.getParentFile()); }
                });
                ll.addView(up);
            }

            for (int i = 0; i < files.length && i < 100; i++) {
                final File f = files[i];
                TextView tv = new TextView(mCtx);
                tv.setText((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
                tv.setTextColor(f.isDirectory() ? 0xFF48BAF3 : 0xFFFFFFFF);
                tv.setTextSize(13);
                tv.setPadding(dp(12), dp(8), dp(12), dp(8));
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (f.isDirectory()) {
                            browseDir(f);
                        } else {
                            new CodeEditDialog(mCtx, f.getAbsolutePath()).show();
                            if (mBrowserDialog != null) mBrowserDialog.dismiss();
                            dismiss();
                        }
                    }
                });
                View line = new View(mCtx);
                line.setBackgroundColor(0x11FFFFFF);
                line.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
                ll.addView(line);
                ll.addView(tv);
            }
            ScrollView sv = new ScrollView(mCtx);
            sv.addView(ll);
            if (mBrowserDialog != null) { try { mBrowserDialog.dismiss(); } catch (Exception ignored) {} }
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(mCtx, com.termux.menu.R.style.Theme_GavinFloat_Dialog);
            b.setTitle("浏览: " + startDir.getName());
            b.setView(sv);
            b.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) { d.dismiss(); if (mBrowserDialog != null) mBrowserDialog = null; }
            });
            mBrowserDialog = b.create();
            if (mBrowserDialog.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    mBrowserDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else mBrowserDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            mBrowserDialog.show();
        } catch (Exception e) {
            Toast.makeText(mCtx, "浏览失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private android.app.AlertDialog mBrowserDialog;

    private void runCode() {
        String ext = mFilePath != null ? mFilePath.toLowerCase() : "";
        String cmd;
        if (ext.endsWith(".py")) cmd = "python3 " + mFilePath;
        else if (ext.endsWith(".sh")) cmd = "bash " + mFilePath;
        else if (ext.endsWith(".js")) cmd = "node " + mFilePath;
        else if (ext.endsWith(".php")) cmd = "php " + mFilePath;
        else if (ext.endsWith(".java")) {
            String fn = new File(mFilePath).getName();
            cmd = "cd " + new File(mFilePath).getParent() + " && javac " + fn + " 2>/dev/null && java " + fn.replace(".java","") + " 2>/dev/null || echo '安装: pkg install openjdk-17'";
        } else cmd = "bash " + mFilePath;
        com.termux.menu.termux.TermuxCommandHelper.getInstance(mCtx).sendCommandToTerminal(cmd);
        Toast.makeText(mCtx, "已发送执行命令: " + ext, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void loadFile() {
        try {
            String content = FileUtils.readFile(mFilePath);
            if (content == null || content.isEmpty()) { content = "// " + new File(mFilePath).getName() + "\n\n"; mDirty = true; }
            else mDirty = false;
            mOriginalContent = content;
            mEditor.setText(content);
            updateTitle();
        } catch (Exception e) {
            mEditor.setText("文件读取失败: " + e.getMessage());
        }
    }
    private String mOriginalContent;

    private void saveFile() {
        String content = mEditor.getText().toString();
        try {
            File f = new File(mFilePath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            FileWriter fw = new FileWriter(f);
            fw.write(content);
            fw.close();
            mOriginalContent = content;
            mDirty = false;
            updateTitle();
            Toast.makeText(mCtx, "已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mCtx, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTitle() {
        String name = new File(mFilePath).getName();
        mTitleText.setText((mDirty ? "● " : "") + name);
    }

    private String detectLanguage(String path) {
        if (path == null) return "text";
        String p = path.toLowerCase();
        if (p.endsWith(".java")) return "Java";
        if (p.endsWith(".kt")) return "Kotlin";
        if (p.endsWith(".py")) return "Python";
        if (p.endsWith(".js")) return "JavaScript";
        if (p.endsWith(".ts")) return "TypeScript";
        if (p.endsWith(".c") || p.endsWith(".h")) return "C";
        if (p.endsWith(".cpp") || p.endsWith(".hpp")) return "C++";
        if (p.endsWith(".sh") || p.endsWith(".bash") || p.endsWith(".zsh")) return "Shell";
        if (p.endsWith(".xml") || p.endsWith(".html") || p.endsWith(".css")) return "Markup";
        if (p.endsWith(".json") || p.endsWith(".yaml") || p.endsWith(".yml") || p.endsWith(".toml")) return "Config";
        if (p.endsWith(".md")) return "Markdown";
        if (p.endsWith(".php")) return "PHP";
        if (p.endsWith(".rb")) return "Ruby";
        if (p.endsWith(".go")) return "Go";
        if (p.endsWith(".rs")) return "Rust";
        if (p.endsWith(".sql")) return "SQL";
        if (p.endsWith(".lua")) return "Lua";
        if (p.endsWith(".swift")) return "Swift";
        if (p.endsWith(".gradle") || p.endsWith(".groovy")) return "Groovy";
        return "text";
    }

    private void applyHighlighting(Editable editable) {
        if (editable.length() > 10000) return;
        ForegroundColorSpan[] old = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan s : old) editable.removeSpan(s);
        String text = editable.toString();
        for (int p = 0; p < HIGHLIGHT_PATTERNS.length; p++) {
            Matcher m = HIGHLIGHT_PATTERNS[p].matcher(text);
            while (m.find()) {
                editable.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLORS[p]), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void aiAssist() {
        try {
            android.content.SharedPreferences p = mCtx.getSharedPreferences("gavinfloat_api", Context.MODE_PRIVATE);
            final String endpoint = p.getString("api_endpoint", "https://api.deepseek.com/v1");
            final String apiKey = p.getString("api_key", "");
            final String model = p.getString("api_model", "deepseek-chat");
            if (apiKey.isEmpty()) { Toast.makeText(mCtx, "请先在设置中配置API Key", Toast.LENGTH_SHORT).show(); return; }

            final EditText input = new EditText(mCtx);
            input.setHint("告诉AI要做什么...");
            input.setTextColor(0xFFFFFFFF);
            input.setHintTextColor(0xFF888888);
            input.setBackgroundColor(0x22FFFFFF);
            input.setPadding(dp(12), dp(8), dp(12), dp(8));
            input.setMinLines(3);
            input.setGravity(Gravity.TOP | Gravity.START);

            LinearLayout ll = new LinearLayout(mCtx);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setPadding(dp(8), dp(8), dp(8), dp(8));

            TextView ctxLabel = new TextView(mCtx);
            ctxLabel.setText("当前文件: " + new File(mFilePath).getName() + " | 行数: " + mEditor.getText().toString().split("\n").length);
            ctxLabel.setTextColor(0xFF888888); ctxLabel.setTextSize(11);
            ll.addView(ctxLabel);
            ll.addView(input);

            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(mCtx, com.termux.menu.R.style.Theme_GavinFloat_Dialog);
            b.setTitle("🤖 AI 代码助手");
            b.setView(ll);
            b.setPositiveButton("发送", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    final String prompt = input.getText().toString().trim();
                    if (prompt.isEmpty()) return;
                    Toast.makeText(mCtx, "AI思考中...", Toast.LENGTH_SHORT).show();
                    final String code = mEditor.getText().toString();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                java.net.URL url = new java.net.URL(endpoint + "/chat/completions");
                                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST"); conn.setConnectTimeout(30000); conn.setReadTimeout(60000);
                                conn.setRequestProperty("Content-Type", "application/json");
                                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                                conn.setDoOutput(true);

                                org.json.JSONObject body = new org.json.JSONObject();
                                body.put("model", model);
                                org.json.JSONArray msgs = new org.json.JSONArray();
                                org.json.JSONObject sysMsg = new org.json.JSONObject();
                                sysMsg.put("role", "system");
                                sysMsg.put("content", "你是代码助手。当前文件: " + new File(mFilePath).getName() + "\n只返回代码，不要解释。");
                                msgs.put(sysMsg);
                                org.json.JSONObject userMsg = new org.json.JSONObject();
                                userMsg.put("role", "user");
                                userMsg.put("content", "文件内容:\n```\n" + code + "\n```\n\n请求: " + prompt);
                                msgs.put(userMsg);
                                body.put("messages", msgs);
                                body.put("max_tokens", 2048);

                                java.io.OutputStream os = conn.getOutputStream();
                                os.write(body.toString().getBytes("UTF-8")); os.close();

                                if (conn.getResponseCode() == 200) {
                                    java.io.InputStream is = conn.getInputStream();
                                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                                    byte[] buf = new byte[4096]; int n;
                                    while ((n = is.read(buf)) != -1) bos.write(buf, 0, n); is.close();
                                    org.json.JSONObject resp = new org.json.JSONObject(bos.toString("UTF-8"));
                                    final String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            int pos = mEditor.getSelectionStart();
                                            if (pos < 0) pos = mEditor.length();
                                            mEditor.getText().insert(pos, "\n" + reply + "\n");
                                            Toast.makeText(mCtx, "AI回复已插入", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    final int code2 = conn.getResponseCode();
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                        public void run() { Toast.makeText(mCtx, "API错误: HTTP " + code2, Toast.LENGTH_SHORT).show(); }
                                    });
                                }
                                conn.disconnect();
                            } catch (final Exception e) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                    public void run() { Toast.makeText(mCtx, "AI错误: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
                                });
                            }
                        }
                    }).start();
                }
            });
            b.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) { d.dismiss(); }
            });
            android.app.AlertDialog d = b.create();
            if (d.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else d.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            d.show();
        } catch (Exception e) {
            Toast.makeText(mCtx, "AI错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRecentFiles() {
        try {
            android.content.SharedPreferences p = mCtx.getSharedPreferences("gavinfloat_editor", Context.MODE_PRIVATE);
            java.util.Set<String> recent = p.getStringSet("recent_files", new java.util.HashSet<String>());
            if (recent.isEmpty()) { Toast.makeText(mCtx, "暂无最近文件", Toast.LENGTH_SHORT).show(); return; }
            java.util.ArrayList<String> list = new java.util.ArrayList<>(recent);
            java.util.Collections.sort(list, java.util.Collections.reverseOrder());

            LinearLayout ll = new LinearLayout(mCtx);
            ll.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < list.size() && i < 30; i++) {
                final String fp = list.get(i);
                if (!new File(fp).exists()) continue;
                TextView tv = new TextView(mCtx);
                tv.setText("📄 " + new File(fp).getName() + "  (" + new File(fp).getParent() + ")");
                tv.setTextColor(0xFFFFFFFF);
                tv.setTextSize(12);
                tv.setPadding(dp(12), dp(8), dp(12), dp(8));
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        new CodeEditDialog(mCtx, fp).show();
                        if (mRecentDialog != null) mRecentDialog.dismiss();
                        dismiss();
                    }
                });
                View line = new View(mCtx);
                line.setBackgroundColor(0x11FFFFFF);
                line.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
                ll.addView(line);
                ll.addView(tv);
            }
            ScrollView sv = new ScrollView(mCtx); sv.addView(ll);
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(mCtx, com.termux.menu.R.style.Theme_GavinFloat_Dialog);
            b.setTitle("最近文件"); b.setView(sv);
            b.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) { d.dismiss(); }
            });
            mRecentDialog = b.create();
            if (mRecentDialog.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    mRecentDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                else mRecentDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            mRecentDialog.show();
        } catch (Exception e) {
            Toast.makeText(mCtx, "最近文件: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private android.app.AlertDialog mRecentDialog;

    private int dp(int val) { return (int)(val * mCtx.getResources().getDisplayMetrics().density + 0.5f); }
}
