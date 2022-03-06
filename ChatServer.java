


//ChatServer.java

import java.io.IOException;
import java.math.BigInteger;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;


public class ChatServer {

  // All client names, so we can check for duplicates upon registration.
  private static Set<String> userNames = new HashSet<>();
  
  private static Map<String,PrintWriter> usersAndPrintWrinter = new HashMap<>();
  
  //waitingList is for persons who are wating for connection
  private static ArrayList<String> waitingList=new ArrayList<String>();
  
  private static Map<String,String> pairedUsers = new HashMap<>();
  
  private static Map<String,List<BigInteger>> keys = new HashMap<>();
  
  
  public static void main(String[] args) throws Exception { 
  	System.out.println("The chat server is running...");
      var pool = Executors.newFixedThreadPool(500);   
      try (var listener = new ServerSocket(59001)) {
          while (true) {
              pool.execute(new Handler(listener.accept()));
          }
      }
  }

  
  private static class Handler implements Runnable {
      private String userName;
      private Socket socket;
      private Scanner in;
      private PrintWriter out;
      public Handler(Socket socket) {
          this.socket = socket;
      }

      
      public void run() {
          try {
              in = new Scanner(socket.getInputStream());
              out = new PrintWriter(socket.getOutputStream(), true);

              // Keep requesting a name until we get a unique one.
              //userName-->username
              //userNames-->list of people online
              while (true) {
                  out.println("SUBMITNAME");
                  userName = in.nextLine();
                  /*if (name == null) {  taking empty name as user name is already verified on client side so these 3 lines are commented
                      return;
                  }*/
                  synchronized (userNames) {
                      if (!userName.isBlank() && !userNames.contains(userName)) {
                          userNames.add(userName);
                          break;
                      }
                  }
              }
              out.println("NAMEACCEPTED " + userName);
              
              // using hashmap1
              usersAndPrintWrinter.put(userName,out);
              pairedUsers.put(userName, null);
              keys.put(userName, getKeys());
              
              for (PrintWriter writer : usersAndPrintWrinter.values()) {
              	writer.println("ONLINEMESSAGE " + userNames.size());
              }
              
              // Accept messages from this client and broadcast them.
              while (true) {
                  String input = in.nextLine();
                  
                  System.out.println(input);
                  if(input.startsWith("$"))
                  {
                	  
                	  int byteArrLen=Integer.parseInt(input.substring(1));
                	  
                	  
                	  
                	// System.out.println((new RSA().bytesToString(byteArr)));
                      PrintWriter writer;
                      String receiver=pairedUsers.get(userName);
                      //input=input.substring(input.indexOf("$")+1);
                      writer=usersAndPrintWrinter.get(receiver);
                      writer.println("MESSAGE "+ userName +": "+byteArrLen);
                      
                      //sender screen should also show same msg
                      //mistake
                     /* writer=usersAndPrintWrinter.get(userName);
                    	writer.println("MESSAGE " + userName +": "+input);*/
                      
                      
                      
                	  //byte[] byteArr=new byte[byteArrLen];
                	  
                	  for(int i=0;i<byteArrLen;i++)
                	  {
                		//  System.out.println(i);
						//byteArr[i]=in.nextByte();
						writer.println(in.nextByte());
                	  }
                	  
                    
                      
                  }
                  else if(input.contains("connect"))
                  {
                  	if(waitingList.isEmpty())
                  	{
                  	waitingList.add(userName);
                  	}
                  	else
                  	{
                  		
                  		String pairUserName=waitingList.remove(0);
                  		if(userName.equals(pairUserName))
                  		{
                  			waitingList.add(userName);
                  		}
                  		else if(!userName.equals(pairUserName))
                  		{
                  		pairedUsers.put(userName, pairUserName);
                  		pairedUsers.put(pairUserName,userName);
                  		usersAndPrintWrinter.get(userName).println("CONNECTEDMSG"+pairUserName);
                  		usersAndPrintWrinter.get(userName).println(keys.get(pairUserName).get(0));
                  		usersAndPrintWrinter.get(userName).println(keys.get(pairUserName).get(1));
                  		usersAndPrintWrinter.get(pairUserName).println("CONNECTEDMSG"+userName);
                  		usersAndPrintWrinter.get(pairUserName).println(keys.get(userName).get(0));
                  		usersAndPrintWrinter.get(pairUserName).println(keys.get(userName).get(1));//doubt
                  		}
                  	}
                  	
                  	//to print hashmap and waitinglist
                  	System.out.println("Iterating Hashmap...");  
                  	   for(Map.Entry m : pairedUsers.entrySet()){    
                  	    System.out.println(m.getKey()+" "+m.getValue());    
                  	   } 
                  	 System.out.println("waiting list");
                  	 System.out.println(waitingList);
                  }
                  else if(input.contains("skip"))
                  {
                  	String pairUserName=pairedUsers.get(userName);
                  	pairedUsers.put(userName,null);
                  	pairedUsers.put(pairUserName,null);
                  	usersAndPrintWrinter.get(userName).println("DISCONNECTEDMSG");
              		usersAndPrintWrinter.get(pairUserName).println("DISCONNECTEDMSG");
              	}
               }
          } catch (Exception e) {
              System.out.println(e);
          } finally {
              if (userName != null) {
                  System.out.println(userName + " is leaving");
                  userNames.remove(userName);
                  usersAndPrintWrinter.remove(userName);
                  pairedUsers.remove(userName);
                  System.out.println("this is after disconnections");
                  //to print 2nd hashmap.
                  /*for(Map.Entry m : pairedUsers.entrySet()){    
              	    System.out.println(m.getKey()+" "+m.getValue());    
              	   }*/
                 for (PrintWriter writer : usersAndPrintWrinter.values()) {
                      writer.println("ONLINEMESSAGE " + userNames.size());//doubt
                  }
              }
              try {
                  socket.close();
              } catch (IOException e) {
              }
          }
      }


		private List<BigInteger> getKeys() {
			// TODO Auto-generated method stub
			List<BigInteger> keyList = new ArrayList<>();
      
			keyList.add(in.nextBigInteger());	//got E
			keyList.add(in.nextBigInteger());	//got N
			System.out.println(keyList);
			return keyList;
		}
  }
}


