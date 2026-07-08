package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.termux.menu.termux.TermuxCommandHelper;

/** 备份管理器 */
public class BackupManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    public BackupManagerDialog(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"备份管理器");

        String[][] items={
            {"备份Home","cd ~ && tar czf ~/backup_home_$(date +%Y%m%d).tar.gz . 2>&1","备份整个Home目录"},
            {"备份usr","cd /data/data/com.termux/files/usr && tar czf ~/backup_usr_$(date +%Y%m%d).tar.gz . 2>&1","备份usr目录"},
            {"备份全部","cd /data/data/com.termux/files && tar czf ~/backup_full_$(date +%Y%m%d).tar.gz . --exclude='backup_*' 2>&1","备份整个Termux数据"},
            {"查看备份","ls -lh ~/backup_*.tar.gz 2>/dev/null || echo 无备份文件","列出所有备份文件"},
            {"恢复Home","cd ~ && tar xzf ~/backup_home_*.tar.gz 2>&1","从最新Home备份恢复"},
            {"复制到SD卡","cp ~/backup_*.tar.gz /sdcard/ 2>/dev/null && echo 已复制","复制备份到SD卡"},
        };

        for(final String[] item:items){
            LinearLayout card=new LinearLayout(mCtx);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackgroundColor(0xFF2A2A3A);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);cp.bottomMargin=dp(6);card.setLayoutParams(cp);card.setOrientation(LinearLayout.VERTICAL);
            TextView name=new TextView(mCtx);name.setText(item[0]);name.setTextColor(0xFFFFFFFF);name.setTextSize(14);card.addView(name);
            TextView desc=new TextView(mCtx);desc.setText(item[2]);desc.setTextColor(0xFF888888);desc.setTextSize(11);desc.setPadding(0,dp(2),0,dp(4));card.addView(desc);
            Button btn=new Button(mCtx);btn.setText("执行");btn.setTextColor(0xFF4CAF50);btn.setBackgroundColor(0x00000000);btn.setTextSize(12);btn.setPadding(0,dp(4),0,dp(4));
            btn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal(item[1]);Toast.makeText(mCtx,item[0]+" 已发送",Toast.LENGTH_SHORT).show();dismiss();
            }});card.addView(btn);r.addView(card);
        }
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
