package mst;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


enum NodeState {
    FOUND, FIND, SLEEPING
}

public class Node extends UnicastRemoteObject implements NodeInterface {
    public int id;             //the id of this node
    public List<Link> Edges;   //the all edges of this node
    public AtomicInteger LN;             //level of current fragment it is part of
    public AtomicInteger FN;           //name of the current fragment it is part of
    public NodeState SN;       //state of the node(find/found)
    public Link best_edge;     //local direction of candidate MOE
    private Link in_branch;    //edge towards core
    private Link test_edge;    //ege checked whether other end in same fragment
    public AtomicInteger best_weight;    //weight of current candidate MOE
    public AtomicInteger find_count;      //number of report messages expected
    public int change = 1;
    public int executing = 0;
    String[] serverList;
    int[][] serverid;
    HashMap<Integer,String> serverMap = new HashMap<Integer, String>();
    public Queue<messageItem> queue;
    public BlockingQueue<messageItem> rqueue;
    public void addRqueue(messageItem q) throws InterruptedException {
        this.rqueue.put(q);
    }
    public int getID() {
        return id;
    }
    public class messageItem{
        int id1;
        int id2;
        Message message;
        messageItem(int id1,int id2,Message message){
            this.id1 =id1;
            this.id2 =id2;
            this.message = message;
        }

    }

