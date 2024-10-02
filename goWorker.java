import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class goWorker implements Runnable{
	private Thread t;
	private String threadName;
	//private Socket serverSocket = null;
	//private int random;
	private int nodeId;
	private String conf;
	private BufferedWriter bw=null;
	private BlockingQueue<String> recvQueue;
	private BlockingQueue<String>[] sendQueue;
	private int[] adjMatrix;
	private int sendMax;
	private int[] parent;
	private int workerId;
	private int nodes;
	private boolean[] leaf;
	private int neighbors;
	private int[] channelState;
	private int[] recording;
	private int[] vector;
	private int snapshotDelay;
	private int[] systemState;
	private boolean[] state;
	private boolean[] snapshot;
	private int minSendDelay;
	private int minPerActive;
	private int maxNumber;
	private int[] currentNumber;
	private int[] countApplnMsg;
	private int[] protocolTermination;
	private boolean[] executing;
	private FileWriter fw=null;

	public goWorker(String name, int id, BufferedWriter w, String cf, BlockingQueue<String> rcvQueue, BlockingQueue<String>[] sndQueue
					,int[] matrix, int[] prnt, int wrkrId, int node, boolean[] lf, int neighbor, int[] vctr
					, int[] cState, int[] record, int snpshtDly, int[] sysState, boolean[] st, boolean[] snap
					, int minSndDly, int minPerActiv, int maxNumb, int[] currentNumb, int[] transmitApln, int[] protTerm
					, boolean[] exec, FileWriter f){
		this.threadName = name;
		//this.random = rand;
		//this.serverSocket = socket;
		this.nodeId = id;
		this.bw = w;
		this.conf = cf;
		this.recvQueue = rcvQueue;
		this.sendQueue = sndQueue;
		this.sendMax = 5;
		this.adjMatrix = matrix;
		this.parent = prnt;
		this.workerId = wrkrId;
		this.nodes=node;
		this.leaf=lf;
		this.neighbors=neighbor;
		this.vector = vctr;
		this.channelState=cState;
		this.recording=record;
		this.snapshotDelay = snpshtDly;
		this.systemState = sysState;
		this.state = st;
		this.snapshot = snap;
		this.minSendDelay = minSndDly;
		this.minPerActive = minPerActiv;
		this.maxNumber = maxNumb;
		this.currentNumber = currentNumb;
		this.countApplnMsg = transmitApln;
		this.protocolTermination=protTerm;
		this.executing=exec;
		this.fw=f;
		//this.LOCK = new Integer(prnt);
		System.out.println("Creating: "+threadName);
	}
	
	public void run(){
		System.out.println("Running: "+threadName);
		try{
			
			//synchronized(bw){
			//	bw.write("Hi");
			//}
			String rcvd;
			String[] token;
			String buildToken = "";
			String mapToken = "";
			String sendToken="";
			boolean setParent = false;
			boolean hasNeighbors=false;
			int countNeighbors=0;
			int[] vecMatrix = new int[nodes*nodes];
			for(int i=0;i<nodes;i++){
				for(int j=0;j<nodes;j++){
					vecMatrix[nodes*i+j]=0;
				}
			}
			//BUILD
			for(int i=0;i<nodes;i++){
				if(adjMatrix[nodes*nodeId+i]==1){
					countNeighbors+=1;
				}
			}
			if(countNeighbors>0){
				hasNeighbors=true;
			}
			if(workerId==0 && nodeId==0){
				parent[0]=0;
				buildToken = "1"+"!"+Integer.toString(nodeId)+"!"+"0";
				if(hasNeighbors==true){
					countNeighbors=0;
					for(int i=0;i<nodes;i++){
						if(adjMatrix[nodes*nodeId+i]==1){
							sendQueue[countNeighbors].put(buildToken);
							System.out.println("Sending "+buildToken+" nodeId: "+nodeId+" to "+countNeighbors);
							countNeighbors+=1;
						}
					}
				}
			}
			//ENDBUILD
			

			
			
			//CHANDY LAMPORT
			if(workerId==0 && nodeId==0){
				//t.sleep(snapshotDelay);
				for(int i=0;i<nodes;i++){
					snapshot[i]=true;
				}
				System.out.println("((((((((((((((((BEGIN SNAPSHOT))))))))))))))");
				String marker = "1"+"!"+Integer.toString(nodeId)+"!"+"1"+"!"+"0";
				synchronized(vector){
					for(int i=0;i<nodes;i++){
						systemState[i] = vector[i];
					}
				}
				//for(int i=0;i<nodes;i++){
				//	protocolTermination[i]=0;
				//}
				for(int i=0;i<neighbors;i++){					
					recording[i]=0;
					channelState[i]=0;
					sendQueue[i].put(marker);
				}
			}
			//END CHANDY LAMPORT

			


			//MAP
			Random rgen = new Random();
			int rand=0;
			for(int i=0;i<nodes;i++){
				protocolTermination[i]=0;
			}

			if(workerId==0 && nodeId==0){
				state[0]=true;
				loop:{
					if(currentNumber[0]<minPerActive && countApplnMsg[0]<maxNumber && state[0]==true){
						synchronized(vector){
							rand = rgen.nextInt(neighbors);
							System.out.println("FFFFFFFFFFFFFF at node "+nodeId+" rand is "+rand);
							mapToken = "0" + "!"+nodeId+"!";
							vector[nodeId]+=1;
							for(int i=0;i<nodes;i++){
								if(i==nodes-1){
									mapToken += Integer.toString(vector[i]);
								}else{
									mapToken += Integer.toString(vector[i])+":";
								}
							}		
							sendQueue[rand].put(mapToken);
							countApplnMsg[0]+=1;
							currentNumber[0]+=1;
							if(currentNumber[0]==minPerActive || countApplnMsg[0]==maxNumber){
								state[0]=false;
								currentNumber[0]=0;
								break loop;
							}
						}
						t.sleep(minSendDelay);					
					}
				}
			}

			//END MAP


			while(true&&executing[0]){  
				while((rcvd=recvQueue.poll())!=null){
					token = rcvd.split("!");
					//BUILD
					setParent=false;
					hasNeighbors=false;
					countNeighbors=0;
					for(int i=0;i<nodes;i++){
						if(adjMatrix[nodes*nodeId+i]==1 && i!=Integer.parseInt(token[1])){
							countNeighbors+=1;
						}
					}
					if(countNeighbors>0){
						hasNeighbors=true;
					}
					if(token.length==3 && Integer.parseInt(token[0])==1 && Integer.parseInt(token[2])==0){
						synchronized(parent){
							if(parent[0]==-1){
								parent[0]=Integer.parseInt(token[1]);
								System.out.println("<<<<<<<<Parent of "+nodeId+ " is "+parent[0]);
								setParent=true;
							}else{
								setParent=false;
							}
						}
						if(setParent==true){
							if(hasNeighbors==true){
								countNeighbors=0;
								for(int i=0;i<nodes;i++){
									if(adjMatrix[nodes*nodeId+i]==1){
										if(i!=Integer.parseInt(token[1])){
											sendToken = "1"+"!"+Integer.toString(nodeId)+"!"+"0";
											sendQueue[countNeighbors].put(sendToken);
										}
										countNeighbors+=1;
									}
								}
							}else{
								leaf[0]=true;
							}
						}
					}
					//ENDBUILD
					
					//MAP
					int msgNodeId = Integer.parseInt(token[1]);
					int msgNodeIndex = 0;
					searchA:{
						for(int j=0;j<nodes;j++){
							if(adjMatrix[nodes*nodeId+j]==1){
								if(j==msgNodeId){
									break searchA;
								}
								msgNodeIndex+=1;
							}
						}
					}
					int[] rcvdVector=new int[nodes];
					if(Integer.parseInt(token[0])==0){
						
						synchronized(vector){
						vector = updateVector(vector, rcvdVector, nodes, Integer.parseInt(token[1]));
						}
						System.out.println("At Node "+nodeId+" "+Arrays.toString(recording));
						
						
						if(recording[msgNodeIndex]==0){
							channelState[msgNodeIndex]+=1;
							//System.out.println("channel state at node "+nodeId+" from node "+Integer.parseInt(token[1])+" is "
							//+Arrays.toString(channelState));
						}
						
						if(state[0]==false && countApplnMsg[0]<maxNumber){
							state[0]=true;
							String[] rcvdVec = token[2].split(":");
							for(int i=0;i<nodes;i++){
								rcvdVector[i]=Integer.parseInt(rcvdVec[i]);
							}
						}
						loop1:{
							if(currentNumber[0]<minPerActive && countApplnMsg[0]<maxNumber && state[0]==true){
								synchronized(vector){
									rand = rgen.nextInt(neighbors);
									System.out.println("FFFFFFFFFFFFFF at node "+nodeId+" rand is "+rand);
									mapToken = "0" + "!"+nodeId+"!";
									vector[nodeId]+=1;
									for(int i=0;i<nodes;i++){
										if(i==nodes-1){
											mapToken += Integer.toString(vector[i]);
										}else{
											mapToken += Integer.toString(vector[i])+":";
										}
									}	
									sendQueue[rand].put(mapToken);
									countApplnMsg[0]+=1;
									currentNumber[0]+=1;
									if(currentNumber[0]==minPerActive || countApplnMsg[0]==maxNumber){
										state[0]=false;
										currentNumber[0]=0;
										break loop1;
									}
								}
								t.sleep(minSendDelay);					
							}
						}
						
					}
					//END MAP
					


					//CHANDY LAMPORT
					
					int parentIndex=0;
					searchparent:{
						for(int i=0;i<nodes;i++){
							if(adjMatrix[nodes*nodeId+i]==1){
								if(parent[0]==i){
									break searchparent;
								}
								parentIndex+=1;
							}
						}
					}
					if(nodeId==0){
						parentIndex=-1;
					}
					System.out.println("66666666666 at node "+nodeId+" has parent "+parentIndex);
					boolean firstMarker = false;
					int incomingNodeId = 0;
					String secondMarker = "";
					String snapshotData = "";
					
					if(token.length==4 && Integer.parseInt(token[0])==1 && Integer.parseInt(token[2])==1 && Integer.parseInt(token[3])==0){
						search: {
							for(int i=0;i<neighbors;i++){
								if(recording[i]==0){
									break search;
								}
							}
							firstMarker=true;
							System.out.println(">>>>>>>>>>>RECEIVED FIRST MARKER AT NODE "+nodeId);
							//if(neighbors==1){
							//	firstMarker=false;

							//}
						}
						if(firstMarker==true){
							if(nodeId != 0){
								//recording[nodeId]=0;
								//channelState[nodeId]=0;
								synchronized(vector){
									for(int i=0;i<nodes;i++){
										systemState[i] = vector[i];
										
									}
									
								}
								for(int i=0;i<neighbors;i++){
									recording[i]=0;
									channelState[i]=0;
								}
								recording[msgNodeIndex]=-1;
							}
							secondMarker= "1"+"!"+Integer.toString(nodeId)+"!"+"1"+"!"+"0";
							
							for(int i=0;i<neighbors;i++){
								sendQueue[i].put(secondMarker);
								//System.out.println("dddddddddddddddddSent Marker to "+i+ " from " +nodeId);
							}
						}
						if(firstMarker==false || (neighbors==1 && nodeId!=0) ){
							System.out.println("><><><><><><><><>RECEIVED SECOND MARKER AT NODE "+nodeId);
							recording[msgNodeIndex]=-1;
							
							search1: {
								for(int i=0;i<neighbors;i++){
									if(recording[i]==0){							
										break search1;
									}
								}
							
								snapshotData= "1"+"!"+Integer.toString(nodeId)+"!"+"1"+"!"+"1"+"!";
								for(int i=0;i<nodes;i++){
									if(i==nodes-1){
										snapshotData+=Integer.toString(systemState[i]);
									}else{
										snapshotData+=Integer.toString(systemState[i])+":";
									}
								}
								snapshotData += "!";
								System.out.println("channel state at node "+nodeId+" from node "+Integer.parseInt(token[1])+" is "
									+Arrays.toString(channelState));
								synchronized(channelState){
									for(int i=0;i<neighbors;i++){
										if(i==neighbors-1){
											snapshotData+=Integer.toString(channelState[i]);
										}else{
											snapshotData+=Integer.toString(channelState[i])+":";
										}
									}
								}
								snapshotData += "!";
								snapshotData += Boolean.toString(state[0]);
								System.out.println("************** Snapshot complete at "+nodeId);
								System.out.println("************** Sending snapshot from "+nodeId+" to "+parentIndex);
								System.out.println("*************"+snapshotData);
								synchronized(bw){
									//bw.write(Arrays.toString(vector));
									for(int j=0;j<nodes;j++){
										bw.write(Integer.toString(systemState[j])+" ");

									}
									bw.newLine();
									
								}
								//bw.close();
								//fw.close();
								if(parentIndex!=-1){
									sendQueue[parentIndex].put(snapshotData);
									System.out.println("countApplnMsg of node "+nodeId+" is "+countApplnMsg[0]);
									for(int i=0;i<neighbors;i++){
										channelState[i]=-1;
									}

								
								}else{//for node 0,
									System.out.println("Snapshot of node 0 :"+snapshotData);
									System.out.println("countApplnMsg of node "+nodeId+" is "+countApplnMsg[0]);

									snapshot[0]=false;
									System.out.println("@@@@@@@@@@"+snapshotData+" "+token.length);
									
									String[] checkChannel = snapshotData.split("!")[5].split(":");
									String[] systState = snapshotData.split("!")[4].split(":");
									for(int j=0;j<nodes;j++){
										vecMatrix[nodes*nodeId+j]=Integer.parseInt(systState[j]);
									}
									//protocolTermination[0]=-1;
									loop2:{
										for(int i=0;i<checkChannel.length;i++){
											if(Integer.parseInt(checkChannel[i])!=0){
												break loop2;
											}
										}
										if(Boolean.parseBoolean(snapshotData.split("!")[6])==true){
											break loop2;
										}
										protocolTermination[0]=-1;
										System.out.println("&&&&& Node 0 has terminated");
									}
								}
							}
						}
												
					
					}

					if(token.length==7 && Integer.parseInt(token[0])==1 && Integer.parseInt(token[2])==1 && Integer.parseInt(token[3])==1){
						if(nodeId != 0){
							sendQueue[parentIndex].put(rcvd);
							System.out.println("Sending snapshot to parent "+parentIndex+" at node "+nodeId);
						}else{
							System.out.println("Snapshot of node "+token[1]+" :"+rcvd);
							snapshot[Integer.parseInt(token[1])]=false;
							String[] checkChannel = token[5].split(":");
							//protocolTermination[Integer.parseInt(token[1])]=-1;
							String[] systState = token[4].split(":");
							for(int j=0;j<nodes;j++){
								vecMatrix[nodes*nodeId+j]=Integer.parseInt(systState[j]);
							}
							loop2:{
								for(int i=0;i<checkChannel.length;i++){
									if(Integer.parseInt(checkChannel[i])!=0){
										break loop2;
									}
								}
								if(Boolean.parseBoolean(token[6])==true){
									break loop2;
								}
								protocolTermination[Integer.parseInt(token[1])]=-1;
								System.out.println("&&&&& Node "+token[1]+" has terminated");
							}
						}
					}
					
					//END CHANDY LAMPORT
					

					//FLOOD
					if(Integer.parseInt(token[0])==2){
					//if(token.length==1){
						System.out.println("FINISH received at "+nodeId);
						String flood="2!"+nodeId;
						for(int i=0;i<neighbors;i++){
							sendQueue[i].put(flood);
						}
						/*synchronized(bw){
							synchronized(vector){
								//bw.write(Arrays.toString(vector));
								for(int j=0;j<nodes;j++){
									bw.write(Integer.toString(vector[j])+" ");
									
								}
								bw.newLine();
							}
						}*/
						bw.close();
						fw.close();
						executing[0]=false;
					}

					//END FLOOD
				}
				
				//MAP
				while(currentNumber[0]<minPerActive && countApplnMsg[0]<maxNumber && state[0]==true){
					synchronized(vector){
						rand = rgen.nextInt(neighbors);
						System.out.println("FFFFFFFFFFFFFF at node "+nodeId+" rand is "+rand);
						mapToken = "0" + "!"+nodeId+"!";
						vector[nodeId]+=1;
						for(int i=0;i<nodes;i++){
							if(i==nodes-1){
								mapToken += Integer.toString(vector[i]);
							}else{
								mapToken += Integer.toString(vector[i])+":";
							}
						}		
						sendQueue[rand].put(mapToken);
						countApplnMsg[0]+=1;
						currentNumber[0]+=1;
						if(currentNumber[0]==minPerActive || countApplnMsg[0]==maxNumber){
							state[0]=false;
							currentNumber[0]=0;
							//break loop;
						}
					}
					t.sleep(minSendDelay);					
				}
				//END MAP

				//Re-initiate snapshot
				if(nodeId==0 && workerId==0){
					searchB:{
						for(int i=0;i<nodes;i++){
							if(snapshot[i]==true){
								//System.out.println("^^^^^^^^^^^Snapshot is true "+i);
								break searchB;
							}
						}
						searchC:{
							for(int i=0;i<nodes;i++){
								if(protocolTermination[i]==0){
									//synchronized(bw){
									//	bw.newLine();
										/*for(int j=0;i<nodes;i++){
											bw.write(Integer.toString(vector[j]));
										}*/
									//	bw.write("test");
									//}
									System.out.println("(((((((((((((((((((PENDING)))))))))))))))))))");
									break searchC;
								}
							}
							if(!checkConsistency(vecMatrix,nodes)){
								
								System.out.println("(((((((((((((((((((PENDING)))))))))))))))))))");
								break searchC;
							}
							//synchronized(bw){
							//	bw.newLine();
								/*for(int i=0;i<nodes;i++){
									bw.write(Integer.toString(vector[i]));
								}*/
							//	bw.write("test1");
							//}
							System.out.println("(((((((((((((((((TERMINATED)))))))))))))))))");
							String flood = "2!"+nodeId;
							for(int i=0;i<neighbors;i++){
								sendQueue[i].put(flood);
							}
							/*synchronized(bw){
								synchronized(vector){
									//bw.write(Arrays.toString(vector));
									for(int j=0;j<nodes;j++){
										bw.write(Integer.toString(vector[j])+" ");
									}
									bw.newLine();
								}
							}*/
							bw.close();
							fw.close();
							executing[0]=false;
						}
						System.out.println("((((((((((((((((END SNAPSHOT))))))))))))))");
						t.sleep(snapshotDelay);
						for(int i=0;i<nodes;i++){
							snapshot[i]=true;
						}
						System.out.println("((((((((((((((((BEGIN SNAPSHOT))))))))))))))");
						String marker = "1"+"!"+Integer.toString(nodeId)+"!"+"1"+"!"+"0";
						systemState = vector;
						for(int i=0;i<neighbors;i++){					
							recording[i]=0;
							channelState[i]=0;
							sendQueue[i].put(marker);
						}
					}
				}
			}
			
				
			
			
			
			
			/*String rcvd;
			String send = "1234567";
			//send += "41424344454647484950515253545556575859606162636465666768697071727374757677787980";
			//send += "81828384858687888990919293949596979899100";
			String EOS = "@";
			sendQueue[0].put(send);
			String sendAgain="";
			//sendQueue.put(EOS);
			while(sendMax>0){
				while((rcvd=recvQueue.poll())!=null){
					System.out.println(rcvd);
					if(rcvd.length()>1){
						sendAgain = rcvd.substring(0,(rcvd.length()-1));
					}
					if(sendMax>0){
						sendQueue[0].put(sendAgain);
						//sendQueue.put(EOS);
						sendMax-=1;
					}
				}
			}*/

			//Build


			
			
			/*
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
			*/
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

	public int[] updateVector(int[] vec1, int[] vec2, int size, int index){
		
		for(int i=0;i<size;i++){
			if(vec1[i]>=vec2[i]){
				//do nothing
			}else{
				vec1[i]=vec2[i];
			}
		}
		vec1[index]+=1;
		return vec1;
	}

	public boolean checkConsistency(int[] vecMatrix, int nodes){
		int[] arr = new int[nodes];
		for(int i=0;i<nodes;i++){
			arr[i]=0;
		}
		for(int i=0;i<nodes;i++){
			for(int j=0;j<nodes;j++){
				if(vecMatrix[nodes*i+j]>arr[i]){
					arr[i]=vecMatrix[nodes*i+j];
				}				
			}
		}
		for(int i=0;i<nodes;i++){
			if(arr[i]!=vecMatrix[nodes*i+i]){
				return false;
			}
		}
		return true;
	}

}
