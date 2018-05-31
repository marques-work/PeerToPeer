LDFLAG =-O2 -g -Wall -DWINDOWS -DWIN32DLL -lpthread -l${P2P}_API -lstdc++ -lws2_32 -Wl,-rpath=./ 
LDFLAG+=-I ../../Include/${P2P}/
LDFLAG+=-L ../../Lib/${OS}/${BOARD}

ifeq ($(BOARD), x64)
exe=64.exe
LDFLAG+=-m64 
else
	ifeq ($(BOARD), x86)
		LDFLAG+=-m32 
		exe=.exe
	endif
endif
dylib=dll

all: clean
	${CXX} ./ConnectionTester.cpp -o ConnectionTester${exe} -s ${LDFLAG} 
	${CXX} ./ReadWriteTester.cpp -o ReadWriteTester${exe} -s ${LDFLAG} 
	${CXX} ./ListenTester.cpp -o ListenTester_${P2P}${exe} -s ${LDFLAG}
	cp -rf ../../Lib/${OS}/${BOARD}/${P2P}_API.${dylib} ./
clean:
	rm -rf *.exe  ${P2P}_API.${dylib}
