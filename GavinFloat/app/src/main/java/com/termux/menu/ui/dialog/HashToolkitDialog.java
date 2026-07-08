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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;

/** Hash工具包 — hash识别/计算/破解 */
public class HashToolkitDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    private EditText mHashInput,mWordlist,mHashFile;
    public HashToolkitDialog(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"Hash 工具包");

        r.addView(label("Hash值:"));
        mHashInput=edit("");mHashInput.setHint("粘贴hash值...");r.addView(mHashInput);

        // Hash识别
        LinearLayout idRow=new LinearLayout(mCtx);
        Button idBtn=btn("识别Hash类型",0xFF2196F3);idBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String h=mHashInput.getText().toString().trim();
            if(h.isEmpty()){Toast.makeText(mCtx,"输入hash",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("nethunter -r 'hashid -m "+h+"' || echo 'hashid未安装'");
            Toast.makeText(mCtx,"正在识别...",Toast.LENGTH_SHORT).show();
        }});idRow.addView(idBtn);

        Button calcBtn=btn("识别Hash",0xFFFF9800);calcBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String h=mHashInput.getText().toString().trim();
            mCmd.sendCommandToTerminal("nethunter -r 'echo "+h+" | hashid'");
        }});idRow.addView(calcBtn);r.addView(idRow);

        // Hash类型快捷
        r.addView(label("Hash类型:"));
        String[] types={"MD5","SHA1","SHA256","SHA512","NTLM","MySQL","bcrypt","MD5(Unix)","SHA256(Unix)","SHA512(Unix)","WPA/WPA2","ZIP/RAR","PDF","Office"};
        LinearLayout typeGrid=new LinearLayout(mCtx);typeGrid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<types.length;i+=3){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<3&&(i+j)<types.length;j++){final String t=types[i+j];
                Button tb=btn(t,0xFF37474F);tb.setTextSize(11);tb.setPadding(dp(6),dp(4),dp(6),dp(4));
                tb.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                    mHashInput.setText(mHashInput.getText()+" "+t);
                }});
                LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);tp.rightMargin=j<2?dp(4):0;tp.bottomMargin=dp(4);tb.setLayoutParams(tp);row.addView(tb);
            }typeGrid.addView(row);
        }r.addView(typeGrid);

        // 破解
        r.addView(label("字典路径:"));
        mWordlist=edit("/usr/share/wordlists/rockyou.txt.gz");r.addView(mWordlist);
        r.addView(label("Hash文件路径(可选):"));
        mHashFile=edit("");mHashFile.setHint("多hash用文件");r.addView(mHashFile);

        LinearLayout crackRow=new LinearLayout(mCtx);
        Button johnBtn=btn("John破解",0xFFE91E63);johnBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String wl=mWordlist.getText().toString().trim(),hf=mHashFile.getText().toString().trim(),hi=mHashInput.getText().toString().trim();
            String h=hf.isEmpty()?hi.replaceAll("\\s+\\w+$","").trim():hf;
            if(h.isEmpty()){Toast.makeText(mCtx,"输入hash",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("nethunter -r 'echo "+h+" > /tmp/hash.txt && john --wordlist="+wl+" /tmp/hash.txt'");
            Toast.makeText(mCtx,"John已启动",Toast.LENGTH_SHORT).show();dismiss();
        }});crackRow.addView(johnBtn);

        Button hashcatBtn=btn("Hashcat",0xFF9C27B0);hashcatBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String wl=mWordlist.getText().toString().trim(),hi=mHashInput.getText().toString().trim();
            String h=hi.replaceAll("\\s+\\w+$","").trim();
            if(h.isEmpty()){Toast.makeText(mCtx,"输入hash",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("nethunter -r 'echo "+h+" > /tmp/hash.txt && hashcat -m 0 -a 0 /tmp/hash.txt "+wl+"'");
            Toast.makeText(mCtx,"Hashcat已启动",Toast.LENGTH_SHORT).show();dismiss();
        }});crackRow.addView(hashcatBtn);r.addView(crackRow);

        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.8));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText edit(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(12);b.setPadding(dp(8),dp(6),dp(8),dp(6));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
