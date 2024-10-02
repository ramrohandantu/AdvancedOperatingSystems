import java.io.*;
import java.net.*;
import java.util.*;

public class goWorker implements Runnable{
	private Thread t;
	private String threadName;
	private Socket serverSocket = null;
	private int random;
	private int nodeId;
	private String conf;
	private BufferedWriter bw=null;
	
	
	public goWorker(String name, int rand, Socket socket, int id, BufferedWriter bw, String cf){
		this.threadName = name;
		this.random = rand;
		this.serverSocket = socket;
		this.nodeId = id;
		this.bw = bw;
		this.conf = cf;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			int nextHop;
			int rand;
			int servPort;
			String servDcXX;
			int pathLength = 0;
			int selfNode = 0;
			int compNode = 0;
			//boolean flag = false;
			BufferedReader recv = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			String line;
			String path="";
			//String path1="";
			while((line = recv.readLine())!=null)
			{
				path += line;
			}
			System.out.println("NodeW "+nodeId+" received "+path);
			String[] words = path.split("!");
			pathLength = words.length;
			
			
			//Declare few more variables
			int tokenNumber = Integer.parseInt(words[0]);
			int totalTokens = Integer.parseInt(words[1]);
			rand = Integer.parseInt(words[2]);
			int hopNumber = Integer.parseInt(words[3])+1;
			int totalHops = Integer.parseInt(words[4]);
			int count = 1;

			//End Condition
			if(hopNumber > totalHops){
				//System.out.println("Node:"+nodeId+" Sum:"+rand);
				synchronized(bw){
					bw.write("Received token "+Integer.toString(tokenNumber+1)+" Token sum = "+rand);
					bw.newLine();
				}
			}else{
				//loop through path
				int start = hopNumber;
				for(int i=start;i<totalHops;i++){
					if(Integer.parseInt(words[i+5].split(":")[0])==nodeId){
						count +=1;
						hopNumber +=1;
						//flag = true;
					}
					if(Integer.parseInt(words[i+5].split(":")[0])!=nodeId){
						i=totalHops;
					}
				}
				path = "";
				for(int i=0;i<pathLength;i++){
					if(i==2){
						path += Integer.toString(count*random+rand) +"!";
					}else{
						if(i==pathLength-1){
							path += words[i];
						}else{
							if(i==3){
								path += Integer.toString(hopNumber) + "!";
							}else{
								path += words[i] + "!";
							}	
						}
					}
				}		
			
				servPort = Integer.parseInt(words[5+hopNumber].split(":")[2]);
				servDcXX = words[5+hopNumber].split(":")[1];
				goClient goC = new goClient();
				goC.setThreadName("ClientThread running on "+nodeId);
				goC.setPort(servPort);
				goC.setDcXX(servDcXX);
				goC.setPacket(path);
				goC.start();
			}
			
			
			recv.close();
			serverSocket.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}//catch(InterruptedException e){
		System.out.println("Thread: "+threadName+" Exiting");
	}
	
	public void start(){
		System.out.println("Starting: "+threadName);
		if(t==null){
			t=new Thread(this,threadName);
			t.start();
		}
	}
}
