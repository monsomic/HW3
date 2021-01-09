CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: clean registrationClient
	g++ -o bin/BGRSclient bin/bgrsConnectionHandler.o bin/bgrsClient.o bin/keyboardReader.o  $(LDFLAGS)

bgrsClient: bin/bgrsConnectionHandler.o bin/bgrsClient.o bin/keyboardReader.o

bin/bgrsConnectionHandler.o: src/bgrsConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/bgrsConnectionHandler.o src/bgrsConnectionHandler.cpp

bin/bgrsClient.o: src/bgrsClient.cpp
	g++ $(CFLAGS) -o bin/registrationClient.o src/registrationClient.cpp

bin/keyboardReader.o: src/keyboardReader.cpp
	g++ $(CFLAGS) -o bin/keyboardReader.o src/keyboardReader.cpp

.PHONY: clean
clean:
	rm -f bin/*
