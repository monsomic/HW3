package bgu.spl.net;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
class User{
	private String username;
	private String password;
	private boolean admin;
	private boolean login;
	private Set<Course> courseList; // sortedlist

	public User(String username, String password, boolean admin) {
		this.username = username;
		this.password = password;
		this.admin = admin;
		login=false;
		courseList= new TreeSet<Course>((Course c1,Course c2)-> c1.getSerialNumber()-c2.getSerialNumber()); //todo : does this work??
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isLogin() {
		return login;
	}

	public List<Integer> getCourseNumlist() {
		List courseNumList = new Vector();
		for (Course c: courseList){
			courseNumList.add(c.getCourseNum());
		}
		return courseNumList;
	}

	public boolean isRegisteredCourse(int courseNum){
		return courseList.contains(courseNum);
	}

	public void setLogin(boolean b){
		login=b;
	}

	public void addCourse(Course course){
		courseList.add(course);
	}
	public void unregisterCourse(Course course){
		courseList.remove(course);
	}

}

class Course{
	private int serialNumber;
	private int courseNum;
	private String courseName;
	private List<Integer> KdamCoursesList;
	private int numOfMaxStudents;
	private int seatsAvailable; // available places
	private Set<String> registeredStudents; //sorted list


	public Course(int serialNumber, int courseNum, String courseName, List<Integer> kdamCoursesList, int numOfMaxStudents) {
		this.serialNumber = serialNumber;
		this.courseNum = courseNum;
		this.courseName = courseName;
		this.KdamCoursesList = kdamCoursesList;
		this.numOfMaxStudents = numOfMaxStudents;
		this.seatsAvailable=numOfMaxStudents;
		this.registeredStudents= new TreeSet<String>(); // names of students sorted

	}
	public int getSerialNumber(){
		return serialNumber;
	}

	public int getCourseNum() {
		return courseNum;
	}

	public String getCourseName() {
		return courseName;
	}

	public List<Integer> getKdamCoursesList() {
		return KdamCoursesList;
	}

	public int getNumOfMaxStudents() {
		return numOfMaxStudents;
	}

	public boolean isAvailable(){
		return seatsAvailable>0;
	}

	public int getSeatsAvailable(){
		return seatsAvailable;
	}
	public Set<String> getRegisteredStudents() {
		return registeredStudents;
	}


	public void register(String studentName){  // needs to be synchronized // need to check isAvailable before use
		seatsAvailable--;
		registeredStudents.add(studentName);
	}

	public void unregister(String studentName){ // needs to check if this student registered before use
		registeredStudents.remove(studentName);
		seatsAvailable++;
	}

}

public class Database {
	private ConcurrentHashMap<String, User> users;
	private ConcurrentHashMap<Integer, Course> courses;

	public static class DatabaseHolder{
		private static Database instance = new Database();

	}



	//to prevent user from creating new Database
	private Database() {
		//users= new ConcurrentHashMap<String, Student>();
		//courses = new Vector<Course>();
	}

	private Database(String coursesFilePath) {
		users= new ConcurrentHashMap<String, User>();
		courses = new ConcurrentHashMap<Integer, Course>();
		initialize(coursesFilePath);
	}


	public static Database getInstance(String coursesFilePath){
		DatabaseHolder.instance= new Database(coursesFilePath);
		return DatabaseHolder.instance;
	}


	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		return DatabaseHolder.instance;
	}
	
