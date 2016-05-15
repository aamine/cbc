# This dockerfile use the ubuntu image
FROM ubuntu:16.04

# maintainer is leungwensen <leungwensen@gmail.com>
MAINTAINER leungwensen <leungwensen@gmail.com>

# commands to update the image
RUN apt-get update && apt-get install -y \
            gcc-multilib g++-multilib libc6-i386 lib32ncurses5 lib32stdc++6 \
            openjdk-8-jre \
            git

RUN git clone https://github.com/leungwensen/cbc-ubuntu-64bit.git

RUN cd cbc-ubuntu-64bit && bash install.sh

RUN echo 'PATH=/usr/local/cbc/bin:$PATH' >> $HOME/.bashrc

RUN echo alias cbc=cbc -Wa,--32 -Wl,-melf_i386 >> $HOME/.bashrc

# commands when creating a new container

