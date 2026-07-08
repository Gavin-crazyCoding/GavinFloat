#!/data/data/com.termux/files/usr/bin/bash
echo '=== MOE 全能工具 ==='
echo '正在安装依赖...'
pkg update -y 2>/dev/null
pkg install curl wget -y 2>/dev/null
echo '正在获取MOE脚本...'
bash -c "$(curl -L gitee.com/mo2/linux/raw/2/2)"
