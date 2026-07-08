package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.content.SharedPreferences;
import android.graphics.Color; import android.graphics.drawable.ColorDrawable; import android.graphics.drawable.GradientDrawable;
import android.os.Build; import android.view.Gravity; import android.view.View; import android.view.ViewGroup;
import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;
import java.util.ArrayList; import java.util.List;

/** SSH连接管理器 — 一键连接/保存/管理SSH */
public class SshManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mHost,mPort,mUser,mPass; private LinearLayout mSavedList;
    private SharedPreferences mPrefs; private static final String PREFS="gavinfloat_ssh";
    public SshManagerDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);mPrefs=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("SSH连接管理器");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setTypeface(null,android.graphics.Typeface.BOLD);t.setPadding(0,0,0,dp(8));r.addView(t);

        r.addView(label("主机:"));mHost=inp("");mHost.setHint("192.168.1.100");r.addView(mHost);
        r.addView(label("端口:"));mPort=inp("22");r.addView(mPort);
        r.addView(label("用户名:"));mUser=inp("root");r.addView(mUser);
        r.addView(label("密码:"));mPass=inp("");mPass.setHint("留空使用密钥");mPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);r.addView(mPass);

        LinearLayout btns=new LinearLayout(mCtx);btns.setPadding(0,dp(8),0,dp(8));
        Button conn=btn("连接",0xFF4CAF50);conn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){connectSSH(false);}});btns.addView(conn);
        Button save=btn("保存",0xFF2196F3);save.setOnClickListener(new View.OnClickListener(){public void onClick(View v){saveHost();}});btns.addView(save);
        Button root=btn("Root连接",0xFFFF9800);root.setOnClickListener(new View.OnClickListener(){public void onClick(View v){connectSSH(true);}});btns.addView(root);
        r.addView(btns);

        r.addView(label("已保存的连接:"));
        mSavedList=new LinearLayout(mCtx);mSavedList.setOrientation(LinearLayout.VERTICAL);r.addView(mSavedList);
        sv.addView(r);setContentView(sv);loadSaved();
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void connectSSH(boolean asRoot){
        String h=mHost.getText().toString().trim(),p=mPort.getText().toString().trim(),u=mUser.getText().toString().trim(),pw=mPass.getText().toString().trim();
        if(h.isEmpty()){Toast.makeText(mCtx,"输入主机地址",Toast.LENGTH_SHORT).show();return;}
        if(p.isEmpty())p="22";
        if(u.isEmpty())u="root";
        String cmd;
        if(pw.isEmpty())cmd="ssh -p "+p+" "+u+"@"+h;
        else cmd="sshpass -p '"+pw.replace("'","'\\''")+"' ssh -o StrictHostKeyChecking=no -p "+p+" "+u+"@"+h;
        if(asRoot){if(!pw.isEmpty())cmd="sshpass -p '"+pw.replace("'","'\\''")+"' ssh -o StrictHostKeyChecking=no -p "+p+" root@"+h; else cmd="ssh -p "+p+" root@"+h;}
        mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,"正在连接SSH: "+u+"@"+h,Toast.LENGTH_SHORT).show();dismiss();
    }
    private void saveHost(){
        String h=mHost.getText().toString().trim(),p=mPort.getText().toString().trim(),u=mUser.getText().toString().trim();
        if(h.isEmpty()){Toast.makeText(mCtx,"输入主机",Toast.LENGTH_SHORT).show();return;}
        if(p.isEmpty())p="22";if(u.isEmpty())u="root";
        int c=mPrefs.getInt("count",0);
        mPrefs.edit().putString("host_"+c,h).putString("port_"+c,p).putString("user_"+c,u).putInt("count",c+1).apply();
        mHost.setText("");loadSaved();Toast.makeText(mCtx,"已保存",Toast.LENGTH_SHORT).show();
    }
    private void loadSaved(){
        mSavedList.removeAllViews();int cnt=mPrefs.getInt("count",0);
        for(int i=0;i<cnt;i++){final String h=mPrefs.getString("host_"+i,""),p=mPrefs.getString("port_"+i,""),u=mPrefs.getString("user_"+i,"");
            if(h.isEmpty())continue;final int idx=i;
            LinearLayout row=new LinearLayout(mCtx);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(4),dp(4),dp(4));
            TextView info=new TextView(mCtx);info.setText(u+"@"+h+":"+p);info.setTextColor(0xFFFFFFFF);info.setTextSize(13);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));row.addView(info);
            Button cbtn=btn2("连接",0xFF4CAF50);cbtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mHost.setText(h);mPort.setText(p);mUser.setText(u);connectSSH(false);
            }});row.addView(cbtn);
            Button dbtn=btn2("删除",0xFFF44336);dbtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mPrefs.edit().remove("host_"+idx).remove("port_"+idx).remove("user_"+idx).apply();loadSaved();
            }});row.addView(dbtn);
            mSavedList.addView(row);
        }
    }
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(8),0,dp(4));return tv;}
    private EditText inp(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(14),dp(8),dp(14),dp(8));return b;}
    private Button btn2(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(c);b.setBackgroundColor(0x00000000);b.setTextSize(11);b.setPadding(dp(8),dp(2),dp(8),dp(2));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
