package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable; import android.os.Build; import android.view.Gravity; import android.view.View;
import android.view.ViewGroup; import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;

/** 系统清理/优化 */
public class SystemCleanerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private LinearLayout mResult;
    public SystemCleanerDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("系统清理");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setTypeface(null,android.graphics.Typeface.BOLD);t.setPadding(0,0,0,dp(4));r.addView(t);
        TextView sub=new TextView(mCtx);sub.setText("一键清理Termux缓存和临时文件");sub.setTextColor(0xFF888888);sub.setTextSize(11);sub.setPadding(0,0,0,dp(12));r.addView(sub);

        String[][] items={
            {"apt缓存","apt clean && echo 'apt缓存已清理'","清理 /var/cache/apt/archives"},
            {"pip缓存","pip cache purge 2>/dev/null || echo 无pip缓存","清理 pip 下载缓存"},
            {"npm缓存","npm cache clean --force 2>/dev/null || echo 无npm缓存","清理 npm 缓存"},
            {"临时文件","rm -rf ~/.tmp/* ~/.cache/* 2>/dev/null; echo done","清理用户临时文件"},
            {"日志文件","rm -f ~/termux.log* 2>/dev/null; logcat -c 2>/dev/null; echo done","清理各种日志"},
            {"驻留包检测","dpkg --get-selections | grep deinstall | awk '{print $1}' | head -20","显示可清除的残留包"},
            {"大文件查找","find ~ -type f -size +50M 2>/dev/null | head -20","查找 >50MB 的大文件"},
            {"全部清理","apt clean && pip cache purge 2>/dev/null && npm cache clean --force 2>/dev/null && rm -rf ~/.tmp/* 2>/dev/null && echo 全部清理完成","一键执行所有清理"},
        };

        mResult=new LinearLayout(mCtx);mResult.setOrientation(LinearLayout.VERTICAL);
        for(final String[] item:items){
            Button b=new Button(mCtx);b.setText(item[0]);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(0xFF2A2A3A);b.setTextSize(12);
            GradientDrawable g=new GradientDrawable();g.setColor(0xFF2A2A3A);g.setCornerRadius(dp(8));b.setBackground(g);
            b.setPadding(dp(12),dp(10),dp(12),dp(10));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);bp.bottomMargin=dp(6);b.setLayoutParams(bp);
            final String cmd=item[1],desc=item[2];
            b.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
                mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,desc,Toast.LENGTH_SHORT).show();
            }});mResult.addView(b);
        }r.addView(mResult);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
