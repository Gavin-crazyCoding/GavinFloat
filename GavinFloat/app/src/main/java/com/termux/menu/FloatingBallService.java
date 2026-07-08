package com.termux.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.model.MenuCategoryData;
import com.termux.menu.model.MenuEntryData;
import com.termux.menu.model.XmlMenuGroup;
import com.termux.menu.model.XmlMenuItem;
import com.termux.menu.termux.TermuxCommandHelper;
import com.termux.menu.ui.EditorController;
import com.termux.menu.ui.FileBrowserController;
import com.termux.menu.ui.FluidRippleDrawable;
import com.termux.menu.ui.IconProvider;
import com.termux.menu.ui.MenuCategoryAdapter;
import com.termux.menu.ui.PageController;
import com.termux.menu.ui.SettingsController;
import com.termux.menu.ui.TerminalOutputController;
import com.termux.menu.ui.dialog.AdbManagerDialog;
import com.termux.menu.ui.dialog.AIChatDialog;
import com.termux.menu.ui.dialog.ApiConfigDialog;
import com.termux.menu.ui.dialog.BackupManagerDialog;
import com.termux.menu.ui.dialog.CamphishDialog;
import com.termux.menu.ui.dialog.CodeEditDialog;
import com.termux.menu.ui.dialog.ContainerManagerDialog;
import com.termux.menu.ui.dialog.CronManagerDialog;
import com.termux.menu.ui.dialog.DirbDialog;
import com.termux.menu.ui.dialog.DiskAnalyzerDialog;
import com.termux.menu.ui.dialog.DownloadCenterDialog;
import com.termux.menu.ui.dialog.GitManagerDialog;
import com.termux.menu.ui.dialog.HashToolkitDialog;
import com.termux.menu.ui.dialog.KaliServiceManager;
import com.termux.menu.ui.dialog.KaliToolsDialog;
import com.termux.menu.ui.dialog.MetasploitDialog;
import com.termux.menu.ui.dialog.NetworkToolsDialog;
import com.termux.menu.ui.dialog.NmapDialog;
import com.termux.menu.ui.dialog.OnlineScriptDialog;
import com.termux.menu.ui.dialog.PackageManagerDialog;
import com.termux.menu.ui.dialog.PortScannerDialog;
import com.termux.menu.ui.dialog.ProcessManagerDialog;
import com.termux.menu.ui.dialog.PythonManagerDialog;
import com.termux.menu.ui.dialog.QuickCommandsDialog;
import com.termux.menu.ui.dialog.ReverseShellDialog;
import com.termux.menu.ui.dialog.SeekerDialog;
import com.termux.menu.ui.dialog.SqlmapDialog;
import com.termux.menu.ui.dialog.SshManagerDialog;
import com.termux.menu.ui.dialog.SystemCleanerDialog;
import com.termux.menu.ui.dialog.SystemDashboardDialog;
import com.termux.menu.ui.dialog.TermuxContainerSwitchDialog;
import com.termux.menu.ui.dialog.TextTemplatesDialog;
import com.termux.menu.ui.dialog.ThemeConfigDialog;
import com.termux.menu.ui.dialog.VncConnectDialog;
import com.termux.menu.ui.dialog.WiFiToolsDialog;
import com.termux.menu.utils.FileUtils;
import com.termux.menu.utils.PrefsManager;
import com.termux.menu.xml.XmlMenuParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FloatingBallService extends Service {
    private static final String TAG = "GavinFloat";
    private static final String CHANNEL_ID = "gavinfloat_channel";
    private static final String EXTERNAL_MENU_PATH = FileUtils.TERMUX_HOME + "/ZtInfo/main_menu_path.xml";

    private WindowManager mWindowManager;
    private View mBallView;
    private View mMenuView;
    protected WindowManager.LayoutParams mBallParams;
    private WindowManager.LayoutParams mMenuParams;
    private boolean mMenuShowing = false;

    private TermuxCommandHelper mCmdHelper;
    private PrefsManager mPrefs;
    private MenuCategoryAdapter mCategoryAdapter;
    private PageController mPageController;
    private EditorController mEditorController;
    private FileBrowserController mFileBrowserController;
    private TerminalOutputController mTerminalController;
    private SettingsController mSettingsController;

    private TextView mServiceStatus;
    private View mInfoCard;
    private ImageView mIpExpandIcon;
    private TextView mIpStatus;
    private View mMenuPackageCard;
    private TextView mMenuPackageCurrent;
    private ImageView mMenuPackageExpand;
    private RecyclerView mMenuPackageList;
    private View mDataInfoCard;
    private ImageView mDataExpandIcon;
    private TextView mDataInfoContent;
    private AlertDialog mSelectionDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mCmdHelper = TermuxCommandHelper.getInstance(this);
        mPrefs = new PrefsManager(this);
        createNotificationChannel();
        startForeground(1, buildNotification());
        createBall();
        createMenuPanel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideMenu();
        if (mBallView != null && mBallView.isAttachedToWindow()) {
            mWindowManager.removeView(mBallView);
        }
        if (mCategoryAdapter != null) {
            mCategoryAdapter.release();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "GavinFloat", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("悬浮菜单服务");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(ch);
            }
        }
    }

    private Notification buildNotification() {
        Intent ni = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, ni, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent exitIntent = new Intent(this, ExitReceiver.class);
        exitIntent.setAction(ExitReceiver.ACTION_EXIT);
        PendingIntent exitPi = PendingIntent.getBroadcast(this, 1, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GavinFloat")
            .setContentText("悬浮菜单运行中")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "退出", exitPi)
            .setOngoing(true)
            .build();
    }

    private int getOverlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
    }

    // ===== Ball =====

    private void createBall() {
        mBallView = new View(this);
        int size = dp(mPrefs.getBallSize());
        GradientDrawable ballBg = new GradientDrawable();
        ballBg.setShape(GradientDrawable.OVAL);
        ballBg.setColors(new int[]{0xFF2C1810, 0xFF0D0221});
        ballBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        ballBg.setGradientRadius(size / 2f);
        ballBg.setStroke(dp(1), 0x80D4AF37);
        mBallView.setBackground(ballBg);
        mBallView.setElevation(dp(4));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBallView.setForeground(new FluidRippleDrawable(0xFFD4AF37));
        }
        mBallParams = new WindowManager.LayoutParams(size, size, getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        mBallParams.gravity = Gravity.TOP | Gravity.START;
        mBallParams.x = mPrefs.getBallX();
        mBallParams.y = dp(mPrefs.getBallY());
        mBallView.setOnTouchListener(new BallTouchListener());
        mWindowManager.addView(mBallView, mBallParams);
    }

    // ===== Menu Panel =====

    private void createMenuPanel() {
        mMenuView = LayoutInflater.from(this).inflate(R.layout.panel_floating, null);
        applySavedTheme(mMenuView);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int menuWidth = (int) (screenWidth * (mPrefs.getMenuWidth() / 100f));
        mMenuParams = new WindowManager.LayoutParams(menuWidth, WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayType(), WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        mMenuParams.gravity = Gravity.TOP | Gravity.START;
        mMenuParams.x = 0;
        mMenuParams.y = 0;
        initControllers();
        findCardViews();
        setupCardListeners();
        loadAndShowMenu();
        updateStatusIndicators();
        ScrollView sv = mMenuView.findViewById(R.id.page_menu);
        if (sv != null) {
            sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            sv.setVerticalScrollBarEnabled(true);
        }
    }

    private void initControllers() {
        mPageController = new PageController(mMenuView, new PageController.PageChangeListener() {
            public void onPageChanged(int page) {
                if (page == PageController.PAGE_MENU) {
                    updateStatusIndicators();
                }
            }
        });
        mPageController.setDefaultTitle("GavinFloat");
        mEditorController = new EditorController(mMenuView, mPageController);
        mFileBrowserController = new FileBrowserController(mMenuView, mPageController, mEditorController);
        mTerminalController = new TerminalOutputController(mMenuView, mCmdHelper, mPageController);
        mSettingsController = new SettingsController(mMenuView, FloatingBallService.this, mPrefs,
            mPageController, new SettingsController.SettingsChangeListener() {
                public void onSettingsChanged() {
                    hideMenu();
                    recreateBallAndMenu();
                }
            });
    }

    private void findCardViews() {
        mServiceStatus = mMenuView.findViewById(R.id.service_status);
        mIpStatus = mMenuView.findViewById(R.id.ip_status);
        mIpExpandIcon = mMenuView.findViewById(R.id.ip_expand_icon);
        mInfoCard = mMenuView.findViewById(R.id.info_card);
        mMenuPackageCard = mMenuView.findViewById(R.id.menu_package_card);
        mMenuPackageCurrent = mMenuView.findViewById(R.id.menu_package_current);
        mMenuPackageExpand = mMenuView.findViewById(R.id.menu_package_expand);
        mMenuPackageList = mMenuView.findViewById(R.id.menu_package_list);
        mDataInfoCard = mMenuView.findViewById(R.id.data_info_card);
        mDataExpandIcon = mMenuView.findViewById(R.id.data_expand_icon);
        mDataInfoContent = mMenuView.findViewById(R.id.data_info_content);
    }

    private void setupCardListeners() {
        View settingsBtn = mMenuView.findViewById(R.id.open_settings_btn);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSettingsController.show();
                }
            });
        }
        View ipCard = mMenuView.findViewById(R.id.ip_card);
        if (ipCard != null) {
            ipCard.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean showing = (mIpStatus != null && mIpStatus.getVisibility() == View.VISIBLE);
                    if (mIpStatus != null) {
                        mIpStatus.setVisibility(showing ? View.GONE : View.VISIBLE);
                    }
                    if (mInfoCard != null) {
                        mInfoCard.setVisibility(showing ? View.GONE : View.VISIBLE);
                    }
                    if (mIpExpandIcon != null) {
                        mIpExpandIcon.setRotation(showing ? 0 : 180);
                    }
                    if (!showing) {
                        loadIpAddress();
                    }
                }
            });
        }
        View qqGroup = mMenuView.findViewById(R.id.qq_group_tv);
        if (qqGroup != null) {
            qqGroup.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(FloatingBallService.this, "QQ群功能暂未开放", Toast.LENGTH_SHORT).show();
                }
            });
        }
        View tgGroup = mMenuView.findViewById(R.id.telegram_group_tv);
        if (tgGroup != null) {
            tgGroup.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(FloatingBallService.this, "Telegram群功能暂未开放", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (mMenuPackageCard != null) {
            mMenuPackageCard.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean showing = mMenuPackageList.getVisibility() == View.VISIBLE;
                    mMenuPackageList.setVisibility(showing ? View.GONE : View.VISIBLE);
                    if (mMenuPackageExpand != null) {
                        mMenuPackageExpand.setRotation(showing ? 0 : 180);
                    }
                    if (!showing) {
                        loadMenuPackageList();
                    }
                }
            });
        }
        View dataCard = mMenuView.findViewById(R.id.data_card);
        if (dataCard != null) {
            dataCard.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean showing = mDataInfoCard.getVisibility() == View.VISIBLE;
                    mDataInfoCard.setVisibility(showing ? View.GONE : View.VISIBLE);
                    if (mDataExpandIcon != null) {
                        mDataExpandIcon.setRotation(showing ? 0 : 180);
                    }
                    if (!showing) {
                        loadDataInfo();
                    }
                }
            });
        }
    }

    private void recreateBallAndMenu() {
        if (mBallView != null && mBallView.isAttachedToWindow()) {
            mWindowManager.removeView(mBallView);
        }
        if (mMenuShowing) {
            try {
                mWindowManager.removeView(mMenuView);
            } catch (Exception ignored) {
            }
            mMenuShowing = false;
        }
        createBall();
        createMenuPanel();
    }

    // ===== Menu Loading =====

    private void loadAndShowMenu() {
        RecyclerView menuList = mMenuView.findViewById(R.id.menu_list);
        if (menuList == null) return;
        menuList.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<MenuCategoryData> categories = loadMenuData();
        if (mCategoryAdapter != null) {
            mCategoryAdapter.release();
        }
        mCategoryAdapter = new MenuCategoryAdapter(this, categories);
        menuList.setAdapter(mCategoryAdapter);
        TextView errorMsg = mMenuView.findViewById(R.id.menu_error);
        if (errorMsg != null) {
            if (categories.isEmpty()) {
                errorMsg.setVisibility(View.VISIBLE);
                errorMsg.setText("菜单为空 — 检查 default_menu.xml 或外部配置");
            } else {
                errorMsg.setVisibility(View.GONE);
            }
        }
    }

    private void updateStatusIndicators() {
        boolean installed = mCmdHelper.isTermuxInstalled();
        if (mServiceStatus != null) {
            mServiceStatus.setText(installed ? "已连接" : "未连接");
            mServiceStatus.setTextColor(installed ? 0xFF4CAF50 : 0xFFF44336);
        }
    }

    // ===== Menu Data =====

    private ArrayList<MenuCategoryData> loadMenuData() {
        List<XmlMenuGroup> groups = null;
        try {
            InputStream is = getAssets().open("default_menu.xml");
            groups = XmlMenuParser.parseFromStream(is);
            is.close();
            Log.i(TAG, "Loaded assets menu: " + (groups != null ? groups.size() : 0));
            if (mMenuPackageCurrent != null) {
                mMenuPackageCurrent.setText("当前: 内置菜单");
            }
        } catch (Exception e) {
            Log.e(TAG, "assets menu error: " + e.getMessage());
        }
        File extFile = new File(EXTERNAL_MENU_PATH);
        if (extFile.exists() && extFile.length() > 0) {
            List<XmlMenuGroup> extGroups = XmlMenuParser.parseFromFile(extFile);
            if (extGroups != null && !extGroups.isEmpty()) {
                groups = extGroups;
                Log.i(TAG, "External XML overrides: " + extGroups.size());
                if (mMenuPackageCurrent != null) {
                    mMenuPackageCurrent.setText("当前: 外部配置菜单");
                }
            }
        }
        if (groups == null || groups.isEmpty()) {
            groups = buildFallbackMenu();
            Log.i(TAG, "Using hardcoded fallback");
            if (mMenuPackageCurrent != null) {
                mMenuPackageCurrent.setText("当前: 兜底菜单");
            }
        }
        ArrayList<MenuCategoryData> categories = new ArrayList<>();
        for (XmlMenuGroup group : groups) {
            ArrayList<MenuEntryData> entries = new ArrayList<>();
            for (XmlMenuItem item : group.getItems()) {
                entries.add(buildEntry(item));
            }
            if (!entries.isEmpty()) {
                categories.add(new MenuCategoryData(group.getGroupName(), group.getId(), entries));
            }
        }
        return categories;
    }

    private MenuEntryData buildEntry(XmlMenuItem item) {
        return new MenuEntryData(item.getName(), resolveIcon(item.getName()), item, new MenuEntryData.ClickHandler() {
            public void onClick(Context ctx) {
                handleClick(item);
            }
        });
    }

    private android.graphics.drawable.Drawable resolveIcon(String name) {
        if (name == null) return IconProvider.getIcon(null);
        int resId = mapIconResource(name);
        if (resId != 0) {
            try {
                android.graphics.drawable.Drawable d = getResources().getDrawable(resId);
                if (d != null) return d;
            } catch (Throwable ignored) {
            }
        }
        return IconProvider.getIcon(name);
    }

    private int mapIconResource(String name) {
        if (name == null) return 0;
        if (name.equals("切换源")) return R.mipmap.qinghua_ico;
        if (name.contains("容器切换") || name.contains("Termux容器")) return R.mipmap.rongqi_ico;
        if (name.contains("备份")) return R.mipmap.beifen_ico;
        if (name.contains("MOE")) return R.mipmap.moe_ico;
        if (name.contains("发行版") || name.contains("Linux")) return R.mipmap.linux_ico;
        if (name.equals("QEMU") || name.contains("QEMU")) return R.mipmap.qemu_ico;
        if (name.equals("定时任务") || name.contains("Cron") || name.contains("cron")) return R.mipmap.timer;
        if (name.contains("Gavin设置") || name.contains("设置")) return R.mipmap.settings;
        if (name.contains("C项目")) return R.drawable.ic_project_c;
        if (name.contains("Java项目")) return R.drawable.ic_project_java;
        if (name.contains("Python项目")) return R.drawable.ic_project_python;
        if (name.contains("PHP项目")) return R.drawable.ic_project_php;
        if (name.contains("NPM项目") || name.contains("npm")) return R.drawable.ic_project_npm;
        if (name.contains("X11")) return R.mipmap.termux_x11;
        if (name.equals("Nmap") || name.contains("Nmap扫描")) return R.mipmap.install_apk;
        if (name.equals("Dirb")) return R.mipmap.filebrowser_ico;
        if (name.equals("Metasploit")) return R.mipmap.mingl_ico;
        if (name.equals("Sqlmap")) return R.mipmap.waring;
        if (name.equals("Seeker")) return R.mipmap.online_sh;
        if (name.equals("CamPhish")) return R.mipmap.duanxin_ico;
        if (name.contains("WiFi") || name.contains("wifi")) return R.mipmap.http;
        if (name.contains("Kali换源")) return R.mipmap.code_view;
        if (name.contains("Kali工具") || name.contains("服务管理")) return R.mipmap.install_module;
        if (name.contains("Hash") || name.contains("hash")) return R.mipmap.waring;
        if (name.contains("反弹Shell")) return R.mipmap.online_sh;
        if (name.contains("SSH管理器") || name.contains("远程")) return R.mipmap.yc_connect;
        if (name.contains("Git管理器") || name.contains("git")) return R.mipmap.github;
        if (name.contains("系统仪表盘")) return R.mipmap.data_msg;
        if (name.contains("系统清理") || name.contains("清理")) return R.mipmap.chongzhi_ico;
        if (name.contains("代码编辑器") || name.contains("editor") || name.contains("代码")) return R.mipmap.code_view;
        if (name.contains("进程管理") || name.contains("进程")) return R.mipmap.run_ico;
        if (name.contains("网络工具") || name.contains("网络") || name.contains("IP")) return R.mipmap.http;
        if (name.contains("磁盘") || name.contains("disk")) return R.mipmap.dsk;
        if (name.contains("内存") || name.contains("mem")) return R.mipmap.run_icooo;
        if (name.contains("AI助手") || name.contains("AI")) return R.mipmap.deepseek;
        if (name.contains("ADB") || name.contains("adb")) return R.mipmap.adb_shell;
        if (name.contains("VNC") || name.contains("vnc")) return R.mipmap.vnc_ico;
        if (name.contains("包管理器") || name.contains("包")) return R.mipmap.apk_img;
        if (name.contains("下载") || name.contains("脚本")) return R.mipmap.download;
        if (name.contains("备份")) return R.mipmap.beifen_ico;
        if (name.contains("Python") || name.contains("pip")) return R.mipmap.install_eg;
        if (name.contains("模板") || name.contains("代码") || name.contains("template")) return R.mipmap.code_view;
        if (name.contains("Docker") || name.contains("docker")) return R.mipmap.docker;
        if (name.contains("论坛")) return R.mipmap.bbs_zero;
        if (name.contains("仓库")) return R.mipmap.gongongcangku;
        if (name.contains("FTP")) return R.mipmap.ftp_web;
        if (name.contains("开源")) return R.mipmap.github;
        if (name.contains("语言")) return R.mipmap.yuyan_ico;
        if (name.contains("CPU") || name.contains("系统") || name.contains("系统信息")) return R.mipmap.caozuo_lll;
        if (name.contains("雪花")) return R.mipmap.xuehua_ico;
        if (name.contains("粒子")) return R.mipmap.particle;
        if (name.contains("清除")) return R.mipmap.clear_style;
        if (name.contains("悬浮")) return R.mipmap.xuanfu_window;
        if (name.contains("字体")) return R.mipmap.ziti_font_ico;
        if (name.contains("全屏")) return R.mipmap.quanping_ico;
        if (name.contains("美化")) return R.mipmap.meihua_all;
        return 0;
    }

    // ===== Fallback Menu =====

    private List<XmlMenuGroup> buildFallbackMenu() {
        List<XmlMenuGroup> groups = new ArrayList<>();
        XmlMenuGroup common = new XmlMenuGroup("常用功能", 0);
        common.addItem(menuItem("切换源", "ztShell:termux-change-repo", true));
        common.addItem(menuItem("Gavin设置", "internal:settings", false));
        groups.add(common);
        XmlMenuGroup sys = new XmlMenuGroup("系统工具", 9);
        sys.addItem(menuItem("系统信息", "shell_output:uname -a", true));
        sys.addItem(menuItem("进程管理", "internal:process_manager", false));
        groups.add(sys);
        return groups;
    }

    private static XmlMenuItem menuItem(String name, String action, boolean autoRun) {
        return new XmlMenuItem(name, action, "", autoRun, "", false, "", "", "", "", "");
    }

    // ===== Click Dispatch =====

    private void handleClick(XmlMenuItem item) {
        String action = item.getClickAction();
        if (TextUtils.isEmpty(action)) return;
        if (action.startsWith("ztShell:")) {
            handleShell(action.substring("ztShell:".length()).trim(), item);
        } else if (action.startsWith("shell_output:")) {
            handleShellOutput(action.substring("shell_output:".length()).trim(), item);
        } else if (action.startsWith("editor:")) {
            handleEditor(action.substring("editor:".length()).trim());
        } else if (action.startsWith("filebrowser:")) {
            handleFileBrowser(action.substring("filebrowser:".length()).trim());
        } else if (action.startsWith("internal:")) {
            handleInternal(action.substring("internal:".length()).trim());
        } else if (action.startsWith("jumpUrl:") || action.startsWith("downloadUrl:") || action.startsWith("appWebUrl:")) {
            handleUrl(action.substring(action.indexOf(":") + 1).trim());
        } else if (action.startsWith("commands:")) {
            handleCommands(action.substring("commands:".length()).trim(), item);
        } else if (action.startsWith("shellUrl:")) {
            mTerminalController.executeAndShow("curl -s '" + action.substring("shellUrl:".length()).trim() + "' | bash");
            hideMenu();
        } else if (action.startsWith("input:")) {
            handleInput(action.substring("input:".length()).trim(), item);
        } else if (action.startsWith("startActivity:") || action.startsWith("actionActivity:")) {
            Toast.makeText(this, "暂不支持启动Activity", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShell(String shell, XmlMenuItem item) {
        if (shell.isEmpty()) return;
        if (item.isDialogConfirm()) {
            hideMenu();
            String title = !TextUtils.isEmpty(item.getDialogTitle()) ? item.getDialogTitle() : "确认";
            String msg = !TextUtils.isEmpty(item.getDialogMessage()) ? item.getDialogMessage() : "确认执行此命令？";
            showConfirmDialog(title, msg, new Runnable() {
                public void run() {
                    mCmdHelper.sendCommandToTerminal(shell);
                    Toast.makeText(FloatingBallService.this, "命令已发送", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mCmdHelper.sendCommandToTerminal(item.isAutoRunShell() ? shell + "\n" : shell);
            Toast.makeText(this, "命令已发送", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShellOutput(String command, XmlMenuItem item) {
        if (command.isEmpty()) return;
        if (item.isDialogConfirm()) {
            String title = !TextUtils.isEmpty(item.getDialogTitle()) ? item.getDialogTitle() : "确认";
            String msg = !TextUtils.isEmpty(item.getDialogMessage()) ? item.getDialogMessage() : "确认执行？";
            showConfirmDialog(title, msg, new Runnable() {
                public void run() {
                    mTerminalController.executeAndShow(command);
                }
            });
        } else {
            mTerminalController.executeAndShow(command);
        }
    }

    private void handleEditor(String path) {
        if (path.isEmpty()) path = FileUtils.TERMUX_HOME;
        hideMenu();
        safeShow(new CodeEditDialog(FloatingBallService.this, path));
    }

    private void handleFileBrowser(String path) {
        if (path.isEmpty()) path = FileUtils.TERMUX_HOME;
        mFileBrowserController.openDirectory(path);
    }

    private void handleInternal(String action) {
        switch (action) {
            case "settings":
                mSettingsController.show();
                break;
            case "editor":
                mEditorController.openNewFile(FileUtils.TERMUX_HOME + "/untitled.txt", "");
                break;
            case "filebrowser":
                mFileBrowserController.openDirectory(FileUtils.TERMUX_HOME);
                break;
            case "language":
                Toast.makeText(this, "语言切换功能暂未实现", Toast.LENGTH_SHORT).show();
                break;
            case "download_center": hideMenu(); safeShow(new DownloadCenterDialog(FloatingBallService.this)); break;
            case "online_scripts": hideMenu(); safeShow(new OnlineScriptDialog(FloatingBallService.this)); break;
            case "nmap": hideMenu(); safeShow(new NmapDialog(FloatingBallService.this)); break;
            case "kali_tools": hideMenu(); safeShow(new KaliToolsDialog(FloatingBallService.this)); break;
            case "sqlmap": hideMenu(); safeShow(new SqlmapDialog(FloatingBallService.this)); break;
            case "metasploit": hideMenu(); safeShow(new MetasploitDialog(FloatingBallService.this)); break;
            case "dirb": hideMenu(); safeShow(new DirbDialog(FloatingBallService.this)); break;
            case "seeker": hideMenu(); safeShow(new SeekerDialog(FloatingBallService.this)); break;
            case "camphish": hideMenu(); safeShow(new CamphishDialog(FloatingBallService.this)); break;
            case "api_config": hideMenu(); safeShow(new ApiConfigDialog(FloatingBallService.this)); break;
            case "pkg_manager": hideMenu(); safeShow(new PackageManagerDialog(FloatingBallService.this)); break;
            case "wifi_tools": hideMenu(); safeShow(new WiFiToolsDialog(FloatingBallService.this)); break;
            case "process_manager": hideMenu(); safeShow(new ProcessManagerDialog(FloatingBallService.this)); break;
            case "network_tools": hideMenu(); safeShow(new NetworkToolsDialog(FloatingBallService.this)); break;
            case "quick_commands": hideMenu(); safeShow(new QuickCommandsDialog(FloatingBallService.this)); break;
            case "adb_manager": hideMenu(); safeShow(new AdbManagerDialog(FloatingBallService.this)); break;
            case "ai_chat": hideMenu(); safeShow(new AIChatDialog(FloatingBallService.this)); break;
            case "git_manager": hideMenu(); safeShow(new GitManagerDialog(FloatingBallService.this)); break;
            case "sys_dashboard": hideMenu(); safeShow(new SystemDashboardDialog(FloatingBallService.this)); break;
            case "ssh_manager": hideMenu(); safeShow(new SshManagerDialog(FloatingBallService.this)); break;
            case "vnc_connect": hideMenu(); safeShow(new VncConnectDialog(FloatingBallService.this)); break;
            case "kali_services": hideMenu(); safeShow(new KaliServiceManager(FloatingBallService.this)); break;
            case "hash_toolkit": hideMenu(); safeShow(new HashToolkitDialog(FloatingBallService.this)); break;
            case "disk_analyzer": hideMenu(); safeShow(new DiskAnalyzerDialog(FloatingBallService.this)); break;
            case "text_templates": hideMenu(); safeShow(new TextTemplatesDialog(FloatingBallService.this)); break;
            case "backup_manager": hideMenu(); safeShow(new BackupManagerDialog(FloatingBallService.this)); break;
            case "python_manager": hideMenu(); safeShow(new PythonManagerDialog(FloatingBallService.this)); break;
            case "container_manager": hideMenu(); safeShow(new ContainerManagerDialog(FloatingBallService.this)); break;
            case "cron_manager": hideMenu(); safeShow(new CronManagerDialog(FloatingBallService.this)); break;
            case "reverse_shell": hideMenu(); safeShow(new ReverseShellDialog(FloatingBallService.this)); break;
            case "port_scanner": hideMenu(); safeShow(new PortScannerDialog(FloatingBallService.this)); break;
            case "system_cleaner": hideMenu(); safeShow(new SystemCleanerDialog(FloatingBallService.this)); break;
            case "install_kali": installKali(); break;
            case "run_moe_script": runAssetScript("scripts/moe.sh", "moe.sh"); break;
            case "run_linux_script": runAssetScript("scripts/linux_install.sh", "linux_install.sh"); break;
            case "run_qemu_script": runAssetScript("scripts/qemu_helper.sh", "qemu.sh"); break;
            case "termux_container_switch":
                hideMenu();
                try {
                    new TermuxContainerSwitchDialog(FloatingBallService.this).show();
                } catch (Throwable t) {
                    Log.e(TAG, "容器切换失败: " + t.getMessage(), t);
                    Toast.makeText(FloatingBallService.this, "容器切换失败", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                Toast.makeText(this, "未知操作: " + action, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleUrl(String url) {
        hideMenu();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleInput(String spec, XmlMenuItem item) {
        String[] parts = spec.split("@@");
        String title = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "输入";
        String hint = parts.length > 1 ? parts[1] : "请输入";
        String shellTemplate = parts.length > 2 ? parts[2] : "";
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(hint);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF888888);
        input.setBackgroundColor(0x22FFFFFF);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        hideMenu();
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_GavinFloat_Dialog)
            .setTitle(title).setView(input)
            .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    String val = input.getText().toString().trim();
                    if (val.isEmpty()) {
                        Toast.makeText(FloatingBallService.this, "输入为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String cmd = shellTemplate.replace("{}", val);
                    if (item.isDialogConfirm()) {
                        showConfirmDialog("确认执行", "即将执行: " + cmd, new Runnable() {
                            public void run() {
                                mCmdHelper.sendCommandToTerminal(cmd);
                            }
                        });
                    } else {
                        mCmdHelper.sendCommandToTerminal(item.isAutoRunShell() ? cmd + "\n" : cmd);
                        Toast.makeText(FloatingBallService.this, "已发送", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    d.dismiss();
                }
            })
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    // ===== Commands / Selection / Dialogs =====

    private void showSelectionList(String title, final String[] titles, final String[] values) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < titles.length; i++) {
            final int idx = i;
            TextView tv = new TextView(this);
            tv.setText(titles[i]);
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(16);
            tv.setPadding(dp(20), dp(14), dp(20), dp(14));
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (mSelectionDialog != null) mSelectionDialog.dismiss();
                    } catch (Throwable ignored) {
                    }
                    String val = values[idx];
                    if (val.startsWith("shell_output:")) {
                        mTerminalController.executeAndShow(val.substring("shell_output:".length()));
                    } else if (val.startsWith("ztShell:")) {
                        mCmdHelper.sendCommandToTerminal(val.substring("ztShell:".length()));
                    } else if (val.startsWith("internal:")) {
                        handleInternal(val.substring("internal:".length()));
                    } else if (val.startsWith("editor:")) {
                        handleEditor(val.substring("editor:".length()));
                    } else if (val.startsWith("jumpUrl:")) {
                        handleUrl(val.substring("jumpUrl:".length()));
                    } else if (val.startsWith("filebrowser:")) {
                        handleFileBrowser(val.substring("filebrowser:".length()));
                    } else {
                        mCmdHelper.sendCommandToTerminal(val);
                    }
                }
            });
            ll.addView(tv);
            if (i < titles.length - 1) {
                View line = new View(this);
                line.setBackgroundColor(0x22FFFFFF);
                line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                ll.addView(line);
            }
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(ll);
        mSelectionDialog = new AlertDialog.Builder(this, R.style.Theme_GavinFloat_Dialog)
            .setTitle(title).setView(sv)
            .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    d.dismiss();
                }
            })
            .create();
        applyDialogType(mSelectionDialog);
        mSelectionDialog.show();
    }

    private void handleCommands(String commands, XmlMenuItem item) {
        try {
            String[] parts = commands.split(",");
            String[] titles = new String[parts.length];
            String[] values = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String[] pair = parts[i].split("@@", 2);
                titles[i] = pair[0].trim();
                values[i] = pair.length > 1 ? pair[1].trim() : "";
            }
            hideMenu();
            String title = !TextUtils.isEmpty(item.getListTitle()) ? item.getListTitle() : "选择";
            showSelectionList(title, titles, values);
        } catch (Exception e) {
            Log.e(TAG, "handleCommands error: " + e.getMessage(), e);
            Toast.makeText(this, "命令解析失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showConfirmDialog(String title, String msg, final Runnable onOk) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_GavinFloat_Dialog)
                .setTitle(title).setMessage(msg)
                .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) {
                        onOk.run();
                    }
                })
                .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) {
                        d.dismiss();
                    }
                })
                .create();
            if (dialog.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }
            }
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Dialog error: " + e.getMessage());
            Toast.makeText(this, title + ": " + msg, Toast.LENGTH_LONG).show();
            onOk.run();
        }
    }

    private void applyDialogType(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }

    // ===== Data Loading =====

    private void runAssetScript(final String assetPath, final String outputName) {
        hideMenu();
        Toast.makeText(this, "正在提取脚本...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    java.io.InputStream is = getAssets().open(assetPath);
                    java.io.File outFile = new java.io.File(FileUtils.TERMUX_HOME + "/" + outputName);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                    is.close();
                    fos.close();
                    new android.os.Handler(getMainLooper()).post(new Runnable() {
                        public void run() {
                            mCmdHelper.sendCommandToTerminal("cd ~ && chmod +x " + outputName + " && ./" + outputName);
                            Toast.makeText(FloatingBallService.this, "脚本已启动: " + outputName, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final java.io.IOException e) {
                    new android.os.Handler(getMainLooper()).post(new Runnable() {
                        public void run() {
                            Toast.makeText(FloatingBallService.this, "脚本失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void installKali() {
        hideMenu();
        Toast.makeText(this, "正在准备Kali安装脚本...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    java.io.InputStream is = getAssets().open("kali/kali.sh");
                    java.io.File outFile = new java.io.File(FileUtils.TERMUX_HOME + "/kali.sh");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                    is.close();
                    fos.close();
                    java.io.InputStream is2 = getAssets().open("kali/nh.sh");
                    java.io.File nhFile = new java.io.File(FileUtils.TERMUX_HOME + "/nh.sh");
                    java.io.FileOutputStream fos2 = new java.io.FileOutputStream(nhFile);
                    byte[] buf2 = new byte[8192];
                    int n2;
                    while ((n2 = is2.read(buf2)) != -1) fos2.write(buf2, 0, n2);
                    is2.close();
                    fos2.close();
                    new android.os.Handler(getMainLooper()).post(new Runnable() {
                        public void run() {
                            mCmdHelper.sendCommandToTerminal("cd ~ && chmod +x kali.sh && ./kali.sh");
                            Toast.makeText(FloatingBallService.this, "Kali安装脚本已启动！请切换到Termux", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final java.io.IOException e) {
                    new android.os.Handler(getMainLooper()).post(new Runnable() {
                        public void run() {
                            Toast.makeText(FloatingBallService.this, "安装失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void applySavedTheme(View panel) {
        android.content.SharedPreferences p = getSharedPreferences("gavinfloat_theme", MODE_PRIVATE);
        int accent = p.getInt("accent_color", 0xFFD4AF37);
        int bg = p.getInt("bg_color", 0xFF0D0221);
        panel.setBackgroundColor(bg);
        TextView title = panel.findViewById(R.id.header_title);
        if (title != null) title.setTextColor(accent);
        applyToAllCardViews(panel, bg, accent);
        if (mBallView != null) {
            GradientDrawable ballBg = new GradientDrawable();
            ballBg.setShape(GradientDrawable.OVAL);
            ballBg.setColors(new int[]{accent, bg});
            ballBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            ballBg.setGradientRadius(dp(mPrefs.getBallSize()) / 2f);
            ballBg.setStroke(dp(1), 0x80D4AF37);
            mBallView.setBackground(ballBg);
        }
    }

    private void applyToAllCardViews(View parent, int bg, int accent) {
        if (parent instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) parent).setCardBackgroundColor((0x18 << 24) | (accent & 0x00FFFFFF));
            return;
        }
        if (parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyToAllCardViews(vg.getChildAt(i), bg, accent);
            }
        }
    }

    private void createAndOpenProject(String subdir, String mainFile, String content) {
        String base = FileUtils.TERMUX_HOME + "/projects/" + subdir;
        java.io.File mainFileObj = new java.io.File(base, mainFile);
        String mainPath = mainFileObj.getAbsolutePath();
        java.io.File parentDir = mainFileObj.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        boolean ok = FileUtils.writeFile(mainPath, content);
        if (ok) {
            Toast.makeText(this, "项目已创建，打开编辑器...", Toast.LENGTH_SHORT).show();
            hideMenu();
            new CodeEditDialog(FloatingBallService.this, mainPath).show();
        } else {
            Toast.makeText(this, "创建失败: " + mainPath, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadIpAddress() {
        if (mIpStatus == null) return;
        mIpStatus.setText("获取中...");
        mCmdHelper.executeAndCapture(
            "(ifconfig 2>/dev/null | grep 'inet ' | grep -v 127.0.0.1 | awk '{print $2}' | head -3; " +
            "ip addr show 2>/dev/null | grep 'inet ' | grep -v 127.0.0.1 | awk '{print $2}' | head -3; " +
            "getprop dhcp.wlan0.ipaddress 2>/dev/null) 2>/dev/null | head -3",
            new TermuxCommandHelper.OutputCallback() {
                public void onOutput(String output) {
                    final String s = output.trim();
                    mIpStatus.post(new Runnable() {
                        public void run() {
                            mIpStatus.setText(s.isEmpty() ? "无网络连接" : s);
                        }
                    });
                }
            });
    }

    private void loadDataInfo() {
        if (mDataInfoContent == null) return;
        mDataInfoContent.setText("加载中...");
        mCmdHelper.executeAndCapture(
            "echo '=== Termux路径 ===' && echo '/data/data/com.termux/files' && " +
            "echo '' && echo '=== Home目录 ===' && du -sh ~/ 2>/dev/null && " +
            "echo '' && echo '=== 磁盘使用 ===' && df -h /data 2>/dev/null | tail -1 && " +
            "echo '' && echo '=== 内核版本 ===' && uname -r 2>/dev/null",
            new TermuxCommandHelper.OutputCallback() {
                public void onOutput(String output) {
                    mDataInfoContent.post(new Runnable() {
                        public void run() {
                            mDataInfoContent.setText(output.trim());
                        }
                    });
                }
            });
    }

    private void loadMenuPackageList() {
        String[] options = {"刷新当前菜单", "网络更新菜单", "重置默认菜单"};
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_GavinFloat_Dialog)
            .setTitle("菜单管理")
            .setItems(options, new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    switch (w) {
                        case 0: loadAndShowMenu(); Toast.makeText(FloatingBallService.this, "菜单已刷新", Toast.LENGTH_SHORT).show(); break;
                        case 1: downloadMenuFromNetwork(); break;
                        case 2: resetDefaultMenu(); break;
                    }
                    if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
                }
            })
            .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int w) {
                    if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
                    d.dismiss();
                }
            })
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private void downloadMenuFromNetwork() {
        Toast.makeText(this, "正在下载菜单...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {
                java.net.HttpURLConnection conn = null;
                try {
                    java.net.URL url = new java.net.URL("https://od.ixcmstudio.cn/repository/main/menu/zt_menu_config.xml");
                    conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "GavinFloat/1.0");
                    conn.connect();
                    if (conn.getResponseCode() == 200) {
                        java.io.InputStream is = conn.getInputStream();
                        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                        is.close();
                        String xml = bos.toString("UTF-8");
                        new java.io.File(EXTERNAL_MENU_PATH).getParentFile().mkdirs();
                        java.io.FileWriter fw = new java.io.FileWriter(EXTERNAL_MENU_PATH);
                        fw.write(xml);
                        fw.close();
                        new android.os.Handler(getMainLooper()).post(new Runnable() {
                            public void run() {
                                Toast.makeText(FloatingBallService.this, "菜单下载成功！", Toast.LENGTH_SHORT).show();
                                mMenuPackageCurrent.setText("当前: 网络更新菜单");
                                loadAndShowMenu();
                                if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        showToastOnMain("下载失败: HTTP " + conn.getResponseCode());
                    }
                } catch (Exception e) {
                    showToastOnMain("下载失败: " + e.getMessage());
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }).start();
    }

    private void resetDefaultMenu() {
        try {
            new java.io.File(EXTERNAL_MENU_PATH).delete();
            java.io.InputStream is = getAssets().open("default_menu.xml");
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            is.close();
            new java.io.File(EXTERNAL_MENU_PATH).getParentFile().mkdirs();
            java.io.FileWriter fw = new java.io.FileWriter(EXTERNAL_MENU_PATH);
            fw.write(bos.toString("UTF-8"));
            fw.close();
            mMenuPackageCurrent.setText("当前: 内置菜单");
            loadAndShowMenu();
            if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
            Toast.makeText(this, "已重置为默认菜单", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "重置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void safeShow(android.app.Dialog dialog) {
        try {
            dialog.show();
        } catch (Throwable t) {
            Log.e(TAG, "Dialog crash: " + t.getMessage(), t);
            Toast.makeText(this, "打开失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showToastOnMain(final String msg) {
        new android.os.Handler(getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(FloatingBallService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Menu Show/Hide =====

    private void toggleMenu() {
        if (mMenuShowing) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void showMenu() {
        if (mMenuShowing) return;
        updateStatusIndicators();
        mPageController.showPage(PageController.PAGE_MENU);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int ballCenterX = mBallParams.x + dp(mPrefs.getBallSize() / 2);
        int menuWidth = (int) (screenWidth * (mPrefs.getMenuWidth() / 100f));
        int targetX;
        if (ballCenterX < screenWidth / 2) {
            targetX = dp(mPrefs.getBallSize() + 5);
        } else {
            targetX = screenWidth - menuWidth - dp(mPrefs.getBallSize() + 5);
        }
        mMenuParams.x = targetX;
        mMenuParams.y = 0;
        mMenuView.setAlpha(0.6f);
        mWindowManager.addView(mMenuView, mMenuParams);
        mMenuShowing = true;
        mMenuView.animate().alpha(1f).setDuration(180).start();
    }

    private void hideMenu() {
        if (!mMenuShowing) return;
        try {
            mWindowManager.removeView(mMenuView);
        } catch (Exception ignored) {
        }
        mMenuShowing = false;
        mPrefs.setBallX(mBallParams.x);
        mPrefs.setBallY(mBallParams.y);
    }

    // ===== Ball Touch =====

    private class BallTouchListener implements View.OnTouchListener {
        private float startX, startY;
        private float initialX, initialY;
        private boolean moved = false;

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    initialX = mBallParams.x;
                    initialY = mBallParams.y;
                    moved = false;
                    mBallView.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).start();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - startX;
                    float dy = event.getRawY() - startY;
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        moved = true;
                        mBallParams.x = (int) (initialX + dx);
                        mBallParams.y = (int) (initialY + dy);
                        mWindowManager.updateViewLayout(mBallView, mBallParams);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    mBallView.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    if (!moved) toggleMenu();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    mBallView.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    return true;
            }
            return false;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
