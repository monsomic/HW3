//
// Created by michael monsonego on 04/01/2021.
//

#include "../include/bgrsConnectionHandler.h"
#include <iostream>
#include <boost/asio/ip/tcp.hpp>


using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

bgrsConnectionHandler::bgrsConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

bgrsConnectionHandler::~bgrsConnectionHandler() {
    close();
}

bool bgrsConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool bgrsConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool bgrsConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool bgrsConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\0');
}

bool bgrsConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}


bool bgrsConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    char firstTwoBytes[2];
    char secondTwoBytes[2];
    try {
    if(!getBytes(firstTwoBytes, 2))
    {
        return false;
    }
    if(!getBytes(secondTwoBytes, 2))
    {
        return false;
    }

    short opcode= bytesToShort(firstTwoBytes);
    short opcodetype = bytesToShort(secondTwoBytes);

    if(opcode==12){
        frame.append("ACK "+opcodetype+'\n');
        do{
            if(!getBytes(&ch, 1))
            {
                return false;
            }
            if(ch!='\0')
                frame.append(1, ch);
        }while (delimiter != ch);
    }
    else if(opcode==13){
        frame.append("ERROR "+opcodetype);
    }
    } 
    
    catch (std::exception& e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool bgrsConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    int index=frame.find_first_of(' ');
    string op = frame.substr(0,index);
    string opcode= decipherOpcode(op);
    string output = opcode;
    //string chars= new char[2];
    //shortToBytes(opcode, chars&);
    //output+= chars; // todo maybe creat a function short to string and send it that way

    if(opcode == "04" || opcode == "11"){// only opcode
        //do nothing
    }
    else if(opcode == "05" || opcode == "06" || opcode == "07" || opcode == "09" || opcode == "10"){// 2 bytes only after the opcode
        string num=(frame.substr(index+1,frame.length())); //the course num
       // char chars[num.length()]; // the course num in bytes array
      //  for(int i=0;i<sizeof (num); i++){
       //     chars[i]= num[i];
     //   }
        short courseNum = boost::lexical_cast<short>(num);
       //short courseNum= bytesToShort(chars); // the course num in short
       char courseNumBytes[2];
       shortToBytes(courseNum,courseNumBytes); // turns the short number into representation of two bytes
       output+=courseNumBytes; //todo if not working we need to change to bytes first and than send in output
    }
    else if(opcode == "08"){ // \0 only one time
        output+=frame.substr(index+1, frame.length())+delimiter;
    }
    else if(opcode == "01" || opcode == "02" || opcode == "03"){ // two /0
        string subFrame = frame.substr(index+1, frame.length());
        int index2=subFrame.find_first_of(' ');//find second space
        output+=subFrame.substr(0,index2)+delimiter+subFrame.substr(index2+1,subFrame.length())+delimiter;
    }
   // bool result=sendBytes(frame.c_str(),frame.length());
    // if(!result) return false;    todo : why check if false ?
    //return sendBytes(&delimiter,1);
    return sendBytes(output.c_str(),output.length());
}

// Close down the connection properly.
void bgrsConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

string bgrsConnectionHandler:: decipherOpcode(string opcode){
    if(opcode.compare("ADMINREG")==0){
        return "01";
    }
    else if (opcode.compare("STUDENTREG")==0){
        return "02";
}
    else if (opcode.compare("LOGIN")==0){
        return "03";
    }
    else if (opcode.compare("LOGOUT")==0){
        return "04";
    }
    else if (opcode.compare("COURSEREG")==0){
        return "05";
    }
    else if (opcode.compare("KDAMCHECK")==0){
        return "06";
    }
    else if (opcode.compare("COURSESTAT")==0){
        return "07";
    }
    else if (opcode.compare("STUDENTSTAT")==0){
        return "08";
    }
    else if (opcode.compare("ISREGISTERED")==0){
        return "09";
    }
    else if (opcode.compare("UNREGISTER")==0){
        return "10";
    }
    else if (opcode.compare("MYCOURSES")==0){
        return "11";
    }
    else return "00";
}

short bgrsConnectionHandler:: bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

void bgrsConnectionHandler:: shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}