#!/data/data/com.termux/files/usr/bin/bash
echo '=== QEMU 虚拟机助手 ==='
echo '用法:'
echo '1) 安装QEMU    pkg install qemu-system-x86-64-headless qemu-utils -y'
echo '2) 创建镜像     qemu-img create -f qcow2 ~/vm.img 10G'
echo '3) 安装系统     qemu-system-x86_64 -hda ~/vm.img -cdrom ~/iso.iso -boot d -m 2G'
echo '4) 启动VM      qemu-system-x86_64 -hda ~/vm.img -m 2G -vnc :1'
echo '5) WinXP模拟    qemu-system-x86_64 -hda ~/winxp.img -m 512 -vga cirrus'
echo '6) Win7模拟     qemu-system-x86_64 -hda ~/win7.img -m 2G -vga std'
read -p '选择操作(0=取消): ' ch
case $ch in
  1) pkg install qemu-system-x86-64-headless qemu-utils -y;;
  2) read -p '镜像大小(GB): ' sz; qemu-img create -f qcow2 ~/vm.img ${sz}G;;
  3) echo '请将iso放入 ~/iso.iso'; qemu-system-x86_64 -hda ~/vm.img -cdrom ~/iso.iso -boot d -m 2G -accel tcg,thread=multi;;
  4) qemu-system-x86_64 -hda ~/vm.img -m 2G -vnc :1 -accel tcg,thread=multi &;;
  5) echo '需要winxp.img镜像'; qemu-system-x86_64 -hda ~/winxp.img -m 512 -vga cirrus -accel tcg;;
  6) echo '需要win7.img镜像'; qemu-system-x86_64 -hda ~/win7.img -m 2G -vga std -accel tcg;;
  *) exit;;
esac
