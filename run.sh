#!/bin/bash

# 编译项目
mvn compile

# 获取所有依赖的类路径
CLASSPATH=$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)

# 运行应用程序，添加所有模块
java --module-path target/classes:$CLASSPATH \
     --add-modules ALL-MODULE-PATH \
     com.example.touchtyped.app.Application 