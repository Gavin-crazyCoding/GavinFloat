package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.termux.menu.termux.TermuxCommandHelper;

/** 磁盘分析器 — 可视化目录大小 */
public class DiskAnalyzerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private LinearLayout mList; private Handler mH=new Handler(Looper.getMainLooper());
    public DiskAnalyzerDialog(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"磁盘分析器");

        String[] dirs={"/data/data/com.termux/files/home","/data/data/com.termux/files/usr","/sdcard","/data","/system"};
        for(final String dir:dirs){Button b=btn(dir,0xFF37474F);b.setTextSize(11);b.setPadding(dp(8),dp(6),dp(8),dp(6));
            b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){analyze(dir);}});r.addView(b);
        }

        mList=new LinearLayout(mCtx);mList.setOrientation(LinearLayout.VERTICAL);r.addView(mList);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.75));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void analyze(String path){
        mList.removeAllViews();TextView loading=new TextView(mCtx);loading.setText("分析中: "+path);loading.setTextColor(0xFF888888);loading.setTextSize(13);mList.addView(loading);
        mCmd.executeAndCapture("du -sh "+path+"/* 2>/dev/null | sort -rh | head -30",new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
            mH.post(new Runnable(){public void run(){mList.removeAllViews();
                String[] lines=o.split("\n");
                for(final String line:lines){String l=line.trim();if(l.isEmpty())continue;
                    String[] parts=l.split("\t");if(parts.length<2)continue;
                    final String size=parts[0],name=parts[1];
                    LinearLayout row=new LinearLayout(mCtx);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(3),dp(4),dp(3));
                    TextView sz=new TextView(mCtx);sz.setText(size);sz.setTextColor(0xFFFF9800);sz.setTextSize(12);sz.setPadding(0,0,dp(8),0);row.addView(sz);
                    TextView nm=new TextView(mCtx);nm.setText(name);nm.setTextColor(0xFFFFFFFF);nm.setTextSize(12);nm.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));row.addView(nm);
                    Button open=quickBtn("打开",0xFF2196F3);open.setOnClickListener(new View.OnClickListener(){public void onClick(View v){dismiss();mCmd.sendCommandToTerminal("ls -la "+name);}});row.addView(open);
                    mList.addView(row);
                }
            }});
        }});
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(10),dp(6),dp(10),dp(6));return b;}
    private Button quickBtn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(c);b.setBackgroundColor(0x00000000);b.setTextSize(10);b.setPadding(dp(6),dp(2),dp(6),dp(2));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
