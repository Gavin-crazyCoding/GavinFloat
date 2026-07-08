package com.termux.menu.ui.dialog;
import android.app.Dialog; import android.content.Context; import android.graphics.Color; import android.graphics.drawable.ColorDrawable;
import android.os.Build; import android.os.Handler; import android.os.Looper; import android.view.Gravity; import android.view.View; import android.view.ViewGroup;
import com.termux.menu.R;
import android.view.Window; import android.view.WindowManager; import android.widget.*; import com.termux.menu.termux.TermuxCommandHelper;
/** 端口扫描器 — 快速端口扫描GUI，比Nmap轻量 */
public class PortScannerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mHost, mPorts; private LinearLayout mResult;
    private Handler mH=new Handler(Looper.getMainLooper());
    private static final String[][] QUICK={
        {"常见端口","21,22,23,25,53,80,110,143,443,445,993,995,1433,1521,2049,3306,3389,5432,5900,6379,8080,8443,9090"},
        {"Web端口","80,443,8080,8443,9090,3000,5000,8000,8888"},
        {"数据库","3306,5432,1521,1433,27017,6379,9200"},
        {"远程管理","22,23,3389,5900,5800,4444"},
    };

    public PortScannerDialog(Context c){super(c,R.style.Theme_GavinFloat_Dialog);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("端口快速扫描");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setTypeface(null,android.graphics.Typeface.BOLD);t.setPadding(0,0,0,dp(6));r.addView(t);
        TextView sub=new TextView(mCtx);sub.setText("无需nmap，使用Termux nc/telnet快速检测");sub.setTextColor(0xFF888888);sub.setTextSize(11);sub.setPadding(0,0,0,dp(12));r.addView(sub);
        r.addView(label("目标主机:"));
        mHost=new EditText(mCtx);mHost.setText("127.0.0.1");mHost.setTextColor(0xFFFFFFFF);
        mHost.setHintTextColor(0xFF888888);mHost.setBackgroundColor(0x22FFFFFF);mHost.setPadding(dp(12),dp(8),dp(12),dp(8));mHost.setTextSize(14);r.addView(mHost);
        r.addView(label("端口(逗号分隔):"));
        mPorts=new EditText(mCtx);mPorts.setText("22,80,443,3306,8080");mPorts.setTextColor(0xFFFFFFFF);
        mPorts.setHintTextColor(0xFF888888);mPorts.setBackgroundColor(0x22FFFFFF);mPorts.setPadding(dp(12),dp(8),dp(12),dp(8));mPorts.setTextSize(14);r.addView(mPorts);
        TextView ql=new TextView(mCtx);ql.setText("快捷选择:");ql.setTextColor(0xFFD4AF37);ql.setTextSize(12);ql.setPadding(0,dp(8),0,dp(4));r.addView(ql);
        LinearLayout qg=new LinearLayout(mCtx);qg.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<QUICK.length;i+=2){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<2&&(i+j)<QUICK.length;j++){final String v=QUICK[i+j][1];final String n=QUICK[i+j][0];
                Button b=new Button(mCtx);b.setText(n);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(0xFF37474F);b.setTextSize(11);b.setPadding(dp(6),dp(4),dp(6),dp(4));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View view){mPorts.setText(v);}});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j<2?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }qg.addView(row);
        }r.addView(qg);
        Button scanBtn=new Button(mCtx);scanBtn.setText("开始扫描");scanBtn.setTextColor(0xFFFFFFFF);scanBtn.setBackgroundColor(0xFF4CAF50);
        scanBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View view){startScan();}});r.addView(scanBtn);
        mResult=new LinearLayout(mCtx);mResult.setOrientation(LinearLayout.VERTICAL);r.addView(mResult);
        sv.addView(r);setContentView(sv);Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.75));
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void startScan(){
        String host=mHost.getText().toString().trim(),ports=mPorts.getText().toString().trim();
        if(host.isEmpty()||ports.isEmpty()){Toast.makeText(mCtx,"输入主机和端口",Toast.LENGTH_SHORT).show();return;}
        mResult.removeAllViews();
        TextView loading=new TextView(mCtx);loading.setText("扫描中...");loading.setTextColor(0xFFFF9800);loading.setTextSize(13);mResult.addView(loading);
        String[] portArr=ports.split(",");
        for(final String p:portArr){final String port=p.trim();if(port.isEmpty())continue;
            mCmd.executeAndCapture("echo >/dev/tcp/"+host+"/"+port+" 2>&1 && echo OPEN || echo CLOSED",
            new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
                mH.post(new Runnable(){public void run(){
                    // 替换loading
                    if(mResult.getChildCount()>0 && ((TextView)mResult.getChildAt(0)).getText().toString().contains("扫描中"))
                        mResult.removeAllViews();
                    LinearLayout row=new LinearLayout(mCtx);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(dp(4),dp(4),dp(4),dp(4));
                    TextView pn=new TextView(mCtx);pn.setText("Port "+port);pn.setTextColor(0xFFFFFFFF);pn.setTextSize(13);pn.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));row.addView(pn);
                    String st=o.trim();boolean open=st.contains("OPEN");
                    TextView sts=new TextView(mCtx);sts.setText(open?"OPEN":"CLOSED");sts.setTextColor(open?0xFF4CAF50:0xFFF44336);sts.setTextSize(12);row.addView(sts);
                    mResult.addView(row);
                }});
            }});
        }
    }
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(8),0,dp(4));return tv;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
