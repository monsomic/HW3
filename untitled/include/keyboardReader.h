//
// Created by michael monsonego on 01/01/2021.
//

#ifndef UNTITLED_KEYBOARDREADER_H
#define UNTITLED_KEYBOARDREADER_H


#include "bgrsConnectionHandler.h"

class keyboardReader {
    private:
    bgrsConnectionHandler connectionHandler;
    bool shouldTerminate;
    //std::mutex & _mutex; //todo needed only if needs synchronize
    // std::mutex& mutex (inside constructor)
public:
    keyboardReader (bgrsConnectionHandler& connectionHandler,bool &shouldTerminate);
    void run();
};



#endif //UNTITLED_KEYBOARDREADER_H
