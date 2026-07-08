package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.termux.menu.termux.TermuxCommandHelper;

/** 容器管理器 — 读取 ~/.termux/container/file* 配置并管理 proot-distro 容器 */
public class ContainerManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    private LinearLayout mList; private Handler mH=new Handler(Looper.getMainLooper());
    private static final String CONTAINER_DIR="/data/data/com.termux/files/home/.termux/container";

    public ContainerManagerDialog(Context c){super(c, com.termux.menu.R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"容器管理");

        Button ref=btn("刷新",0xFF2196F3);ref.setOnClickListener(new View.OnClickListener(){public void onClick(View v){loadContainers();}});r.addView(ref);
        mList=new LinearLayout(mCtx);mList.setOrientation(LinearLayout.VERTICAL);r.addView(mList);

        // proot-distro快捷
        r.addView(label("proot-distro快捷:"));
        String[][] cmds={{"列出所有","proot-distro list"},{"安装Ubuntu","proot-distro install ubuntu"},{"安装Debian","proot-distro install debian"},
            {"安装Arch","proot-distro install archlinux"},{"安装Fedora","proot-distro install fedora"},{"安装Kali","proot-distro install kali"},
            {"登录Ubuntu","proot-distro login ubuntu"},{"重置Ubuntu","proot-distro reset ubuntu"},{"删除Ubuntu","proot-distro remove ubuntu"}};
        LinearLayout grid=new LinearLayout(mCtx);grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<cmds.length;i+=3){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<3&&(i+j)<cmds.length;j++){final String cmd=cmds[i+j][1];final String label=cmds[i+j][0];
                Button b=btn(label,0xFF37474F);b.setTextSize(10);b.setPadding(dp(6),dp(4),dp(6),dp(4));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,label,Toast.LENGTH_SHORT).show();}});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j<2?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }grid.addView(row);
        }r.addView(grid);

        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.75));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
        loadContainers();
    }

    private void loadContainers(){
        mList.removeAllViews();
        // 读取容器配置目录下的 file, file1, file2...
        mCmd.executeAndCapture("ls "+CONTAINER_DIR+"/file* 2>/dev/null; echo '---'; proot-distro list 2>/dev/null",
        new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
            mH.post(new Runnable(){public void run(){mList.removeAllViews();
                String[] parts=o.split("---");String configFiles=parts.length>0?parts[0].trim():"";
                String prootList=parts.length>1?parts[1].trim():"";
                // 显示容器配置文件
                String[] files=configFiles.split("\n");boolean hasAny=false;
                for(final String f:files){String l=f.trim();if(l.isEmpty())continue;hasAny=true;
                    final String fname=l.substring(l.lastIndexOf('/')+1);
                    LinearLayout row=new LinearLayout(mCtx);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(4),dp(4),dp(4));
                    TextView nm=new TextView(mCtx);nm.setText("容器: "+fname);nm.setTextColor(0xFFFFFFFF);nm.setTextSize(13);
                    nm.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));row.addView(nm);
                    Button start=quickBtn("启动",0xFF4CAF50);start.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                        mCmd.sendCommandToTerminal("sh "+CONTAINER_DIR+"/"+fname);Toast.makeText(mCtx,"启动容器: "+fname,Toast.LENGTH_SHORT).show();
                    }});row.addView(start);
                    Button edit=quickBtn("编辑",0xFF2196F3);edit.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                        mCmd.sendCommandToTerminal("cat "+CONTAINER_DIR+"/"+fname);dismiss();
                    }});row.addView(edit);
                    mList.addView(row);
                }
                if(!hasAny){TextView e=new TextView(mCtx);e.setText("未找到容器配置(file*)\n使用proot-distro快捷命令创建");e.setTextColor(0xFF888888);e.setTextSize(12);e.setPadding(0,dp(8),0,dp(8));mList.addView(e);}
                // proot-distro列表
                if(!prootList.isEmpty()){TextView hdr=new TextView(mCtx);hdr.setText("proot-distro:");hdr.setTextColor(0xFFD4AF37);hdr.setTextSize(13);hdr.setPadding(0,dp(8),0,dp(4));mList.addView(hdr);
                    String[] distros=prootList.split("\n");
                    for(final String d:distros){String dl=d.trim();if(dl.isEmpty())continue;
                        LinearLayout row=new LinearLayout(mCtx);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(2),dp(4),dp(2));
                        TextView dn=new TextView(mCtx);dn.setText(dl);dn.setTextColor(0xFFFFFFFF);dn.setTextSize(12);
                        dn.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));row.addView(dn);
                        Button login=quickBtn("登录",0xFF4CAF50);login.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                            String name=dl.replaceAll("\\*.*","").trim();if(name.isEmpty())name="ubuntu";
                            mCmd.sendCommandToTerminal("proot-distro login "+name);Toast.makeText(mCtx,"proot-distro login "+name,Toast.LENGTH_SHORT).show();
                        }});row.addView(login);mList.addView(row);
                    }
                }
            }});
        }});
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(12);b.setPadding(dp(8),dp(6),dp(8),dp(6));return b;}
    private Button quickBtn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(c);b.setBackgroundColor(0x00000000);b.setTextSize(10);b.setPadding(dp(6),dp(2),dp(6),dp(2));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
