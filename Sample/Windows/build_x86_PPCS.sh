#!/bin/sh

OS=Windows
BOARD=x86
P2P=PPCS

CC=i686-w64-mingw32-gcc
CXX=i686-w64-mingw32-g++
STRIP=i686-w64-mingw32-strip

make -f ./Makefile OS=${OS} BOARD=${BOARD} P2P=${P2P} CC=${CC} CXX=${CXX} STRIP=${STRIP} 