
#include <stdlib.h>
#include <iostream>
#include <thread>
#include "../include/keyboardReader.h"
#include "../include/bgrsConnectionHandler.h"


int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    bgrsConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    bool shouldTerminate=false;
    keyboardReader keyboard = keyboardReader(connectionHandler,shouldTerminate);
    std:: thread th2(&keyboardReader::run, &keyboard);

    while (1) {
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }

        std::cout <<  answer << std::endl;

        if (answer == "ACK 4 Logout successfully") { // todo check if it is the right sentence
            shouldTerminate=true;
            std::cout << "Exiting...\n" << std::endl;
            break;
        }
    }
    th2.join();
    connectionHandler.close();//todo bien??
    return 0;
}