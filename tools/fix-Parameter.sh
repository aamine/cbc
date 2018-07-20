#!/bin/bash

# rename class 'Paramater' (conflict with 'java.lang.reflect.Parameter') to 'Param'
find net/loveruby/cflat -type f -iname "*.java"|xargs sed -i "s/\bParameter\b/Param/g"
sed -i "s/\bParameter\b/Param/g" net/loveruby/cflat/parser/Parser.jj
mv net/loveruby/cflat/entity/Parameter.java net/loveruby/cflat/entity/Param.java
