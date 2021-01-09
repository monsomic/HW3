cmake_minimum_required(VERSION 3.17)
project(untitled)

set(CMAKE_CXX_STANDARD 11)

add_executable(untitled main.cpp src/keyboardReader.cpp include/keyboardReader.h src/bgrsConnectionHandler.cpp include/bgrsConnectionHandler.h src/bgrsClient.cpp include/bgrsClient.h)