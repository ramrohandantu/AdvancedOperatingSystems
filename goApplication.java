import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class goApplication implements Runnable{
	private Thread t;
	private String threadName;
	private int nodeId;
	private BlockingQueue<String> tobRx;
	private BlockingQueue<String> tobSnd;
	private FileWriter fw;
	private BufferedWriter bw;
	private int delay;
	private String config_file;
	private int numMessages;
	private int nodes;

	public goApplication(String name, int id, BufferedWriter bw, FileWriter fw, int delay, String config 													, int messages, BlockingQueue<String> tobRx, BlockingQueue<String> tobSnd, int node){
		this.threadName = name;
		this.nodeId = id;
		this.tobRx = tobRx;
		this.tobSnd = tobSnd;
		this.fw = fw;
		this.bw = bw;
		this.delay= delay;
		this.config_file = config;
		this.numMessages = messages;
		this.nodes=node;
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			int count = 0;
			int writeCount = 0;
			String num="";
			while(count < numMessages){
				Random rand = new Random();
				int next = rand.nextInt(10000);
				tobSnd.put(Integer.toString(next));
				System.out.println("Random number "+next+" sending at"+nodeId);
				while((num=tobRx.poll())!=null){
					bw.write(num);
					System.out.println("1Writing at "+nodeId+" "+num);
					bw.newLine();
					writeCount +=1;
				}
				count +=1;
				Thread.sleep(delay);
			}
			while(writeCount < (numMessages*nodes)){
				
				while((num=tobRx.poll())!=null){
					bw.write(num);
					System.out.println("2Writing at "+nodeId+" "+num);
					bw.newLine();
					//bw.newLine();
					writeCount +=1;
				}
			}
			bw.close();
			fw.close();

						
		}
		catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
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
