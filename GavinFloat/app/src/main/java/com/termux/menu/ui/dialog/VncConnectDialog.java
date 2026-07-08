package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.os.Build; import android.view.Gravity; import android.view.View; import android.view.ViewGroup; import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;

/** VNC连接管理 */
public class VncConnectDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mAddr,mPort,mPass;
    public VncConnectDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"VNC 连接管理");

        r.addView(label("地址:"));
        mAddr=edit("127.0.0.1");r.addView(mAddr);
        r.addView(label("端口:"));
        mPort=edit("5901");r.addView(mPort);
        r.addView(label("密码(可选):"));
        mPass=edit("");mPass.setHint("默认password");r.addView(mPass);

        LinearLayout btns=new LinearLayout(mCtx);btns.setPadding(0,dp(8),0,dp(8));
        Button conn=btn("连接",0xFF4CAF50);conn.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            String a=mAddr.getText().toString().trim(),p=mPort.getText().toString().trim(),pw=mPass.getText().toString().trim();
            String cmd="nethunter -r 'vncviewer "+a+":"+p+(pw.isEmpty()?"":" -password "+pw)+"'";
            if(!pw.isEmpty())cmd="nethunter -r 'echo "+pw+" | vncviewer -autopass "+a+":"+p+"'";
            mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,"VNC连接中: "+a+":"+p,Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(conn);
        Button start=btn("启动服务",0xFF2196F3);start.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            mCmd.sendCommandToTerminal("nethunter -r 'vncserver -geometry 1280x720 :1'");
            Toast.makeText(mCtx,"VNC服务启动中",Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(start);
        Button stop=btn("停止服务",0xFFF44336);stop.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
            mCmd.sendCommandToTerminal("nethunter -r 'vncserver -kill :1'");
            Toast.makeText(mCtx,"VNC服务已停止",Toast.LENGTH_SHORT).show();dismiss();
        }});btns.addView(stop);
        r.addView(btns);

        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.55));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setTypeface(null,android.graphics.Typeface.BOLD);tv.setPadding(0,0,0,dp(8));r.addView(tv);}
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(8),0,dp(4));return tv;}
    private EditText edit(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(14),dp(8),dp(14),dp(8));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
