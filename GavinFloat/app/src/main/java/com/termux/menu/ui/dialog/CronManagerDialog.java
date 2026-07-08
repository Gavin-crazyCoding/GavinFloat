package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.os.Build; import android.view.Gravity; import android.view.View; import android.view.ViewGroup; import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;
public class CronManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mCronSpec, mCmdEdit;
    private static final String[][] PRESETS={
            {"每分钟","* * * * *"},{"每5分钟","*/5 * * * *"},{"每10分钟","*/10 * * * *"},
            {"每小时","0 * * * *"},{"每天0点","0 0 * * *"},{"每天12点","0 12 * * *"},
            {"每周一","0 0 * * 1"},{"每月1号","0 0 1 * *"},{"每季度","0 0 1 1,4,7,10 *"},
            {"重启后","@reboot"},{"每小时","@hourly"},{"每天","@daily"},{"每周","@weekly"},{"每月","@monthly"}
    };
    public CronManagerDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("定时任务(Crontab)");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setTypeface(null, android.graphics.Typeface.BOLD);t.setPadding(0,0,0,dp(10));r.addView(t);

        r.addView(label("定时表达式:"));
        mCronSpec=new EditText(mCtx);mCronSpec.setText("* * * * *");mCronSpec.setTextColor(0xFFFFFFFF);mCronSpec.setHintTextColor(0xFF888888);mCronSpec.setBackgroundColor(0x22FFFFFF);mCronSpec.setPadding(dp(12),dp(8),dp(12),dp(8));mCronSpec.setTextSize(14);r.addView(mCronSpec);

        TextView p=new TextView(mCtx);p.setText("快捷预设:");p.setTextColor(0xFFD4AF37);p.setTextSize(12);p.setPadding(0,dp(8),0,dp(4));r.addView(p);
        LinearLayout grid=new LinearLayout(mCtx);grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<PRESETS.length;i+=3){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<3&&(i+j)<PRESETS.length;j++){final String v=PRESETS[i+j][1];final String n=PRESETS[i+j][0];
                Button b=new Button(mCtx);b.setText(n);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(0xFF37474F);b.setTextSize(11);b.setPadding(dp(6),dp(4),dp(6),dp(4));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View view){mCronSpec.setText(v);}});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j<2?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }grid.addView(row);
        }r.addView(grid);

        r.addView(label("要执行的命令:"));
        mCmdEdit=new EditText(mCtx);mCmdEdit.setHint("cd ~ && echo hello > /sdcard/test.txt");mCmdEdit.setTextColor(0xFFFFFFFF);mCmdEdit.setHintTextColor(0xFF888888);mCmdEdit.setBackgroundColor(0x22FFFFFF);mCmdEdit.setPadding(dp(12),dp(8),dp(12),dp(8));mCmdEdit.setTextSize(14);r.addView(mCmdEdit);

        LinearLayout btns=new LinearLayout(mCtx);btns.setPadding(0,dp(8),0,0);
        Button add=btn("添加任务",0xFF4CAF50);add.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            String cron=mCronSpec.getText().toString().trim(),cmd=mCmdEdit.getText().toString().trim();
            if(cmd.isEmpty()){Toast.makeText(mCtx,"请输入命令",Toast.LENGTH_SHORT).show();return;}
            String safeCron = cron.replace("'", "'\\''");
            String safeCmd = cmd.replace("'", "'\\''");
            mCmd.sendCommandToTerminal("(crontab -l 2>/dev/null; echo '"+safeCron+" "+safeCmd+"') | crontab -");
            Toast.makeText(mCtx,"定时任务已添加",Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(add);
        Button list=btn("列出任务",0xFF2196F3);list.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            mCmd.sendCommandToTerminal("crontab -l 2>/dev/null || echo '暂无定时任务'");
            Toast.makeText(mCtx,"已发送",Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(list);
        Button del=btn("清空任务",0xFFF44336);del.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            mCmd.sendCommandToTerminal("crontab -r && echo '已清空'");
            Toast.makeText(mCtx,"已清空",Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(del);r.addView(btns);

        sv.addView(r);setContentView(sv);Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(12);b.setPadding(dp(10),dp(8),dp(10),dp(8));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
