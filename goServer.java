import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class goServer implements Runnable{
	private Thread t;
	private String threadName;
	private int port;
	private int nodeId;
	//private int random;
	//private String conf;
	//private BufferedWriter bw=null;
	private BlockingQueue<String> recvQueue;
	
	public goServer(String name, int prt, int node, BlockingQueue<String> queue){
		//this.bw = bw;
		this.threadName = name;
		this.port = prt;
		this.nodeId = node;
		//this.random = rand;
		//this.conf = cf;
		this.recvQueue = queue;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		
		String message="Hello from server";
		//Create a server socket at predefined port according to configuration file
		try{
			ServerSocket serverSock = new ServerSocket(port);
			//Server goes into a permanent loop accepting connections from clients			
			int count=0;
			while(true)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				//ServerSocket serverSock = new ServerSocket(port);
				
				Socket clientSocket = null;
				try{
					System.out.println("nodeId:"+nodeId+"count:"+count);
					clientSocket = serverSock.accept();
					System.out.println("2nodeId:"+nodeId+"count:"+count);
					count +=1;
				}catch(IOException ex){
					ex.printStackTrace();
				}
				new Thread(new goListener("ListenerThread"+nodeId, clientSocket, nodeId, recvQueue)).start();
			}
		}catch(IOException ex1){
			ex1.printStackTrace();
		}
	}
	
	public void start(){
		System.out.println("Starting: "+threadName);
		if(t==null){
			t=new Thread(this,threadName);
			t.start();
		}
	}


}
