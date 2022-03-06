
//ChatClient.java
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class ChatClient {
    
    
	String my_Name;			//my_Name client username or userid		
    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JButton send=new JButton("Send");
    JButton connect=new JButton("Start Chat");
    JButton skip=new JButton("Skip");
    RSA rsa;
    JPanel panel=new JPanel();
    JTextArea messageArea = new JTextArea(20,75);
    JPanel onlineArea = new JPanel();
    Label connectedPersonLabel;
    BigInteger connectedE;
    BigInteger connectedN;
    
    
    byte[] encrypt(String x) {		//call rsa from here
    	/*int size = x.length();
        System.out.println(x);
        char[] myNameChars = x.toCharArray();
        for(int i=0;i<size;i++)
        {
            //int temp = myNameChars[i];
            System.out.println("before"+myNameChars[i]);
            myNameChars[i] = (char)((int)(myNameChars[i]) + 3);
            System.out.println("after"+myNameChars[i]);
        }
        
        x = String.valueOf(myNameChars);    //convers chararray to string
        System.out.println(x);
        System.out.println("Encoded String "+ x);
        return x;*/
    	
    	return rsa.encrypt(x.getBytes(),connectedE,connectedN);
        }
    
    
    void onExit() {
  	  //System.err.println("Exit");
  	  out.println("skip");
  	  System.exit(0);
  	}
	
    
    void sendMessage() {
    		String x=textField.getText();
            if(x.length()==0) {
            	return;
            }
            messageArea.append(my_Name+": "+x + "\n");
            
            byte[] encryptedmsg=encrypt(x);
            x="$"+x;
            out.println("$"+encryptedmsg.length);
            System.out.println(rsa.bytesToString(encryptedmsg));
            for(byte b:encryptedmsg)
            out.println(b);
            textField.setText("");
    }
 
    
    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
        textField.setEditable(false);
        messageArea.setEditable(false);
        send.setVisible(false);
        skip.setVisible(false);
        panel.add(textField);
        panel.add(send);
        panel.add(skip);
         panel.add(connect);
        
        connect.setBackground(Color.green); skip.setForeground(Color.white); skip.setBackground(Color.red);  send.setBackground(Color.cyan);//setting colors for buttons  
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(onlineArea, BorderLayout.NORTH);
        frame.getContentPane().add(panel,BorderLayout.SOUTH);
        frame.pack();
        //action listner for enter to send text 
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	sendMessage();   
            }
        });
        
      //action listner for send button to send text 
        send.addActionListener(new ActionListener(){  
    public void actionPerformed(ActionEvent e){
    	sendMessage();    }    }); 
        
      //action listner for connect button to start chat 
        connect.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e){
            	if(connectedPersonLabel!=null)
            	onlineArea.remove(connectedPersonLabel);
            	messageArea.setText("");
            	connectedPersonLabel=new Label("Connecting... ");
           	 Font font = new Font("Courier", Font.BOLD,20);
           	connectedPersonLabel.setFont(font);
            onlineArea.revalidate();    
           	
           	onlineArea.add(connectedPersonLabel);
            	out.println("connect");    }    });
        
        ////action listner for skip button to skip connected user 
        skip.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e){
            	out.println("skip");    }    }); 
      
}
    
    //taking unique username from client
    private String getUserName(int noOfTimesInputTaken ) {
    	String userName="";
    	if(noOfTimesInputTaken==0)
    			 userName= JOptionPane.showInputDialog(frame, "Enter Unique User Name", "UserName",
                JOptionPane.PLAIN_MESSAGE);
    	if(userName.isEmpty()||noOfTimesInputTaken>0)
        {
        	userName= JOptionPane.showInputDialog(frame, "Please Enter valid and Unique UserName", "UserName",
	                JOptionPane.PLAIN_MESSAGE);
        }
    	return userName;
    }
    

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            int noOfTimesInputTaken=-1;
            while (in.hasNextLine()) {
            	noOfTimesInputTaken++;
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                	out.println(getUserName(noOfTimesInputTaken));
                } else if (line.startsWith("NAMEACCEPTED")) {
                	rsa = new RSA();
                  	out.println(rsa.getE());
                  	out.println(rsa.getN());
                  	my_Name=line.substring(13);
                    this.frame.setTitle("Chatter - " + my_Name);
                    textField.setVisible(false);
                    send.setVisible(false);
                    skip.setVisible(false);
                } else if (line.startsWith("MESSAGE")) {
                	
                    int byteArrLen=Integer.parseInt(line.substring(line.indexOf(':')+2));
                  //decryption part  
                    //change to rsa
                    byte[] byteArr=new byte[byteArrLen];
              	  
              	  	for(int i=0;i<byteArrLen;i++)
	              	  {
	              		//  System.out.println(i);
							byteArr[i]=in.nextByte();
	              	  }
              	  	byteArr=rsa.decrypt(byteArr);
              	  	
                    /*char[] myNameChars = x.toCharArray();
                    
                    for(int i=0;i<size;i++)
                    {
                        myNameChars[i] = (char)((int)(myNameChars[i]) - 3);
                    }
                    
                    x = String.valueOf(myNameChars);    //convers chararray to string
                    
                    //System.out.println("Decoded String "+ x);
                    x=line.substring(8,line.indexOf(':')+1)+x;*/
                    
                    
                    messageArea.append(line.substring(8,line.indexOf(':')+1)+new String(byteArr) + "\n");
                    
                    
                }
                else if (line.startsWith("ONLINEMESSAGE")) {
                    onlineArea.removeAll();
                    Label onlinecountlabel=new Label ("Online count:"+line.substring(14));
                    onlineArea.setLayout(new BorderLayout());
                    onlineArea.revalidate();
                    onlineArea.setSize(200, 100);  
                    //to make online count and connected status bold and increase fornt size
                    Font font = new Font("Courier", Font.BOLD,20);
                    onlinecountlabel.setFont(font);
                    onlineArea.add(onlinecountlabel,BorderLayout.EAST);
                }
                else if (line.startsWith("CONNECTEDMSG")) {
                	connectedE = in.nextBigInteger();
                	connectedN = in.nextBigInteger();
                	messageArea.setText("");
                	connect.setVisible(false);
                	textField.setVisible(true);
                    send.setVisible(true);
                    skip.setVisible(true);
                    textField.setEditable(true);
                    onlineArea.remove(connectedPersonLabel);
                	 connectedPersonLabel=new Label("You are connected to "+line.substring(12));
                	 Font font = new Font("Courier", Font.BOLD,20);
                     connectedPersonLabel.setFont(font);
                     onlineArea.revalidate();
                	onlineArea.add(connectedPersonLabel);
                	
                }
                else if (line.startsWith("DISCONNECTEDMSG")) {
                	connect.setVisible(true);
                	textField.setVisible(false);
                    send.setVisible(false);
                    skip.setVisible(false);
                    onlineArea.revalidate();
                    onlineArea.remove(connectedPersonLabel);
                	connectedPersonLabel=new Label("You are disconned ");
                	 Font font = new Font("Courier", Font.BOLD,20);
                     connectedPersonLabel.setFont(font);
                    onlineArea.add(connectedPersonLabel);
                }
              } 
            }
         finally {
        	 System.out.println("this is finally block");
            frame.setVisible(false);
            frame.dispose();
        }
}
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        
        var client = new ChatClient(args[0]);
        
       // client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        //action lister for close button to know disconnect the opp person
        client.frame.addWindowListener(new WindowAdapter() {
        	   public void windowClosing(WindowEvent evt) {
        	     client.onExit();
        	   }
        	  });
        client.frame.setVisible(true);
        client.run();
    }
}
