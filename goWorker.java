import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class goWorker implements Runnable{
	private Thread t;
	private String threadName;
	private int nodeId;
	private BlockingQueue<String> recvQueue;
	private BlockingQueue<String>[] sendQueue;
	private BlockingQueue<String> tobMsgRx;
	private BlockingQueue<String> tobMsgSnd;
	private BlockingQueue<String> mutexRx;
	private BlockingQueue<String> mutexSnd;
	private int nodes;
	private int[] adjMatrix;

	public goWorker(String name, int id, BlockingQueue<String> rcvQueue, BlockingQueue<String>[] sndQueue, int[] matrix, int node
					, BlockingQueue<String> tobMsgRx, BlockingQueue<String> tobMsgSnd
					, BlockingQueue<String> mutexRx, BlockingQueue<String> mutexSnd){  
		this.threadName = name;
		this.nodeId = id;
		this.recvQueue = rcvQueue;
		this.sendQueue = sndQueue;
		this.adjMatrix = matrix;
		this.nodes = node;
		this.tobMsgRx = tobMsgRx;
		this.tobMsgSnd = tobMsgSnd;
		this.mutexRx = mutexRx;
		this.mutexSnd = mutexSnd;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			String stobMsgSnd="";
			String smutexSnd="";
			String srcvQueue="";
			String[] word;
			int count;
			while(true){
				while((srcvQueue=recvQueue.poll())!=null){
					word = srcvQueue.split("!");
					if(word[0].equals("ACK")){
						tobMsgRx.put(srcvQueue);
					}
					if(word[0].equals("BROADCAST")){
						tobMsgRx.put(srcvQueue);
					}
					if(word[0].equals("REQUEST")){
						mutexRx.put(srcvQueue);
					}
					if(word[0].equals("REPLY")){
						mutexRx.put(srcvQueue);
					}
					if(word[0].equals("RELEASE")){
						mutexRx.put(srcvQueue);
					}
				}
				while((stobMsgSnd=tobMsgSnd.poll())!=null){
					word = stobMsgSnd.split("!");
					if(word[0].equals("BROADCAST")){
						for(int i=0;i<nodes-1;i++){
							sendQueue[i].put(stobMsgSnd);
						}
					}
					if(word[0].equals("ACK")){
						count=0;
						search:{
							for(int j=0;j<nodes;j++){
								if(adjMatrix[nodes*nodeId+j]==1){
									if(Integer.parseInt(word[2])==j){
										sendQueue[count].put(stobMsgSnd);
										break search;
									}
									count+=1;
								}
							}
						}
					}					
				}
				while((smutexSnd=mutexSnd.poll())!=null){
					word=smutexSnd.split("!");
					if(word[0].equals("REQUEST")){
						for(int i=0;i<nodes-1;i++){
							sendQueue[i].put(smutexSnd);
						}
					}
					if(word[0].equals("RELEASE")){
						for(int i=0;i<nodes-1;i++){
							sendQueue[i].put(smutexSnd);
						}
					}
					if(word[0].equals("REPLY")){
						count=0;
						search:{
							for(int j=0;j<nodes;j++){
								if(adjMatrix[nodes*nodeId+j]==1){
									if(Integer.parseInt(word[3])==j){
										sendQueue[count].put(smutexSnd);
										break search;
									}
									count+=1;
								}
							}
						}
						/*count=0;
						search1:{
							for(int j=0;j<nodes;j++){
								if(nodeId!=j){
									if(Integer.parseInt(word[3])==j){
										sendQueue[count].put(smutexSnd);
									}
									count+=1;
								}
							}
						}*/
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

}
