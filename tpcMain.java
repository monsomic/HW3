package bgu.spl.net;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Reactor;

import java.util.function.Supplier;

public class tpcMain {

    public static void main(String args[]) {
        BaseServer srv = new bgrsServer(
                7777,
                () -> new bgrsProtocol(),
                () -> new bgrsMessageEncoderDecoder());
                srv.serve();

    }
}