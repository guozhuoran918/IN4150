package mst;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
public class Nodeprocess extends Thread {
    public Node node;

    public int id;
    public Nodeprocess(int ID, String[] serverList, int[][] threadID, HashMap severmap) {
        super();

        try {
            this.node = new Node(ID, serverList, threadID,severmap);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        this.id = ID;
    }
    public Node getNode(){
        return this.node;
    }
    @Override
    public void run() {
    	try {
			this.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        while (true) {
        	try {
    			this.sleep(500);
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
            Node.messageItem q = null;
            try {
				q = this.node.rqueue.poll(4, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (q == null) {
                System.out.println(this);
                if (this.node.SN == NodeState.FIND) {
                    node.test();
                    //}

                }
                continue;
            }
            //todo
            //System.out.println(this.id+" "+q.message.type);
            Link dst = node.weightToEdge(q.id1,q.id2);  //find link destination
            if (node.SN == NodeState.SLEEPING) node.wakeup();
            node.execute(dst, q.message);
            if(node.change==1)
            {
            	node.change = 0;
            	node.check_queue();
            }
        }
    }

    public String toString() {
        String string = id + " " + node.SN + " core of final result =" + node.FN + " final Level=" + node.LN;
        return string;
    }

    public void wakeup() {
        this.node.wakeup();
    }

    public void addedge(Link edge) {

        this.node.Edges.add(edge);
    }
    public int getID(){
        return this.node.id;
    }
}
