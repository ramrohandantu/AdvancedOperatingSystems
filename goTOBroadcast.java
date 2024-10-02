import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.PriorityQueue;

public class goTOBroadcast implements Runnable{
	private Thread t;
	private String threadName;
	private int nodeId;
	private BlockingQueue<String> csSnd;
	private BlockingQueue<String> csRx;
	private BlockingQueue<String> tobMsgRx;
	private BlockingQueue<String> tobMsgSnd;
	private BlockingQueue<String> tobRx;
	private BlockingQueue<String> tobSnd;
	private int numMessages;
	//private boolean l1;
	//private boolean l2;
	//private int clock;
	//private PriorityQueue<float> pq=null;
	private int nodes;
	private int[] vector;
	private boolean full;

	public goTOBroadcast(String name, int id, BlockingQueue<String> tobRx, BlockingQueue<String> tobSnd, int messages, int node
				, BlockingQueue<String> csSnd, BlockingQueue<String> csRx
				, BlockingQueue<String> tobMsgRx, BlockingQueue<String> tobMsgSnd){  
		this.threadName = name;
		this.nodeId = id;
		this.tobRx = tobRx;
		this.tobSnd = tobSnd;
		this.numMessages = messages;
		this.nodes=node;
		this.csSnd=csSnd;
		this.csRx=csRx;
		this.tobMsgRx=tobMsgRx;
		this.tobMsgSnd=tobMsgSnd;
		this.vector=new int[nodes];
		this.full=false;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			int count = 0;
			String stobRx="";
			String stobMsgRx = "";
			String scsEnter = "";
			String send="";
			while(true){
				while((stobRx=tobSnd.poll())!=null){
					csSnd.put("csEnter");
					System.out.println("Request for csEnter at node "+nodeId);
					scsEnter="";
					do{
						scsEnter=csRx.poll();
						while((stobMsgRx=tobMsgRx.poll())!=null){
							String[] word=stobMsgRx.split("!");
							if(word[0].equals("BROADCAST")){
								tobRx.put(word[2]);
								tobMsgSnd.put("ACK!"+Integer.toString(nodeId)+"!"+word[1]);
								System.out.println("Sent ACK at "+nodeId);
							}
						}
					}while(scsEnter==null || (scsEnter!=null && !scsEnter.equals("csEnter")));//Blocking call for csEnter
					
					System.out.println("Request granted at node "+nodeId);
					send = "BROADCAST!"+Integer.toString(nodeId)+"!"+stobRx;
					//clock +=1;
					resetVector();
					tobMsgSnd.put(send);
					tobRx.put(stobRx);
					updateVector(nodeId);
					do{
						while((stobMsgRx=tobMsgRx.poll())!=null){
							String[] word=stobMsgRx.split("!");
							if(word[0].equals("ACK")){
								updateVector(Integer.parseInt(word[1]));
							}
						}
					}while(!full);
					resetVector();
					csSnd.put("csExit");					
				}
				while((stobMsgRx=tobMsgRx.poll())!=null){
					String[] word=stobMsgRx.split("!");
					if(word[0].equals("BROADCAST")){
						tobRx.put(word[2]);
						tobMsgSnd.put("ACK!"+Integer.toString(nodeId)+"!"+word[1]);
					}
				}

			}
				
		}
		catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}//catch(IOException e){
		//	e.printStackTrace();
		//}
		
		System.out.println("Thread: "+threadName+" Exiting");
	}
	
	public void start(){
		System.out.println("Starting: "+threadName);
		if(t==null){
			t=new Thread(this,threadName);
			t.start();
		}
	}

	public void resetVector(){
		for(int i=0;i<nodes;i++){
			vector[i]=0;
		}
		full=false;
	}

	public void updateVector(int node){
		vector[node]=1;
		for(int i=0;i<nodes;i++){
			if(vector[i]==0){
				return;
			}
		}
		full=true;
	}
}
