
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author susha
 */
public class myftp{
    private Socket socket = null; 
    private Scanner  input   = null; 
    private DataOutputStream out     = null; 
    private DataInputStream in    = null;
    /**
     *
     * @param address
     * @param port
     */
        @SuppressWarnings("deprecation")
       public myftp (String address, int port) 
    { 
        try
        { 
            socket = new Socket(address, port); 
            System.out.println("Connected to the server"); 
  
            input  = new Scanner(System.in); 
  
            out    = new DataOutputStream(socket.getOutputStream()); 
            
            in = new DataInputStream( 
                    new BufferedInputStream(socket.getInputStream()));
        } 
        catch(UnknownHostException u) 
        { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
        String sending_line = ""; 
        String recieveing_line;
        recieveing_line = "";
        while (!sending_line.equals("quit")) 
        { 
        	BufferedOutputStream bos =null;
            try
            { 
            	System.out.println("myftp>");
                sending_line = input.nextLine();
                if(sending_line.contains("put")) {
                    DataOutputStream output;
                    output = new DataOutputStream(socket.getOutputStream());
                    BufferedInputStream bis = null;
                    String [] sending_lineArr=sending_line.split(" ");
        			String fileName=sending_lineArr[1];
                    File file = new File(fileName);
                    boolean exists = file.exists();
                    if(exists) {
                    	out.writeUTF(sending_line);	
                    	FileInputStream fis = new FileInputStream(file);
                    	byte[] buffer = new byte[(int)file.length()];
                    	bis = new BufferedInputStream(fis);
                    	bis.read(buffer,0,buffer.length);
                    	output.write(buffer,0,buffer.length);
                		output.flush();
                		recieveing_line = in.readUTF();
                		System.out.println(recieveing_line);
                    }
                    else {
                    	System.out.println("No such file exists");
                    }
                    
            	}
                else {
                	out.writeUTF(sending_line);	
                }
                
            } 
            catch(IOException i) 
            { 
                System.out.println(i); 
            } 
            try
            {
            	
            	if(sending_line.indexOf("get")>=0) {
            		String fileName="";
            		if(sending_line.indexOf("/")>=0) {
            			String [] sending_lineArr=sending_line.split("/");
            			fileName=sending_lineArr[sending_lineArr.length-1];
            		}
            		else {
            			String [] sending_lineArr=sending_line.split(" ");
            			fileName=sending_lineArr[1];
            		}
            		String fileCheck=recieveing_line = in.readUTF();
            		if(fileCheck.equals("true")) {
            			FileOutputStream fos = new FileOutputStream(fileName);
                		bos = new BufferedOutputStream(fos);
                		byte[] buffer = new byte[4096];
                		
                		
                		int read = in.read(buffer,0,buffer.length);
                		bos.write(buffer, 0 , read);
                		bos.flush();
                		System.out.println("Received the file "+ fileName);
                		fos.flush();
                		fos.close();
                		bos.close();
            		}
            		else {
               		 System.out.println("No such file exists");
            		}
            		
            		
            		
            	}
            	if(!(sending_line.indexOf("get")>=0 ||sending_line.indexOf("put")>=0||sending_line.indexOf("quit")>=0)) {
            		
            		recieveing_line = in.readUTF();
            		System.out.println(recieveing_line);
            	}
            	
             if(sending_line.indexOf("quit")>=0){
            	  try
                  { 
                      input.close(); 
                      out.close();
                      in.close();
                      socket.close(); 
                      System.out.println("Connection with the server has been closed");
                      System.out.println("Shutting down client....");
                  } 
            	  catch(IOException i) {
            		  System.out.println(i); 
            	  }
             }
            } 
            catch(IOException i) 
            { 
            	
                System.out.println(i); 
            } 
            
        } 
  
        // close the connection 
      
       
    } 
  
    public static void main(String args[]) 
    { 
    	System.out.println("Enter the server machine name");
    	Scanner sca=new Scanner(System.in);
    	String serverName=sca.nextLine();
    	System.out.println("Enter the port number");
    	int portNo=sca.nextInt();
    	myftp client = new myftp(serverName, portNo); 
    } 
}
