package mst;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.Serializable;

enum EdgeState {
    unknown,in_mst,not_in_mst
}
public class Link implements Serializable{
    int weight;
    NodeInterface Node1,Node2;
    boolean iscore;
    EdgeState state;
    public Link(int weight,NodeInterface node1, NodeInterface node2){
        this.weight = weight;
        this.Node1  = node1;
        this.Node2  = node2;
        this.iscore = false;
        this.state = EdgeState.unknown;
    }
    public int getWeight(){
        return this.weight;
    }
    public void setState(EdgeState state){
        this.state = state;
    }

    public NodeInterface  dst(int id) throws RemoteException {
        int localID=-1;
        localID = Node1.getID();

        //int localID=-1;
        if(id == localID){
            return Node2;
        }
        else{
            return Node1;
        }
    }
    public String toString() {
        String string = "null";
        try {
            string = "link " + Node1.getID() + " to " + Node2.getID() + " " + state + " weight=" + weight; //+ node1.getID() + " to " + node2.getID()
        } catch (Exception e) {
            System.out.println("@link");
            System.exit(1);
        }
        return string;
    }

    }
