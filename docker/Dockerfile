FROM ubuntu:16.04
MAINTAINER leungwensen <leungwensen@gmail.com>

RUN apt-get update && apt-get install -y \
            gcc-multilib g++-multilib libc6-i386 lib32ncurses5 lib32stdc++6 \
            openjdk-8-jre \
            git

RUN git clone https://github.com/leungwensen/cbc-ubuntu-64bit.git

RUN cd cbc-ubuntu-64bit

RUN ./install.sh

