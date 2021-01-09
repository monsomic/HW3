package bgu.spl.net;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class bgrsMessageEncoderDecoder implements MessageEncoderDecoder<String> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private String opcodeString="";
    private short opcode;
    private boolean foundZero1 = false;
    private boolean foundZero2 = false;

    @Override
    public String decodeNextByte(byte nextByte) {

        if (len == 2) {
            opcodeString = new String(bytes, 0, len, StandardCharsets.UTF_8);;
            opcode = Short.parseShort(opcodeString);
        }
        // if opcode=0, continue to the next byte (length < 2)

        else if (opcode == 4 || opcode == 11) { // only opcode
            return popString();
        } else if (opcode == 5 || opcode == 6 || opcode == 7 || opcode == 9 || opcode == 10 ) { // 2 bytes only after the opcode
            if (len == 4) {
                return popStringCourseNum();
            }
        } else if (opcode == 8) { // \0 only one time  (TODO isCorrect to put ack(different kind of decode ?))
            if (foundZero1)
                return popString();

        } else if (opcode == 1 || opcode == 2 || opcode == 3) { // two \0 in the message
            if (foundZero2)
                return popString();

        }


        pushByte(nextByte);
        if (nextByte == '\0'){
            if (!foundZero1)
                foundZero1=true;
            else
                foundZero2=true;
        }
        return null;


    }

    @Override
    public byte[] encode(String message) {
        return message.getBytes();    }



    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.

        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        opcodeString = "";
        opcode=0;
        foundZero1 = false;
        foundZero2 = false;
        return result;
    }
    public String popStringCourseNum(){ // todo check if works
        byte[] courseNumBytes= {bytes[2],bytes[3]};
        short courseNum = bytesToShort(courseNumBytes);
        String result = new String(new byte[]{bytes[0], bytes[1]}, 0, len, StandardCharsets.UTF_8);
        result+=courseNum;
        len = 0;
        opcodeString = "";
        opcode=0;
        foundZero1 = false;
        foundZero2 = false;
        return result;
    }
}