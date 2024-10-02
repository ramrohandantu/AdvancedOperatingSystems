import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class goClient implements Runnable{
	private Thread t;
	private String threadName;
	private int port;
	private String dcXX;
	private String packet;
	private BlockingQueue<String> sendQueue;
	
	public void setThreadName(String name)
	{
		this.threadName = name;
		System.out.println("Creating: "+threadName);
	}

	public void setPort(int prt)
	{
		this.port = prt;
	}

	public void setDcXX(String dc)
	{
		this.dcXX = dc;
	}

	public void setSend(BlockingQueue<String> queue)
	{
		this.sendQueue = queue;
	}

	public void run(){
		System.out.println("Running: "+threadName);
		try{
			//String message;
			//Create a client socket and connect to server at predefined port as per config file
			
			//Socket clientSocket = new Socket(dcXX,port);   // change 127 to net05
			Socket clientSocket;
			PrintWriter send;
			//clientSocket = new Socket(dcXX,port);
			//send = new PrintWriter(clientSocket.getOutputStream(),true);
			boolean scanning = true;
			while(scanning)
			{
				try
				{
					System.out.println(dcXX+" "+port);
					clientSocket = new Socket(dcXX,port);
					send = new PrintWriter(clientSocket.getOutputStream(),true);
					//System.out.println("pkt"+this.packet);

					//proj2
					String message;
					while(true){
						while((message = sendQueue.poll())!=null){
							System.out.println("Sending: at nodeId "+dcXX +" "+message);
							send.println(message);
						}
					}
				}
				catch(ConnectException e)
				{
					System.out.println("Connection failed, Waiting and trying again!!");
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException ie)
					{	
						ie.printStackTrace();
					}
				}
			}
			//PrintWriter send = new PrintWriter(clientSocket.getOutputStream(),true);
			
		}
		catch(UnknownHostException exU){
			exU.printStackTrace();
		}

		catch(IOException ex)
		{
			ex.printStackTrace();
		}//catch(InterruptedException e){
		//System.out.println("Thread: "+threadName+" Exiting");
	}
	
	public void start(){
		System.out.println("Starting: "+threadName);
		if(t==null){
			t=new Thread(this,threadName);
			t.start();
		}
	}
}
