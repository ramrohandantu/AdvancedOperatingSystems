import java.util.PriorityQueue;
import java.util.Comparator;
public class MutualExclusion{
	private boolean l1;
	private boolean l2;
	public PriorityQueue<Double> pq=null;
	private int nodes;
	private double nodeId;
	private int[] vector;
	
	 

	public MutualExclusion(int node, int id){
		this.nodes = node;
		this.nodeId = id/100.0;
		System.out.println(" id is "+id+" id/100.0 is "+id/100.0);
		System.out.println("nodeId in pq "+nodeId);
		this.pq = new PriorityQueue<Double>();
		this.vector = new int[nodes];
		this.l1=false;
		this.l2=false;
	}
	
	public void putQueue(double put){
		pq.add(put);
		System.out.println("###Adding request "+put+" at "+nodeId);
		updateL2(nodeId);
	} 
	
	public double getQueue(){
		System.out.println("get1 ");
		double get = pq.poll();
		System.out.println("get2 ");
		updateL2(nodeId);
		/*if(get==null){
			get=0;
		}*/
		return get;
	} 
	
	public double headQueue(){
		return pq.peek();
	}
	
	public boolean checkL2(){
		return l2;
	}

	public void updateL2(double current){
		System.out.println(current);
		double head = headQueue();
		System.out.println(head);
		head = head - (int)head;
		System.out.println(head);
		System.out.println(" PQ Size "+pq.size());
		//if(current == truncateDecimal(head,2)){
		//int compareWith=(int)(current*100);
		//int compared;
		boolean equal = false;
		if(Math.abs(head-current) < 0.0005){
			equal=true;
		}else{
			equal=false;
		}
		
		System.out.println("current " +current);
		System.out.println("head " + head);
		System.out.println("result after comparing "+ Boolean.toString(equal));
		
		if(equal){
		//if((int)(current*100) == (int)(head*100)){
			l2 = true;
			System.out.println("Made True ");
		}else{
			l2=false;
		}
	}

	public boolean checkL1(){
		return l1;
	}

	public void updateL1(){
		int count = 0;
		for(int i=0;i<nodes;i++){
			if(vector[i]==1){
				count +=1;
			}
		}
		if(count>=nodes-1){
			l1=true;
		}else{
			l1=false;
		}
	}

	public void resetVector(){
		for(int i=0;i<nodes;i++){
			vector[i]=0;
		}
		l1=false;
	}

	public void updateVector(int node){
		vector[node]=1;
		updateL1();
	}

	public boolean checkL1L2(){
		if(l1==true && l2==true){
			return true;
		}else{
			return false;
		}
	}

	/*public BigDecimal truncateDecimal(double x,int numberofDecimals)
	{
	    if ( x > 0) {
	            	return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
	    }
	}*/
}
