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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;

/** Kali 服务管理器 — 可视化启停常用服务 */
public class KaliServiceManager extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    public KaliServiceManager(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"Kali 服务管理");

        String[][] services={
            {"SSH (22)","service ssh start","service ssh stop","service ssh status"},
            {"Apache (80)","service apache2 start","service apache2 stop","service apache2 status"},
            {"MySQL (3306)","service mysql start","service mysql stop","service mysql status"},
            {"PostgreSQL","service postgresql start","service postgresql stop","service postgresql status"},
            {"VNC (5901)","vncserver :1","vncserver -kill :1","vncserver -list"},
            {"X11VNC","x11vnc -forever","pkill x11vnc","pgrep x11vnc"},
            {"ngrok","ngrok tcp 22","pkill ngrok","pgrep ngrok"},
            {"tor","service tor start","service tor stop","service tor status"},
            {"proxychains","proxychains4 curl ifconfig.me 2>/dev/null","pkill proxychains","pgrep proxychains"},
            {"Metasploit","msfdb start","msfdb stop","msfdb status"},
        };

        for(final String[] s:services){
            LinearLayout card=new LinearLayout(mCtx);card.setPadding(dp(12),dp(8),dp(12),dp(8));card.setBackgroundColor(0xFF2A2A3A);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);cp.bottomMargin=dp(6);card.setLayoutParams(cp);card.setOrientation(LinearLayout.VERTICAL);

            TextView name=new TextView(mCtx);name.setText(s[0]);name.setTextColor(0xFFFFFFFF);name.setTextSize(15);name.setPadding(0,0,0,dp(4));card.addView(name);

            LinearLayout btns=new LinearLayout(mCtx);btns.setOrientation(LinearLayout.HORIZONTAL);
            Button start=quickBtn("启动",0xFF4CAF50);start.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal("nethunter -r '"+s[1]+"'");Toast.makeText(mCtx,s[0]+" 启动中",Toast.LENGTH_SHORT).show();
            }});btns.addView(start);
            Button stop=quickBtn("停止",0xFFF44336);stop.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal("nethunter -r '"+s[2]+"'");Toast.makeText(mCtx,s[0]+" 已停止",Toast.LENGTH_SHORT).show();
            }});btns.addView(stop);
            Button stat=quickBtn("状态",0xFF2196F3);stat.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal("nethunter -r '"+s[3]+"'");
            }});btns.addView(stat);
            card.addView(btns);r.addView(card);
        }
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.8));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private Button quickBtn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(c);b.setBackgroundColor(0x00000000);b.setTextSize(11);b.setPadding(dp(8),dp(3),dp(8),dp(3));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