	/**
	 * loades the courses from the file path specified 
	 * into the Database, returns true if successful.
	 */
	boolean initialize(String coursesFilePath) {

		try {
			File courseFile = new File(coursesFilePath);
			Scanner myReader = new Scanner(courseFile);
			int serialNum=0;
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				int index=data.indexOf('|');
				int courseNum=Integer.parseInt(data.substring(0,index));

				data=data.substring(index+1,data.length());
				index=data.indexOf('|');
				String courseName= data.substring(0,index);

				data=data.substring(index+1,data.length());
				index=data.indexOf('|');
				String KdamCourseList= data.substring(1,index-1);
				List<Integer> kdamList = new Vector<Integer>();
				Boolean stop=false;
				while(!stop){
					int i=KdamCourseList.indexOf(',');
					if(i!=-1) {
						kdamList.add(Integer.parseInt(KdamCourseList.substring(0,i)));
						KdamCourseList=KdamCourseList.substring(i+1,KdamCourseList.length());
					}
					else{ // last course
						kdamList.add(Integer.parseInt(KdamCourseList));
						stop = true;
					}
				}

				data=data.substring(index+1,data.length());
				int numOfMaxStudents = Integer.parseInt(data);

				serialNum++;
				courses.put(courseNum,new Course(serialNum,courseNum,courseName,kdamList,numOfMaxStudents));
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isRegistered(String username){
		User s = users.get(username);
		if(s!=null && s.getUsername().equals(username))
			return true;
		return false;
	}

	public void adminRegister(String username, String password){
		User s= new User(username,password,true);
		users.put(username,s);
	}

	public void studentRegister(String username, String password){
		User s= new User(username,password,false);
		users.put(username,s);
	}

	public boolean loginApproved(String username, String password){
		User s = users.get(username);
		if(s!=null && !s.isLogin() && s.getUsername().equals(username) && s.getPassword().equals(password))
				return true;
		return false;
	}

	public void loginStudent(String username){
		User s = users.get(username);
		s.setLogin(true);
	}

	public void logoutStudent(String username){
		User s = users.get(username);
		s.setLogin(false);
	}
	 public boolean isCourseRelevant(Integer courseNum, String username){ // checks if course exists, has sits available , checks kdam courses
		Course c = courses.get(courseNum);
		User s= users.get(username);
		if(c==null || !c.isAvailable() || s.isAdmin())
			return false;
		List<Integer> studentCourseList = s.getCourseNumlist();
		for(Integer i: c.getKdamCoursesList()){
			if(!studentCourseList.contains(i))
				return false;
		}
		return true;

	 }

	 public void registerCourse(Integer courseNum,String username){ // used after the use of isCourseRelevant // synchronize!!
		 Course c = courses.get(courseNum);
		 User s= users.get(username);
		 c.register(username);
		 s.addCourse(c);
	 }

	 public String getKdamCourses(int courseNum){ //todo needs to be sorted as in the course file
		 Course c = courses.get(courseNum);
		 String toReturn = "";
		 for(Integer i:c.getKdamCoursesList()){
			 if(toReturn=="")
				 toReturn+=i;
			 else
				 toReturn+= ','+i;
		 }
		 return toReturn;
	 }

	 public boolean isAdmin(String username){
		return users.get(username).isAdmin();
	 }

	 public String getCourseStat(int courseNum){
		 Course c = courses.get(courseNum);
		 String toReturn = "Course: "+"("+courseNum+") "+c.getCourseName()+"\n";
		 toReturn+= "Seats Available: "+c.getSeatsAvailable()+"/"+c.getNumOfMaxStudents()+"\n";
		 toReturn+= "Students Registered: [";
		 boolean first=true;
		 for(String name: c.getRegisteredStudents()){
		 	if(first) {
				toReturn += name;
				first=false;
			}
		 	else
				toReturn += ","+name;
		 }
		 toReturn+="]";
		 return  toReturn;
	 }

	 public String getStudentStat(String username){ //todo ordered courses
		User s= users.get(username);
		String toReturn= "Student: "+username+'\n'+"Courses: ";
		for(int num:s.getCourseNumlist()){
			toReturn+= "["+num+"]";
		}
		return toReturn;
	 }

	 public Boolean isRegisteredCourse(String username, int courseNum){
		return users.get(username).isRegisteredCourse(courseNum);
	 }
	public Boolean unregisterCourse(String username, int courseNum){
		Course c = courses.get(courseNum);
		User s= users.get(username);
		c.unregister(username);
		s.unregisterCourse(c);
		return true;
	}

	public String getMyCourses(String username) {
		User s = users.get(username);
		boolean first = true;
		String output = "[";
		for (int num : s.getCourseNumlist()) {
			if (first) {
				output += num;
				first = false;
			} else
				output += "," + num;
		}
		output+="]";
		return output;
	}


}

