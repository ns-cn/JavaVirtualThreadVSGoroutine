#!/bin/zsh
threads=${1:-1000}
type=${2:--1}
coreSize=${3:-1}
echo threads: $threads type: $type coreSize: $coreSize
echo "JAVA 开始"
cd java
~/mine/jdk-19.jdk/Contents/Home/bin/javac --release 19 --enable-preview Main.java
~/mine/jdk-19.jdk/Contents/Home/bin/java --enable-preview Main $threads $type $coreSize
echo "GO 开始"
cd ../go
go run . --threads $threads