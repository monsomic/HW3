#include <iostream>
#include <mutex>
#include <thread>
#include "../include/bgrsConnectionHandler.h"

class keyboardReader{
private:
    bgrsConnectionHandler connectionHandler;
    bool shouldTerminate;
    //std::mutex & _mutex;
    //std::mutex& mutex) :  _mutex(mutex
public:
    keyboardReader (bgrsConnectionHandler& connectionHandler,bool &shouldTerminate) {
        this->connectionHandler= connectionHandler;
        this->shouldTerminate=shouldTerminate;
    }

    void run(){
        while(!shouldTerminate){
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            int len=line.length();
            if (!connectionHandler.sendLine(line)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }

            std::cout << "Sent " << len << " bytes to server" << std::endl;
        }
    }
};