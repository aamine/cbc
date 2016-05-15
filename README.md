CbC - Cflat Compiler (the ubuntu 64bit version)
====================

[《自制编译器》](http://www.ituring.com.cn/book/1308)一书中实现的cbc编译器的ubuntu64位版本。这个项目主要解决64位机器上无法正常编译和运行[cbc](https://github.com/aamine/cbc)的问题。

## 直接安装使用（在ubuntu 64位系统上）

### 安装依赖

> 注意：不同ubuntu发行版可能依赖库名称不一致。此处代码为ubuntu 16.04版本上的安装命令

```shell
apt-get update && apt-get install -y \
        gcc-multilib g++-multilib libc6-i386 lib32ncurses5 lib32stdc++6 \
        openjdk-8-jre \
        git
```

### 下载&安装cbc

```shell
git clone https://github.com/leungwensen/cbc-ubuntu-64bit.git
cd cbc-ubuntu-64bit && ./install.sh
```

### 使用

和原始的cbc不同，在64位系统里需要增加`-Wa,"--32" -Wl,"-melf_i386"`执行参数。

```shell
cbc -Wa,"--32" -Wl,"-melf_i386" test/hello.cb
./hello
> Hello, World!
```

## 使用docker镜像（在任意64位宿主环境上）

大致的原理就是基于ubuntu 16.04的64位系统构建了一个可供cbc编译、执行的环境。用户只需把打包好的镜像下载到本地就可以得到可执行的cbc，免去配置和编译cbc的麻烦。

### 安装docker：详见[Docker 中文指南](http://docker.widuu.com/index.html)

### 启动docker daemon进程

```shell
eval $(docker-machine env default)
```

### 下载镜像[leungwensen/cbc-ubuntu-64bit](https://hub.docker.com/r/leungwensen/cbc-ubuntu-64bit)

```shell
docker pull leungwensen/cbc-ubuntu-64bit
```

### 执行镜像

```shell
docker run -t -i leungwensen/cbc-ubuntu-64bit
```

### 执行cbc

> 镜像里的cbc命令为`cbc -Wa,--32 -Wl,-melf_i386`的别名，可以直接执行。

```shell
cbc cbc-ubuntu-64bit/test/hello.cb
```

## 共建／bug报告

有任何使用上的问题，可以[搜索已有issue或者新建一个issue](https://github.com/leungwensen/cbc-ubuntu-64bit/issues)。

更希望大家可以一起完善这个项目，给我提[pull request](https://github.com/leungwensen/cbc-ubuntu-64bit/pulls)

## 参考

* [aamine/cbc](https://github.com/aamine/cbc)
* [ふつうのコンパイラをつくろう Ubuntu64bit](http://blog.livedoor.jp/yamanobori_old/archives/5189798.html)

