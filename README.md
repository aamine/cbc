CbC - Cflat Compiler (the ubuntu 64bit version)
====================

[《自制编译器》](http://www.ituring.com.cn/book/1308)一书中实现的cbc编译器的ubuntu64位版本。

## 用法（在ubuntu 64位系统上）

### 安装依赖

```shell
apt-get update && apt-get install -y \
        gcc-multilib g++-multilib libc6-i386 lib32ncurses5 lib32stdc++6 \
        openjdk-8-jre \
        git
```

### 下载&安装

```shell
git clone https://github.com/leungwensen/cbc-ubuntu-64bit.git
cd cbc-ubuntu-64bit
./install.sh
```

### 使用

和原始的cbc不同，在64位系统里需要增加`-Wa,"--32" -Wl,"-melf_i386"`执行参数。

```shell
$ cbc -Wa,"--32" -Wl,"-melf_i386" test/hello.cb
$ ./hello
Hello, World!
```

## docker镜像

## 参考

* [aamine/cbc](https://github.com/aamine/cbc)
* [ふつうのコンパイラをつくろう Ubuntu64bit](http://blog.livedoor.jp/yamanobori_old/archives/5189798.html)

