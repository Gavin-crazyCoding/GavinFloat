package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.os.Build; import android.os.Handler; import android.os.Looper; import android.view.Gravity; import android.view.View; import android.view.ViewGroup;
import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;

public class PythonManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mPkg;
    public PythonManagerDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"Python/pip 管理");
        r.addView(label("安装包:"));
        LinearLayout rowL=new LinearLayout(mCtx);
        mPkg=edit("");mPkg.setHint("包名 如: requests");rowL.addView(mPkg);
        Button inst=btn("安装",0xFF4CAF50);inst.setTextSize(12);
        inst.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            String p=mPkg.getText().toString().trim();if(p.isEmpty())return;
            mCmd.sendCommandToTerminal("pip install "+p);Toast.makeText(mCtx,"pip install "+p,Toast.LENGTH_SHORT).show();
        }});rowL.addView(inst);r.addView(rowL);
        // 纵向排列按钮，避免溢出
        LinearLayout btnGrid=new LinearLayout(mCtx);btnGrid.setOrientation(LinearLayout.VERTICAL);
        String[][] items={{"列出已安装","pip list 2>/dev/null | head -40"},{"更新pip","pip install --upgrade pip"},
            {"virtualenv","pip install virtualenv"},{"检查Python","python --version 2>/dev/null || pkg install python -y"},
            {"jupyter","pip install jupyter"},{"requests","pip install requests"},{"flask","pip install flask"},
            {"numpy","pip install numpy"},{"HTTP服务器","python3 -m http.server 8080"},{"JSON格式化","python3 -m json.tool"}};
        for(int i=0;i<items.length;i+=2){LinearLayout rowLL=new LinearLayout(mCtx);
            for(int j=0;j<2&&(i+j)<items.length;j++){final String cmd=items[i+j][1];final String name=items[i+j][0];
                Button tb=btn(name,0xFF37474F);tb.setTextSize(11);tb.setPadding(dp(6),dp(4),dp(6),dp(4));
                tb.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
                    mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,name,Toast.LENGTH_SHORT).show();
                }});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);
                lp.rightMargin=j<1?dp(4):0;lp.bottomMargin=dp(4);tb.setLayoutParams(lp);rowLL.addView(tb);
            }btnGrid.addView(rowLL);
        }r.addView(btnGrid);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.6));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText edit(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);et.setLayoutParams(lp);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(12);b.setPadding(dp(8),dp(6),dp(8),dp(6));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
