import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.PriorityQueue;

public class goMutex implements Runnable{
	private Thread t;
	private String threadName;
	private int nodeId;
	private BlockingQueue<String> csSnd;
	private BlockingQueue<String> csRx;
	private BlockingQueue<String> mutexRx;
	private BlockingQueue<String> mutexSnd;
	private int clock;
	private int nodes;

	public goMutex(String name, int id, BlockingQueue<String> csSnd, BlockingQueue<String> csRx
				, BlockingQueue<String> mutexRx, BlockingQueue<String> mutexSnd, int node){  
		this.threadName = name;
		this.nodeId=id;
		this.nodes=node;
		this.csSnd=csSnd;
		this.csRx=csRx;
		this.mutexRx=mutexRx;
		this.mutexSnd=mutexSnd;
		clock=0;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			String smutexRx = "";
			String scsEnter = "";
			int id;
			int clk;
			MutualExclusion mutex = new MutualExclusion(nodes,nodeId);
			mutex.resetVector();
			while(true){
				while((scsEnter=csSnd.poll())!=null){
					if(scsEnter.equals("csEnter")){
						//mutex.resetVector();
						System.out.println("Self-Request at "+nodeId);
						clock+=1;
						mutex.putQueue(clock+nodeId/100.0);
						mutexSnd.put("REQUEST!"+Integer.toString(nodeId)+"!"+Integer.toString(clock));
						do{
							while((smutexRx=mutexRx.poll())!=null){
								String[] word=smutexRx.split("!");
								id = Integer.parseInt(word[1]);
								clk = Integer.parseInt(word[2]);
								if(clk>clock){
									clock=clk;
								}
								clock+=1;
								if(word[0].equals("REQUEST")){
									mutex.putQueue(clk+id/100.0);
									clock+=1;
									mutexSnd.put("REPLY!"+Integer.toString(nodeId)+"!"+Integer.toString(clock)+"!"
											+Integer.toString(id));
								}
								if(word[0].equals("REPLY")){
									mutex.updateVector(id);
								}
								if(word[0].equals("RELEASE")){
									System.out.println("CAME HERE!!!!!!!!!!!!!!!!!!!!");
									double get = mutex.getQueue();
								}
							}					
							//System.out.println(Boolean.toString(mutex.checkL1L2()));
						}while(!mutex.checkL1L2());
						System.out.println("csEnter "+nodeId);	
						csRx.put("csEnter");
					}
					if(scsEnter.equals("csExit")){
						//if(mutex.headQueue()!=null){
						//double get = mutex.getQueue();
						//}
						if(mutex.headQueue()>=0){
							System.out.println("get now");
							System.out.println("get "+mutex.pq.poll());
						}
						clock+=1;
						mutex.resetVector();
						mutexSnd.put("RELEASE!"+Integer.toString(nodeId)+"!"+Integer.toString(clock));						
					}
				}
				while((smutexRx=mutexRx.poll())!=null){
					String[] word=smutexRx.split("!");
					id = Integer.parseInt(word[1]);
					clk = Integer.parseInt(word[2]);
					if(clk>clock){
						clock=clk;
					}
					clock+=1;
					if(word[0].equals("REQUEST")){
						mutex.putQueue(clk+id/100.0);
						clock+=1;
						mutexSnd.put("REPLY!"+Integer.toString(nodeId)+"!"+Integer.toString(clock)+"!"+Integer.toString(id));
					}
					if(word[0].equals("RELEASE")){
						double get = mutex.pq.poll();
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
