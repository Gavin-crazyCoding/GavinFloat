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

/** 代码模板 — 快捷粘贴常用代码片段 */
public class TextTemplatesDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    private static final String[][] TEMPLATES={
        {"Python Flask","from flask import Flask\napp=Flask(__name__)\n@app.route('/')\ndef hello():\n    return 'Hello'\napp.run(host='0.0.0.0',port=8080)"},
        {"Python HTTP","python3 -m http.server 8080"},
        {"Bash循环","for i in $(seq 1 10); do\n  echo $i\ndone"},
        {"Bash函数","myfunc(){\n  echo $1\n}\nmyfunc hello"},
        {"C main","#include <stdio.h>\nint main(){\n  printf(\"Hello\");\n  return 0;\n}"},
        {"Java class","public class Main{\n  public static void main(String[]a){\n    System.out.println(\"Hello\");\n  }\n}"},
        {"HTML5","<!DOCTYPE html><html><head><title>Page</title></head><body><h1>Hello</h1></body></html>"},
        {"JS fetch","fetch('https://api.example.com')\n  .then(r=>r.json())\n  .then(d=>console.log(d))"},
        {"Nmap快速","nmap -sV -sC -T4 -p- {target}"},
        {"SQLMap GET","sqlmap -u '{url}' --batch --random-agent --dbs"},
        {"Metasploit","msfconsole -q -x 'use multi/handler;set PAYLOAD android/meterpreter/reverse_tcp;set LHOST {ip};set LPORT 4444;exploit'"},
        {"SSH隧道","ssh -L 8080:localhost:80 user@host"},
        {"Curl POST","curl -X POST -H 'Content-Type: application/json' -d '{}' {url}"},
        {"Git一键","git add . && git commit -m 'update' && git push"},
        {"tar打包","tar czf archive.tar.gz /path/to/dir"},
        {"find查找","find . -name '*.txt' -type f"},
        {"grep搜索","grep -r 'pattern' /path --include='*.py'"},
        {"sed替换","sed -i 's/old/new/g' file.txt"},
        {"awk提取","awk '{print $1,$3}' file.txt"},
        {"定时任务","* * * * * /path/to/script.sh"},
    };

    public TextTemplatesDialog(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"代码模板 (点击粘贴)");

        for(final String[] t:TEMPLATES){
            LinearLayout card=new LinearLayout(mCtx);card.setPadding(dp(10),dp(8),dp(10),dp(8));card.setBackgroundColor(0xFF2A2A3A);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);cp.bottomMargin=dp(4);card.setLayoutParams(cp);card.setOrientation(LinearLayout.VERTICAL);
            TextView name=new TextView(mCtx);name.setText(t[0]);name.setTextColor(0xFF48baf3);name.setTextSize(13);card.addView(name);
            TextView code=new TextView(mCtx);code.setText(t[1]);code.setTextColor(0xFF888888);code.setTextSize(10);code.setMaxLines(2);card.addView(code);
            card.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal(t[1]);Toast.makeText(mCtx,t[0]+" 已粘贴",Toast.LENGTH_SHORT).show();dismiss();
            }});r.addView(card);
        }
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.85));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
