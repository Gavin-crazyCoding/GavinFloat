#!/data/data/com.termux/files/usr/bin/bash
echo '=== Linux 发行版安装器 ==='
echo '支持的发行版: Ubuntu, Debian, Kali, Arch'
echo '选择发行版:'
echo '1) Ubuntu 22.04'
echo '2) Kali NetHunter'
echo '3) Debian 12'
echo '4) Arch Linux'
read -p '输入编号(1-4): ' choice
case $choice in
  1) proot-distro install ubuntu; proot-distro login ubuntu;;
  2) echo '正在安装Kali...'; pkg install wget proot -y; cd ~; wget https://raw.githubusercontent.com/Gavin-crazyCoding/GavinTermux/main/assets/kali/kali.sh 2>/dev/null || echo '使用内置脚本'; bash kali.sh;;
  3) proot-distro install debian; proot-distro login debian;;
  4) proot-distro install archlinux; proot-distro login archlinux;;
  *) echo '无效选择'; exit 1;;
esac