    public Node(int ID,String[] serverList,int[][] threadID,HashMap serverMap) throws RemoteException {
        super();
        this.id = ID;
        this.serverList = serverList;
        this.serverid = threadID;
        this.serverMap = serverMap;
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry("192.168.1.102", 8400);
            registry.rebind("Node" + id, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        /*
        for(int i=0;i<this.serverList.length;++i)
        {
            for(int j=0;j<serverid[i].length;++j)
            {
                int sid = serverid[i][j];
                serverMap.put(sid, serverList[i]);
            }
        }
        */

        //this.id = ID;
        this.SN = NodeState.SLEEPING;
        this.LN = new AtomicInteger();
        this.LN.set(0);
        this.FN = new AtomicInteger();
        this.FN.set(0);
        this.find_count = new AtomicInteger();
        this.find_count.set(0);
        this.Edges=new ArrayList<Link>();
        this.best_weight = new AtomicInteger();
        this.best_weight.set(Integer.MAX_VALUE); 
        this.best_edge = null;
        this.in_branch = null;
        this.test_edge = null;
        this.rqueue = new LinkedBlockingQueue<messageItem>();
        this.queue = new LinkedList<messageItem>();

    }
    public void addedge(Link edge){
    	try {
			System.out.println(this.id+" "+edge.Node1.getID()+" "+edge.Node2.getID());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.Edges.add(edge);
    }
    public void check_queue(){
    	executing = 1;
        int size = queue.size();
        if(size!=0){
            for (int i=0;i<size;i++){
                messageItem obj = queue.remove();
                execute(weightToEdge(obj.id1,obj.id2),obj.message);
            }
        }
        executing=0;
    }
    public Link weightToEdge(int id1,int id2){
        Link dst = null;
        for (Link e:this.Edges){
            try {
				if((e.Node1.getID() == id1&&e.Node2.getID()==id2)||(e.Node1.getID() == id2&&e.Node2.getID()==id1)){
				    dst = e;
				}
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        //System.out.println(id1+" "+id2+" "+this.id+"xxxx");
        //System.out.println(id1+" "+id2+" "+dst.weight);
        return dst;
    }

    public synchronized void sendMessage(Link dst,Message message) {
        //NodeInterface dstNode = dst.dst(id);
       Registry registry;
      // for(int i = 0;i<this.serverList.length;i++){
      ///     for(int j=0;j<this.serverid[id].length;j++) {

       //       int sid = this.serverid[i][j];
       //        if (sid != this.id) {
                   try {
                       int nodeid = dst.dst(id).getID();
                       registry = LocateRegistry.getRegistry(serverMap.get(nodeid), 8400);
                       //Nodeprocess dstNode =dst.dst(id);
                       //int nodeid = dst.dst(id).getID();
                       NodeInterface N = (NodeInterface) registry.lookup("Node" + nodeid);
                       //System.out.println(serverMap.get(nodeid+serverMap.get(nodeid)));
                       N.onRevieve(dst.Node1.getID(),dst.Node2.getID(), message);
                       //(new messageItem(dst.weight, message));
                   } catch (RemoteException | NotBoundException e) {
                       e.printStackTrace();
                   }
               }
      //     }
    //   }


   // }

    public void onRevieve(int id1,int id2, Message message){
        try{
            this.rqueue.put((new messageItem(id1,id2,message)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void execute(Link edge,Message message){
        switch(message.type){
            case connect:
                connect(edge,message);
                break;

            case initiate:
                initiate(edge,message);
                break;

            case test:
                receivetest(edge,message);
                break;
            case accept:
                receiveaccpet(edge);
                break;
            case reject:
                receivereject(edge);
                break;
            case report:
                receivereport(edge,message);
                break;
            case change_root:
                change_root();
                break;
            default:
                break;
        }
    }

    public synchronized void wakeup(){
        if(SN != NodeState.SLEEPING)return;

            //this.LN=0;

            //this.find_count=0;
            //this.best_weight=Edges.get(0).weight;
        	int temp = Integer.MAX_VALUE;
        	Link tempEdge = null;
            for(Link e : Edges){
                if(e.weight<temp){
                    temp = e.weight;
                    tempEdge = e;
                }
            }
            this.LN.set(0);
            SN=NodeState.FOUND;
            this.find_count.set(0);

            tempEdge.setState(EdgeState.in_mst);
            //System.out.println("Node"+id+"best_edge"+best_edge.state);
            System.out.println("Node "+id+" wake up");
            sendMessage(tempEdge, new Message(Type.connect,this.LN.get(),this.FN.get(),this.SN,best_weight.get()));
    }


    public synchronized void connect(Link edge,Message message){
    	//System.out.println(this.id+" connected");
        //wakeup at first message
        if(this.SN==NodeState.SLEEPING){
            wakeup();
        }
        //absorb lower-level fragment
        if(message.LN<this.LN.get()){
        	change = 1;
            edge.state = EdgeState.in_mst;
            sendMessage(edge,new Message(Type.initiate,LN.get(),FN.get(),SN,best_weight.get()));
            System.out.println("Node "+id+" absorb link "+edge.getWeight());
            if(this.SN == NodeState.FIND){
                find_count.incrementAndGet();
            }
        }
        else
        {
        	if(edge.state==EdgeState.unknown){
	            try {
					queue.add(new messageItem(edge.Node1.getID(),edge.Node2.getID(),message));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            //append message to queue
        	}
        	else{
            	//edge.state = EdgeState.in_mst;
                sendMessage(edge,new Message(Type.initiate,LN.get()+1,edge.getWeight(),NodeState.FIND,best_weight.get()));
                System.out.println("Node"+id+" merge link"+edge.weight);
            }
        }
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }
    public synchronized void initiate(Link edge, Message message){
    	//System.out.println(this.id+" init");
    	change = 1;
        this.LN.set( message.LN);
        this.FN.set(message.FN);
        this.SN = message.state;
        this.in_branch = edge;
        this.best_edge = null;
        this.best_weight.set( Integer.MAX_VALUE);
        for (Link e : Edges){
            if(e.getWeight()!=in_branch.getWeight() && e.state==EdgeState.in_mst ){
                sendMessage(e,new Message(Type.initiate,this.LN.get(),this.FN.get(),this.SN,best_weight.get()));
                //send(initiate;L,F,S)on edge e
                if(this.SN==NodeState.FIND){
                    this.find_count.incrementAndGet();
                }
            }
        }
        if(this.SN==NodeState.FIND){
            test();

        }
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }


    public synchronized void test(){
    	//System.out.println(this.id+" test");
    	//System.out.println(this.rqueue.size());
        int minWeight = Integer.MAX_VALUE;
        Link candidate = null;
        for (Link e: Edges){
            if (e.state == EdgeState.unknown && e.getWeight()<minWeight){
            	//if(e.weight==Integer.MAX_VALUE) System.out.println("nmdnmdn");
                candidate = e;
                minWeight =  e.getWeight();

            }

        }
    		//System.out.println(this.id+" "+minWeight+" ");
        if(candidate ==null) {
        	test_edge = null;
            report();
            //this.noedge = true;
        }
        else{
        	test_edge = candidate;
            //this.best_weight.set( this.test_edge.getWeight());
            sendMessage(this.test_edge,new Message(Type.test,LN.get(),FN.get(),SN,this.best_weight.get()));
            //send(test,LN,FN) on test_edge;
        }
    }


    public synchronized void receivetest(Link edge,Message message) {
    	//System.out.println(this.id+" receive test");
        if (this.SN == NodeState.SLEEPING) {
            wakeup();
        }
        //level too high, postpone
        if (message.LN > this.LN.get()) {
            try {
            	//System.out.println(message.LN+" nmd "+this.LN.get());
				queue.add(new messageItem(edge.Node1.getID(),edge.Node2.getID(), message));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            //append message to queue
        }
        else {
            if (message.FN != this.FN.get()) {
                // other fragment
                sendMessage(edge, new Message(Type.accept, this.LN.get(), this.FN.get(), this.SN, best_weight.get()));
                //send(accept)on edge j
            } else {
                if (edge.state == EdgeState.unknown) {
                	change = 1;
                    edge.setState(EdgeState.not_in_mst);
                    //sendMessage(edge, new Message(Type.reject, this.LN, this.FN, this.SN, best_weight));
                }
                if (test_edge==null||test_edge.weight != edge.weight) {
                    sendMessage(edge, new Message(Type.reject, this.LN.get(), this.FN.get(), this.SN, best_weight.get()));
                    //sendreject
                }else
                {
                	test();
                }
            }
        }
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }

    public synchronized void receiveaccpet(Link edge){
    	//System.out.println(this.id+" acc");
        this.test_edge = null;   // no need to look futher
        if(edge.weight<this.best_weight.get()){
            best_edge= edge;
            best_weight.set(edge.getWeight());
        }
        report();
    }

    public synchronized  void receivereject(Link edge){
    	//System.out.println(this.id+" rec");
        if(edge.state == EdgeState.unknown){
        	change = 1;
            edge.setState(EdgeState.not_in_mst);
        }
        //if(this.noedge==false)
        test();
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }

    public synchronized  void report(){
    	//System.out.println(this.id+" report");
        if(find_count.get()==0 && test_edge == null){
        	change = 1;
            this.SN = NodeState.FOUND;
            //System.out.println("zero");
            sendMessage(in_branch,new Message(Type.report,LN.get(),FN.get(),SN,best_weight.get()));
            //send(report,bestweight) on in-brance  //report best edge towards core
        }
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }

    public synchronized  void receivereport(Link edge, Message message){
    	//System.out.println(this.id+" receive report");
        if( edge.getWeight()!=in_branch.getWeight()){
            find_count.decrementAndGet();
            if(message.weight<best_weight.get()){
                this.best_weight.set( message.weight);
                this.best_edge =edge;
            }
            report();
        }
        else {
            if (this.SN == NodeState.FIND) {
                try {
					queue.add(new messageItem(edge.Node1.getID(),edge.Node2.getID(), message));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//put message on the queue

            }
            else {
            	//System.out.println("hahaha");
                if (message.weight > best_weight.get()) {
                    change_root();
                } else if (message.weight == Integer.MAX_VALUE && best_weight.get() == Integer.MAX_VALUE) {
                    //halt
                    System.out.println("Node" + id + "halt");
                    System.exit(0);
                }
            }
        }

    }

    private synchronized  void change_root(){
        if(best_edge.state==EdgeState.in_mst){
            sendMessage(best_edge, new Message(Type.change_root,LN.get(),FN.get(),SN,best_weight.get()));
            //send(change_root)on best_edge;
        }
        else{
        	change = 1;
            sendMessage(best_edge, new Message(Type.connect,LN.get(),FN.get(),SN,best_weight.get()));
            //send(connect;LN)on best_edge;
            best_edge.setState(EdgeState.in_mst);
        }
        if(change==1&&executing==0)
        {
        	change = 0;
        	check_queue();
        }
    }


}
