import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;



public class Project2
{
	public static int port;
	public static int nodes;
	public static int nodeId;
	public static int minPerActive;
	public static int maxPerActive;
	public static int minSendDelay;
	public static int snapshotDelay;
	public static int maxNumber;
	public static boolean[] leaf;
	public static int[] parent;
	
	private static int nextHop;
	private static int servPort;
	private static String servDcXX;
	

	public static int randInt(int min, int max)
	{
		Random rand = new Random();
		int randomNum = rand.nextInt((max-min) + 1) + min;
		return randomNum;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String args[])
	{
		
		//Proj2
		BlockingQueue<String> recvQueue = new LinkedBlockingQueue<String>();
		//BlockingQueue<String> sendQueue = new LinkedBlockingQueue<String>();
		int[] arrPort;
		String[] arrDcxx;
		//End Proj2
		parent = new int[1];
		leaf = new boolean[1];
		parent[0]=-1;
		leaf[0] = false;
		int[] adjMatrix;
		boolean[] state = new boolean[1];
		state[0] = false;
		
		nodes = Integer.parseInt(args[2]);
		
		
		

		//int[] matrix;
		adjMatrix = new int[nodes*nodes];
		arrPort = new int[nodes];
		arrDcxx = new String[nodes];
		nodeId = Integer.parseInt(args[0]);
		String path = new String("");
		

		for(int i=0;i<nodes;i++){
			arrPort[i]=0;
			arrDcxx[i]="";
			for(int j=0;j<nodes;j++){
				adjMatrix[nodes*i+j]=0;
			}
		}
		/*	
		String[] dc;
		dc = new String[nodes];
		int[] prt = new int[nodes];
		int tokenNumber = 0;
		int totalTokens = 1;
		int hopNumber = 1;
		int totalHops = 0;
		int rand = randInt(1,10);
		
		String netId = "rxd132730";
		String config_file = args[4];
		String config = config_file.split(".txt")[0];
		config = config.split("/")[config.split("/").length-1];
		System.out.println("name of config: "+config);
		//System.out.println(config_file.split(".txt")[0]);
		String fileName = config+"-"+netId+"-"+nodeId+".out";
		String midLine = "Node ID: "+nodeId;
		String firstLine = "# BEGIN filename: "+fileName;
		String secondLine = "Net ID: "+netId;
		String thirdLine = "Listening on "+args[3]+":"+args[1];
		String fourthLine = "Random number: "+rand;
		String writeLine;
		FileWriter fw = null;
		BufferedWriter bw = null;
		File file1 = new File(fileName);
	
		try{
			if(!file1.exists()){
				file1.createNewFile();
			}
			fw = new FileWriter(file1,true);
			bw = new BufferedWriter(fw);
			synchronized(bw){
				bw.write(firstLine);
				bw.newLine();
				bw.write(secondLine);
				bw.newLine();
				bw.write(midLine);
				bw.newLine();
				bw.write(thirdLine);
				bw.newLine();
				bw.write(fourthLine);
				bw.newLine();
				//bw.write(fifthLine);
				//bw.newLine();
			}
			//String writeLine;
		}catch(IOException exa){
			exa.printStackTrace();
		}

		*/

		//int rand = randInt(1,10);	
		//System.out.println("Random Number Generated by "+nodeId+" is "+rand);
		String netId = "rxd132730";
		String config_file = args[4];
		String config = config_file.split(".txt")[0];
		config = config.split("/")[config.split("/").length-1];
		String fileName = config+"-"+nodeId+".out";
		FileWriter fw = null;
		BufferedWriter bw = null;
		File file1 = new File(fileName);
		File file = new File(config_file);
		/*try{
			if(!file1.exists()){
				file1.createNewFile();
			}
			fw = new FileWriter(file1,true);
			bw = new BufferedWriter(fw);
			//synchronized(bw){
			//WRITE TO FILE HERE <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				//bw.write("NewLine");
			//}
		}catch(IOException exa){
			exa.printStackTrace();
		}*/
		try{
			if(!file1.exists()){
				file1.createNewFile();
			}
			fw = new FileWriter(file1,true);
			bw = new BufferedWriter(fw);
			
			//Start server thread
			port = Integer.parseInt(args[1]);
			
			Scanner in = new Scanner(file);
			int lineCount = 0;
			nodes=0;
			
			while(in.hasNextLine()){
				String nxt = in.nextLine();
				String currentLine;
				if((nxt.indexOf("#") > 0 || nxt.indexOf("#") == -1) && !nxt.equals("")){
					if(nxt.indexOf("#")>0){
						currentLine = nxt.substring(0,nxt.indexOf("#"));
						//System.out.println(currentLine);
					}else{
						currentLine = nxt;
						//System.out.println(currentLine);
					}
					if(lineCount==0){
						nodes = Integer.parseInt(currentLine.split(" ")[0]);
						minPerActive = Integer.parseInt(currentLine.split(" ")[1]);
						maxPerActive = Integer.parseInt(currentLine.split(" ")[2]);
						minSendDelay = Integer.parseInt(currentLine.split(" ")[3]);
						snapshotDelay = Integer.parseInt(currentLine.split(" ")[4]);
						maxNumber = Integer.parseInt(currentLine.split(" ")[5]);	
						arrPort = new int[nodes];
						arrDcxx = new String[nodes];
						adjMatrix = new int[nodes*nodes];
						/*for(int i=0;i<nodes;i++){
							for(int j=0;j<nodes;j++){
								adjMatrix[nodes*i+j]=0;
							}
						}*/
					}else{
						if(lineCount>0 && lineCount<=nodes){
							arrPort[lineCount-1]=Integer.parseInt(currentLine.split(" ")[2]);
							arrDcxx[lineCount-1]=currentLine.split(" ")[1];
						}else{
							if(lineCount>nodes){
							String[] list = currentLine.split(" ");
								for(int j=0;j<list.length;j++){
									adjMatrix[nodes*(lineCount-nodes-1)+Integer.parseInt(list[j])]=1;
									adjMatrix[nodes*(Integer.parseInt(list[j]))+(lineCount-nodes-1)]=1;
								}
							}
						}
					}



					lineCount +=1;

				}	
				/*
				if(nxt.length() > 0){ 
					if((nxt.indexOf("#") == -1) && (lineCount >0) && (lineCount<=nodes)){
						System.out.println("lineCount"+lineCount);
						String[] words1 = nxt.split(" ");
						//System.out.println("nodeId:"+nodeId+"nodes:"+nodes);
						System.out.println("nxt"+nxt);
						//System.out.println(nodes+" "+words1[0]+ " "+words1[1]+" "+words1[2]);
						dc[Integer.parseInt(words1[0])]=words1[1];
						int a = Integer.parseInt(words1[2]);
						prt[Integer.parseInt(words1[0])]=a;
					}
					if(nxt.indexOf("#") > 0){
						String[] half = nxt.split("#");
						//System.out.println(half[0]);
						String[] words2 = half[0].split(" ");
						if(words2[0] != null && Integer.parseInt(words2[0])==nodeId){
							int leng = words2.length;
							System.out.println("leng:"+leng);
							path = Integer.toString(tokenNumber)+ "!";
							path += Integer.toString(totalTokens)+"!";
							tokenNumber+=1;
							totalTokens+=1;
							path += Integer.toString(rand)+"!";
							path += Integer.toString(hopNumber)+"!";
							totalHops = leng;
							path += Integer.toString(totalHops)+"!";
							//path +=
							writeLine = "Emitting token "+tokenNumber+" with path ";
							for(int i=1;i<leng;i++){
								//System.out.println("path:"+words2[i-1]);
								//System.out.println(words2[0]+" "+words2[i]+"length: "+words2[i].length());
								matrix[nodes*Integer.parseInt(words2[i-1])+Integer.parseInt(words2[i])]= 1;
								writeLine += words2[i-1]+ " -> ";
								path = path + String.valueOf(words2[i-1])+ ":";
								path = path + dc[Integer.parseInt(words2[i-1])]  + ":";
								path = path + Integer.toString(prt[Integer.parseInt(words2[i-1])]) + "!";
								//matrix[nodes*Integer.parseInt(words2[0])+Integer.parseInt(words2[i])] = 1;
							}
							writeLine += words2[leng-1]+ " -> ";
							path = path + String.valueOf(words2[leng-1])+":";
							path = path + dc[Integer.parseInt(words2[leng-1])]+":";
							path = path + Integer.toString(prt[Integer.parseInt(words2[leng-1])])+"!";
							writeLine += words2[0];
							path = path + String.valueOf(words2[0])+":";
							path = path + dc[Integer.parseInt(words2[0])]+":";
							path = path + Integer.toString(prt[Integer.parseInt(words2[0])]);
							matrix[nodes*Integer.parseInt(words2[leng-1])+Integer.parseInt(words2[0])]=1;
							System.out.println("1path:"+path+"Endpath");
							int count = 1;
							hopNumber = 1;
							for(int i=1;i<Integer.parseInt(path.split("!")[4]);i++){
								if(totalHops>1 && Integer.parseInt(path.split("!")[i+5].split(":")[0])==nodeId){
									count +=1;
									hopNumber +=1;
								}
								if(totalHops>1 && Integer.parseInt(path.split("!")[i+5].split(":")[0])!=nodeId){
									i=5+Integer.parseInt(path.split("!")[4]);
								}
							}
							String[] wordsa = path.split("!");
							int pathLeng = wordsa.length;
							path = "";
							for(int i=0;i<pathLeng;i++){
								if(i==2){
									path += Integer.toString(count * rand) + "!";
								}else{
									
									if(i==pathLeng-1){
										path += wordsa[i];
									}else{
										if(i==3){
											path += Integer.toString(hopNumber) + "!";
										}else{
											path += wordsa[i]+"!";
										}
									}
								}
							}
							System.out.println("2path:"+path+"EndPath");
							
							
							synchronized(bw){
								bw.write(writeLine);
								bw.newLine();
							}
							
							servPort = Integer.parseInt(path.split("!")[5+hopNumber].split(":")[2]);
							servDcXX = path.split("!")[5+hopNumber].split(":")[1];
							goClient goC = new goClient();
							goC.setThreadName("ClientThread running on "+nodeId);
							goC.setPort(servPort);
							goC.setDcXX(servDcXX);
							goC.setPacket(path);
							goC.start();

						}
					}
					if(nxt.indexOf("#") == -1){
						lineCount += 1;
					}
				}*/
				
			}
			
			//BlockingQueue<String> sendQueue = new LinkedBlockingQueue<String>();
			int neighbors=0;
			for(int i=0;i<nodes;i++){
				if(adjMatrix[nodes*nodeId+i]==1){
					neighbors+=1;
				}
			}
			BlockingQueue<String>[] sendQueue = (LinkedBlockingQueue<String>[])new LinkedBlockingQueue<?>[neighbors];
			
			//Variables for Chandy Lamport
			int[] channelState = new int[neighbors];
			int[] recording = new int[neighbors];
			int[] vector = new int[nodes];
			int[] systemState= new int[nodes];
			boolean[] snapshot=new boolean[nodes];
			int[] currentNumber=new int[1];	
			int[] countApplnMsg=new int[1];
			int[] protocolTermination=new int[nodes];
			countApplnMsg[0]=0;
			int totalNeighbors = neighbors;
			boolean[] executing=new boolean[1];
			executing[0]=true;

			for(int i=0;i<neighbors;i++){
				channelState[i]=-1;
				recording[i]=-1;
			}

			for(int i=0;i<nodes;i++){
				vector[i]=0;
				snapshot[i]=false;
				protocolTermination[i]=-1;
			}

			for(int i=0;i<neighbors;i++){
				sendQueue[i] = new LinkedBlockingQueue<String>();
			}

			goServer goS = new goServer("Server Thread "+ "node :"+nodeId,port,nodeId,recvQueue);
			goS.start();
			minPerActive=maxPerActive;
			neighbors=0;
			int workers=0;
			for(int k=0;k<nodes;k++){
				
				if(adjMatrix[nodes*nodeId+k]==1){
						
					for(int i=0;i<2;i++){
						goWorker goW = new goWorker("Worker Thread "+"node :"+nodeId, nodeId, bw, config_file, recvQueue, sendQueue
							, adjMatrix, parent, workers, nodes, leaf, totalNeighbors, vector
							, channelState, recording, snapshotDelay, systemState, state, snapshot
							, minSendDelay, minPerActive, maxNumber, currentNumber, countApplnMsg, protocolTermination,executing, fw);
						goW.start();
						workers +=1;
					}
					
					//System.out.println("test nodeId "+nodeId+" k "+k);
					servPort = arrPort[k];
					servDcXX = arrDcxx[k];
					//System.out.println("test2 "+servPort+ " "+servDcXX);
					goClient goC = new goClient();
					goC.setThreadName("ClientThread running on "+nodeId);
					goC.setPort(servPort);
					goC.setDcXX(servDcXX);
					goC.setSend(sendQueue[neighbors]);
					//goC.setNeighbor(neighbors);
					goC.start();
					neighbors +=1;
				}
			}
			

			//Thread.sleep(1000*nodes);
			/*synchronized(bw){
				bw.write("All tokens received");
				bw.newLine();
				bw.write("# END");
			}*/
			//bw.close();
		}//catch(FileNotFoundException ex){
		//	System.out.println("FileNotFound;Nodeid"+args[0]);
		//}
		catch(IOException exb){
			exb.printStackTrace();
		}
		//catch(InterruptedException exc){
		//	exc.printStackTrace();
		//}

	}
} 
