package bgu.spl.net;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.Reactor;

import java.util.function.Supplier;

public class reactorMain {
    public static void main (String args[]){
        Reactor reactor = new Reactor(
        7,    //todo numThreads WTF ??
        7777,
        ()-> new bgrsProtocol(),
        ()-> new bgrsMessageEncoderDecoder()) ;

        reactor.serve();




    }
}
