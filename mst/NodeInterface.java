package mst;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public interface NodeInterface extends Remote{
     public void addedge(Link link) throws RemoteException;
    public void sendMessage(Link link,Message message) throws RemoteException;
     public void wakeup() throws RemoteException;
     public void onRevieve(int id1,int id2, Message message) throws RemoteException;
      public int getID() throws RemoteException;
    }


