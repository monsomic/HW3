package bgu.spl.net;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.List;

public class bgrsProtocol implements MessagingProtocol<String> {
    private String user=null;       //seted at login
    private Database database= Database.getInstance();
    private boolean shouldTerminate = false;

    @Override
    public String process(String msg) {
        String opcode =(msg.substring(0,2));
        //short opcode= Short.parseShort(op);
      //  byte[] b = shortToBytes(opcode);
       // short sh = bytesToShort(b);
        String info = msg.substring(2,msg.length());
        switch(opcode){
            case "01": {  //Admin register
                int index=info.indexOf('\0');
                String username=info.substring(0,index);
                info = msg.substring(index+1,msg.length());
                index=info.indexOf('\0');
                String password=info.substring(0,index);
                if(user==null || database.isRegistered(username))
                    return "13"+opcode; //error
                else {
                    database.adminRegister(username, password);
                    return "12"+opcode+"Admin registered successfully"+'\0'; //todo check if opcode is sent right(as string and not short)
                }
            }

            case "02":{  //student register
                int index=info.indexOf('\0');
                String username=info.substring(0,index);
                info = msg.substring(index+1,msg.length());
                index=info.indexOf('\0');
                String password=info.substring(0,index);
                if(user==null || database.isRegistered(username))
                    return "13"+opcode; //error
                else {
                    database.studentRegister(username, password);
                    return "12"+opcode+"Student registered successfully"+'\0';
                }
            }

            case "03":{ //login
                int index=info.indexOf('\0');
                String username=info.substring(0,index);
                info = msg.substring(index+1,msg.length());
                index=info.indexOf('\0');
                String password=info.substring(0,index);
                if(user==null && database.loginApproved(username,password)) {
                    user = username;
                    database.loginStudent(username);
                    return "12"+opcode+"Login successfully"+'\0';
                }
                else {
                    return "13"+opcode; //error
                }
            }
            case "04":{ // logout
                if(user==null) // checks if is login
                    return "13"+opcode; //error
                else{
                    database.logoutStudent(user);
                    user=null;
                    shouldTerminate=true;
                    return "12"+opcode+"Logout successfully"+'\0';
                    //todo send ack , the client receives this ack in order to terminate
                }

            }
            case "05":{ // register to course
                Integer courseNum = Integer.parseInt(info); // todo check if right
                if(user!=null && database.isCourseRelevant(courseNum,user)) { // checks if is login and relevant
                    database.registerCourse(courseNum,user);
                    return "12"+opcode+"Registered to course: "+courseNum+" successfully"+'\0';
                }
                else{
                    return "13"+opcode; //error
                }

            }
            case "06":{ // check kdam course
                if(user==null || database.isAdmin(user)){
                    return "13"+opcode; //error
                }
                else {
                    Integer courseNum = Integer.parseInt(info); //todo check if right
                    return "12"+opcode+database.getKdamCourses(courseNum)+'\0'; // attach the string through ACK
                }
            }
            case "07":{ // course stat
                Integer courseNum = Integer.parseInt(info); //todo check if right
                if(user==null || !database.isAdmin(user))
                    return "13"+opcode; //error
                else{
                    return "12"+opcode+database.getCourseStat(courseNum)+'\0'; // attach the string through ACK
                }

            }
            case "08":{ // student stat
                String username= info.substring(0,info.length()-1);
                if(user==null || !database.isAdmin(user))
                    return "13"+opcode; //error
                else{
                    return "12"+opcode+database.getStudentStat(username)+'\0'; // attach the string through ACK
                    //ack
                }
            }
            case "09":{ // is registered
                Integer courseNum = Integer.parseInt(info); //todo check if right
                if(user==null) // checks if is login
                    return "13"+opcode; //error
                else{
                    String output;
                    if(database.isRegisteredCourse(user,courseNum))
                        output="REGISTERED";
                    else
                        output="NOT REGISTERED";
                    return "12"+opcode+output+'\0';
                }
            }
            case "10":{ // unregister course
                Integer courseNum = Integer.parseInt(info); //todo check if right
                if(user==null || !database.isRegisteredCourse(user,courseNum)) // checks if is login
                    return "13"+opcode; //error
                else{
                    database.unregisterCourse(user,courseNum);
                    return "12"+opcode+"Unregistered to course: "+courseNum+" successfully"+'\0';
                }
            }
            case "11":{ // my courses
                if(user==null)
                    return "13"+opcode; //error
                else{
                    return "12"+opcode+database.getMyCourses(user)+'\0';// attach the string through ACK
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

}
