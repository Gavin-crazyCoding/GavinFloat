package com.termux.menu.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 容器切换 — PopupWindow 实现，无需 Activity 上下文
 */
public class TermuxContainerSwitchDialog {

    private final Context mCtx;
    private final Handler mH = new Handler(Looper.getMainLooper());
    private WindowManager mWm;
    private View mRootView;
    private PopupWindow mPopup;
    private LinearLayout mList;

    public TermuxContainerSwitchDialog(Context context) {
        mCtx = context;
        mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void show() {
        LinearLayout root = new LinearLayout(mCtx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setPadding(dp(20), dp(20), dp(16), dp(20));

        TextView title = new TextView(mCtx);
        title.setText("Termux 容器切换");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView sub = new TextView(mCtx);
        sub.setText("扫描 /data/data/com.termux/files*");
        sub.setTextColor(0xFF888888);
        sub.setTextSize(11);
        sub.setPadding(0, 0, 0, dp(12));
        root.addView(sub);

        Button closeBtn = new Button(mCtx);
        closeBtn.setText("✕ 关闭");
        closeBtn.setTextColor(0xFF888888);
        closeBtn.setTextSize(12);
        closeBtn.setBackgroundColor(0x00000000);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) { dismiss(); }
        });
        root.addView(closeBtn);

        mList = new LinearLayout(mCtx);
        mList.setOrientation(LinearLayout.VERTICAL);
        root.addView(mList);

        TextView warn = new TextView(mCtx);
        warn.setText("切换前请关闭 Termux\n使用 Java renameTo，安全不崩溃");
        warn.setTextColor(0xFFFF9800);
        warn.setTextSize(11);
        warn.setPadding(0, dp(8), 0, 0);
        root.addView(warn);

        ScrollView sv = new ScrollView(mCtx);
        sv.addView(root);

        int h = (int) (mCtx.getResources().getDisplayMetrics().heightPixels * 0.65);
        mPopup = new PopupWindow(sv, ViewGroup.LayoutParams.MATCH_PARENT, h, true);
        mPopup.setBackgroundDrawable(null);
        mPopup.setOutsideTouchable(true);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mPopup.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                mPopup.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            mPopup.showAtLocation(sv, Gravity.BOTTOM, 0, 0);
        } catch (Exception e) {
            Toast.makeText(mCtx, "容器切换暂不可用: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        scan();
    }

    public void dismiss() {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    private void scan() {
        TextView ld = new TextView(mCtx);
        ld.setText("扫描中...");
        ld.setTextColor(0xFF888888);
        ld.setPadding(0, dp(16), 0, dp(16));
        mList.addView(ld);

        new Thread(new Runnable() {
            public void run() {
                final List<String[]> items = new ArrayList<String[]>();
                File root = new File("/data/data/com.termux");
                if (root.exists()) {
                    File[] files = root.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isDirectory() && f.getName().startsWith("files")) {
                                items.add(new String[]{f.getName(), f.getAbsolutePath()});
                            }
                        }
                    }
                }
                Collections.sort(items, new Comparator<String[]>() {
                    public int compare(String[] a, String[] b) {
                        boolean aAct = a[0].equals("files");
                        boolean bAct = b[0].equals("files");
                        return aAct ? -1 : bAct ? 1 : a[0].compareTo(b[0]);
                    }
                });
                mH.post(new Runnable() {
                    public void run() { showItems(items); }
                });
            }
        }).start();
    }

    private void showItems(final List<String[]> items) {
        mList.removeAllViews();
        if (items.isEmpty()) {
            TextView e = new TextView(mCtx);
            e.setText("未找到容器目录");
            e.setTextColor(0xFFFF5252);
            e.setPadding(0, dp(8), 0, dp(8));
            mList.addView(e);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            final String name = items.get(i)[0];
            final String path = items.get(i)[1];
            final boolean active = name.equals("files");

            LinearLayout card = new LinearLayout(mCtx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(12), dp(16), dp(12));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
            cp.bottomMargin = dp(8);
            card.setLayoutParams(cp);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(active ? 0xFF1E3A5F : 0xFF2A2A3A);
            bg.setCornerRadius(dp(10));
            card.setBackground(bg);

            TextView nameTv = new TextView(mCtx);
            nameTv.setText(name + (active ? " (当前)" : ""));
            nameTv.setTextColor(active ? 0xFF4CAF50 : 0xFFFFFFFF);
            nameTv.setTextSize(15);
            nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(nameTv);

            if (!active) {
                View sp = new View(mCtx);
                sp.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(8)));
                card.addView(sp);

                Button btn = new Button(mCtx);
                btn.setText("切换至此容器");
                btn.setTextColor(0xFFFFFFFF);
                GradientDrawable btnBg = new GradientDrawable();
                btnBg.setColor(0xFF4CAF50);
                btnBg.setCornerRadius(dp(6));
                btn.setBackground(btnBg);
                btn.setPadding(dp(14), dp(8), dp(14), dp(8));
                btn.setTextSize(12);
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        doSwitch(name, path);
                    }
                });
                card.addView(btn);
            }
            mList.addView(card);
        }
    }

    private void doSwitch(final String targetName, final String targetPath) {
        Toast.makeText(mCtx, "切换中...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {
                File active = new File("/data/data/com.termux/files");
                File target = new File(targetPath);
                File temp = new File("/data/data/com.termux/.swap_tmp_java");

                if (temp.exists()) deleteRecursive(temp);
                if (!active.renameTo(temp)) { fail("第一步失败"); return; }
                if (!target.renameTo(active)) { temp.renameTo(active); fail("第二步失败"); return; }
                if (!temp.renameTo(target)) { target.renameTo(active); fail("第三步失败"); return; }
                mH.post(new Runnable() {
                    public void run() {
                        Toast.makeText(mCtx, "✓ 切换成功，请重启 Termux", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
            }
        }).start();
    }

    private void fail(final String msg) {
        mH.post(new Runnable() {
            public void run() {
                Toast.makeText(mCtx, "✗ " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) { File[] c = f.listFiles(); if (c != null) for (File x : c) deleteRecursive(x); }
        f.delete();
    }

    private int dp(int v) {
        return (int) (v * mCtx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
