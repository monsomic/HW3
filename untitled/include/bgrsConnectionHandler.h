//
// Created by michael monsonego on 04/01/2021.
//

#ifndef UNTITLED_BGRSCONNECTIONHANDLER_H
#define UNTITLED_BGRSCONNECTIONHANDLER_H
#include <iostream>
#include <boost/asio.hpp>



using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
using boost::asio::ip::tcp;



class bgrsConnectionHandler {

    //
// Created by michael monsonego on 04/01/2021.
//
private:
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;


public:
    bgrsConnectionHandler(string host, short port);

~bgrsConnectionHandler() ;
bool connect() ;

bool getBytes(char bytes[], unsigned int bytesToRead) ;

bool sendBytes(const char bytes[], int bytesToWrite) ;
bool getLine(std::string& line) ;


bool sendLine(std::string& line) ;

bool getFrameAscii(std::string& frame, char delimiter) ;


bool sendFrameAscii(const std::string& frame, char delimiter) ;

// Close down the connection properly.
void close() ;

string decipherOpcode(string opcode);

short  bytesToShort(char* bytesArr);

void shortToBytes(short num, char* bytesArr);


};


#endif //UNTITLED_BGRSCONNECTIONHANDLER_H
