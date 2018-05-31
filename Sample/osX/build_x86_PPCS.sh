#!/bin/sh

OS=osX
BOARD=x86
P2P=PPCS

CC=gcc
CXX=g++
STRIP=strip

make -f ./Makefile OS=${OS} BOARD=${BOARD} P2P=${P2P} CC=${CC} CXX=${CXX} STRIP=${STRIP} 