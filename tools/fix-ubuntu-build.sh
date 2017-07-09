#!/bin/bash

# replace c runtime object path for debian/ubuntu
sed -i "s/\/usr\/lib/\/usr\/lib\/i386-linux-gnu/" net/loveruby/cflat/sysdep/GNULinker.java
