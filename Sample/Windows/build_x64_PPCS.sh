#!/bin/sh

OS=Windows
BOARD=x64
P2P=PPCS

CC=x86_64-w64-mingw32-gcc
CXX=x86_64-w64-mingw32-g++
STRIP=x86_64-w64-mingw32-strip

make -f ./Makefile OS=${OS} BOARD=${BOARD} P2P=${P2P} CC=${CC} CXX=${CXX} STRIP=${STRIP} 