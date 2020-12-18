package mst;
//start server
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Nodemulti {
	static HashMap<Integer,String> serverMap;
    public static void main(String[] args) throws FileNotFoundException, RemoteException, NotBoundException {
        String[] serverList = {"192.168.1.102","192.168.1.105"};
        System.setProperty("java.rmi.server.hostname", "192.168.1.102");
        int[][] serverComponentID= new int[2][];
        serverComponentID[0] = new int[1000];
        serverComponentID[1] = new int[1000];
        int server_id = 0;
        if (args.length > 0) {
            server_id = Integer.parseInt(args[0]);
        }
        String filename = "D:\\Q2\\distributed algorithms\\ass3\\mst\\causalOrderingMessage\\src\\mst\\graph2";
        int A[][] = creatematrix(filename);
        Thread[] threads = new Thread[A.length];
        LocateRegistry.createRegistry(8400);
        int id[] = new int[1000];
        serverMap = new HashMap<Integer, String>();
        for(int i=0;i<A.length/2;i++)
        	id[i] = i;
        for(int i=0;i<A.length/2;i++)
        {
        	serverComponentID[0][i] = i;
        	serverMap.put(i, serverList[0]);
        }
        for(int i=0;i<A.length -(A.length/2);i++)
        {
        	serverComponentID[1][i] = i+(A.length/2);
        	serverMap.put(i+(A.length/2), serverList[1]);
        }
        //NodeInterface[] nodes = new Node[A.length];
        /*
        serverMap = new HashMap<Integer, String>();
        for (int i = 0; i < serverList.length; ++i) {
            for (int j = 0; j < serverComponentID[i].length; ++j) {
                int sid = serverComponentID[i][j];
                serverMap.put(sid, serverList[i]);
            }
        }
        */
        Nodeprocess[] ns = new Nodeprocess[A.length/2];
        List<NodeInterface> nodes = new ArrayList<NodeInterface>();
        for (int i = 0; i < A.length/2; i++) {
            Nodeprocess nodeprocess = new Nodeprocess(id[i], serverList, serverComponentID,serverMap);
            ns[i] = nodeprocess;
            threads[i] = new Thread(nodeprocess);
            threads[i].start();
            //NodeInterface nodeStub = (NodeInterface) UnicastRemoteObject.exportObject(nodeprocess.node, 0);
            nodes.add(nodeprocess.node);
        }
        while(waitInitialization(A.length-1)==false);
        System.out.println("ok");
        for (int j = 0; j < A.length/2; j++) {
            for (int k = 0; k < A.length; k++) {
                if (A[j][k] != 0) {
                    NodeInterface dst= null;
                    if(k<A.length/2){
                        dst = nodes.get(k);
                    }
                    else{
                        Registry registry;
                        //System.out.println(serverMap.get(k));
                        registry = LocateRegistry.getRegistry(serverMap.get(k), 8400);
                        dst = (NodeInterface) registry.lookup("Node" + k);

                    }
                    Link link = new Link(A[j][k],nodes.get(j),dst);
                    nodes.get(j).addedge(link);
                }
            }
        }
        ns[0].wakeup();
        //for(int i=0;i<4;i++)
        //	ns[i].wakeup();
    }



    public static boolean waitInitialization(int number)
    {
    	Registry registry;
        try {
			registry = LocateRegistry.getRegistry(serverMap.get(number), 8400);
			registry.lookup("Node" + number);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("not find");
			return false;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("not find");
			return false;
		}
        return true;
    }
    private static int[][] creatematrix(String filename) throws  FileNotFoundException{
        int n = new Scanner(new File(filename)).nextLine().split(" ").length;
        int [][]matrix=new int[n][n];
        Scanner s = new Scanner(new File(filename));
        for(int i=0; i<n;i++){
            for(int j=0;j<n;j++){
                matrix[i][j]=s.nextInt();
            }
        }
        return matrix;
    }



}
