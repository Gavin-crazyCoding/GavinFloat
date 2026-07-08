# GavinFloat

> 悬浮球图形化管理工具 — 基于 Termux 的高级侧边栏控制面板

[![Android](https://img.shields.io/badge/Android-6.0%2B-green)](https://developer.android.com)
[![AIDE](https://img.shields.io/badge/AIDE-Compatible-blue)](https://aide-ide.com)
[![Java](https://img.shields.io/badge/Java-100%25-orange)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

---

## 简介

**GavinFloat** 是一个 Android 悬浮球侧边栏应用，运行在 WindowManager 叠加层中。与 Termux 共享 `com.termux` 用户 ID，可直接操作 Termux 文件系统和发送命令到终端。

提供 **35+ 图形化工具弹窗**，覆盖渗透测试、系统管理、文件编辑、网络工具、AI 对话、ADB 控制等场景。

---

## 功能特性

### 📋 菜单系统
| 分组 | 功能 |
|------|------|
| **常用功能** | 切换源、容器切换、备份恢复、包管理器、进程管理、网络工具、端口扫描、ADB 管理器、系统清理 |
| **创建项目** | C/Java/Python/PHP/NPM 项目模板、代码编辑器 |
| **Kali Nethunter** | 安装、终端、桌面 GUI(KeX)、SSH、换源、工具集、服务管理、Hash 工具包、反弹 Shell 生成器 |
| **系统工具** | 系统仪表盘、CPU/内存/磁盘监控、环境变量、网络测试 |
| **开发工具** | 代码编辑器(21种语言高亮)、代码模板、备份管理、Python/pip、磁盘分析、Git 管理、快捷命令 |
| **配置终端** | Shell 运行、密钥配置、bashrc 编辑、欢迎信息、开机命令 |
| **X11 功能** | X11 设置、终端显示/隐藏、环境配置、VNC 连接 |
| **ROOT/ADB** | 网络 ADB、ADB 连接/断开/设备列表、Docker 检查 |

### 🛠 35+ 图形化工具

| 工具 | 功能 |
|------|------|
| **代码编辑器** | 21种语言语法高亮、文件浏览、新建文件、查找、AI 辅助编码、最近文件、一键运行 |
| **AI 助手** | 多轮对话、对话历史持久化、支持 DeepSeek/OpenAI/Claude/Ollama |
| **Nmap** | 端口扫描参数表单，9种高级选项，时序模板 |
| **Metasploit** | msfvenom Payload 生成器，6平台 × 多种载荷 × 回调类型 |
| **Sqlmap** | SQL 注入参数表单，向导模式/高级选项 |
| **Dirb** | Web 目录扫描，字典/代理/Cookie 配置 |
| **ADB 管理器** | 设备列表、APK 安装、Shell 命令、端口转发、截图录屏 |
| **SSH 管理器** | 保存连接、密码/密钥认证、一键连接 |
| **VNC 连接** | 地址/端口/密码配置，启动/停止服务 |
| **包管理器** | 搜索/安装/卸载 pkg 软件包 |
| **进程管理器** | 进程列表 + KILL 确认 |
| **网络工具** | Ping/Traceroute/NSLookup/Curl/Whois/DNS |
| **端口扫描** | 快捷端口预设 + 实时 OPEN/CLOSED 检测 |
| **系统仪表盘** | CPU/内存/磁盘/运行时间实时数据 |
| **下载中心** | 真实下载到 /sdcard/Download/ |
| **Crontab** | 14种定时预设 + 添加/列表/清空 |
| **WiFi 工具集** | 8种 WiFi 安全工具(需要 Kali) |
| **Hash 工具包** | Hash 识别 + John/Hashcat 破解 |
| **反弹 Shell** | 8种载荷类型(Bash/Python/PHP/NC 等) |
| **Kali 服务管理** | 10种服务可视化启停 |
| **Kali 工具分类** | 13类 50+ 工具(点击运行/长按安装) |
| **快捷命令** | 保存/运行常用命令 |
| **代码模板** | 20种代码片段点击即粘贴 |
| **Git 管理器** | 克隆/拉取/提交/推送/日志 |
| **Python/pip** | pip 安装/列出/更新 + 快捷命令 |
| **磁盘分析器** | du -sh 可视化排序 |
| **系统清理** | apt/pip/npm 缓存清理 + 大文件查找 |
| **备份管理** | Home/usr/全量备份 + 恢复 |
| **容器管理** | proot-distro 容器管理 |
| **Termux 容器切换** | files/files1/files2 目录交换 |
| **主题配色** | 6种预设 + 自定义强调色 |

### ⚡ 核心技术

- **拖拽悬浮球** — 果冻弹性缩放动画
- **流体波纹** — 金色涟漪呼吸动画
- **Q弹触摸** — 触点弹性反馈
- **渐变面板** — 深紫→藏蓝奢华渐变背景
- **UI 动画** — 渐入、弹性滑动
- **全 Service 架构** — 无 Activity 依赖，PopupWindow 安全兼容

---

## 安装

### 环境要求
- Android 6.0+ (API 23)
- ARM64 设备
- 已安装 [Termux](https://github.com/termux/termux-app) (得和utermux签名相同的版本)

### 安装步骤
1. 下载最新 APK 从 [Releases](https://github.com/Gavin-crazyCoding/GavinFloat/releases)
2. 在系统设置中授予「悬浮窗权限」
3. 打开应用 → 悬浮球出现在屏幕边缘
4. 点击悬浮球打开菜单面板

### AIDE 编译
1. 用 AIDE 打开项目根目录
2. 等待索引完成
3. 点击「运行」编译安装

```bash
# 或使用 Gradle
./gradlew assembleDebug
```

---

## 项目结构

```
com.termux.menu/
├── FloatingBallService.java  # 核心悬浮球服务 (菜单/球/命令派发)
├── MainActivity.java         # 启动 Activity
├── ExitReceiver.java          # 通知栏退出广播
├── model/                     # 数据模型
│   ├── MenuCategoryData.java  # 菜单分组
│   ├── MenuEntryData.java     # 菜单项
│   ├── XmlMenuItem.java       # XML 菜单项模型
│   ├── XmlMenuGroup.java      # XML 菜单分组
│   └── RequestMessageItem.java # AI 消息模型
├── termux/
│   └── TermuxCommandHelper.java # Termux 命令通信
├── ui/
│   ├── dialog/                # 35+ 图形化工具弹窗
│   │   ├── CodeEditDialog.java      # 代码编辑器(21语言)
│   │   ├── AIChatDialog.java        # AI 对话
│   │   ├── NmapDialog.java          # Nmap 扫描
│   │   ├── MetasploitDialog.java    # MSF Payload
│   │   ├── SqlmapDialog.java        # SQL 注入
│   │   ├── AdbManagerDialog.java    # ADB 管理
│   │   ├── SshManagerDialog.java    # SSH 管理
│   │   ├── VncConnectDialog.java    # VNC 连接
│   │   ├── PackageManagerDialog.java# 包管理器
│   │   ├── ProcessManagerDialog.java# 进程管理
│   │   ├── KaliToolsDialog.java     # Kali 工具分类
│   │   ├── Wi-FiToolsDialog.java    # WiFi 工具
│   │   ├── HashToolkitDialog.java   # Hash 工具
│   │   └── ...                     # 更多...
│   ├── IconProvider.java     # 图标生成
│   ├── PageController.java   # 页面控制
│   ├── SettingsController.java # 设置面板
│   ├── FluidRippleDrawable.java  # 流体波纹动画
│   └── TouchRippleView.java     # 触摸涟漪
├── utils/
│   ├── FileUtils.java        # 文件 I/O
│   └── PrefsManager.java     # 偏好设置
└── xml/
    └── XmlMenuParser.java    # XML 菜单解析
```

---

## 菜单配置

默认菜单在 `assets/default_menu.xml`。支持外部配置：
```
/data/data/com.termux/files/home/ZtInfo/main_menu_path.xml
```

### 支持的点击类型

| 类型 | 格式 | 说明 |
|------|------|------|
| `ztShell:` | 发送命令到 Termux 终端 | `ztShell:pkg update -y` |
| `shell_output:` | 执行命令并显示输出 | `shell_output:uname -a` |
| `editor:` | 打开代码编辑器 | `editor:/path/to/file` |
| `filebrowser:` | 打开文件浏览器 | `filebrowser:/path/` |
| `internal:` | 调用内部功能 | `internal:nmap` |
| `jumpUrl:` | 浏览器打开 URL | `jumpUrl:https://...` |
| `commands:` | 选项列表弹窗 | `commands:选项1@@命令1,选项2@@命令2` |
| `input:` | 输入弹窗 | `input:标题@@提示@@模板` |
| `shellUrl:` | 下载脚本并执行 | `shellUrl:https://...` |

---

## 依赖

- `androidx.appcompat:appcompat:1.6.1`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.cardview:cardview:1.0.0`
- `com.google.android.material:material:1.12.0`
- 全部 **0 Kotlin**，纯 Java 实现
- 无 OkHttp / Gson / RxJava 等第三方库

---

## 开源协议

[MIT License](LICENSE)

---

## 致谢

- [ZeroTermux](https://github.com/hanxinhao000/ZeroTermux) — 菜单设计及功能参考
- [Termux](https://github.com/termux/termux-app) — Android 终端模拟器
- [Claude Code](https://claude.ai/code) — AI 辅助开发

---

🤖 Built with [Claude Code](https://claude.ai/code)

