//start server
package mst;
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

public class Nodemulti1 {
	static HashMap<Integer,String> serverMap;
    public static void main(String[] args) throws FileNotFoundException, RemoteException, NotBoundException {
        String[] serverList = {"192.168.1.102","192.168.1.105"};
        System.setProperty("java.rmi.server.hostname", "192.168.1.105");
        int[][] serverComponentID= new int[2][];
        serverComponentID[0] = new int[1000];
        serverComponentID[1] = new int[1000];
        int server_id = 0;
        if (args.length > 0) {
            server_id = Integer.parseInt(args[0]);
        }
        String filename = "E:\\workspace\\causalOrderingMessage\\src\\graph2";
        int A[][] = creatematrix(filename);
        Thread[] threads = new Thread[A.length];
        //LocateRegistry.createRegistry(8400);
        int len = A.length -(A.length/2);
        int id[] = new int[1000];
        serverMap = new HashMap<Integer, String>();
        for(int i=0;i<len;i++)
        	id[i] = i+(A.length/2);
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
        List<NodeInterface> nodes = new ArrayList<NodeInterface>();
        for (int i = 0; i < len; i++) {
            Nodeprocess nodeprocess = new Nodeprocess(id[i], serverList, serverComponentID,serverMap);

            threads[i] = new Thread(nodeprocess);

            threads[i].start();
            //NodeInterface nodeStub = (NodeInterface) UnicastRemoteObject.exportObject(nodeprocess.node, 0);
            nodes.add(nodeprocess.node);
        }
        while(waitInitialization(A.length/2-1)==false);
        System.out.println("ok");
        for (int j = A.length/2; j < A.length; j++) {
            for (int k = 0; k < A.length; k++) {
            	if (A[j][k] != 0) {
                    NodeInterface dst= null;
                    if(k>=A.length/2){
                        dst = nodes.get(k-A.length/2);
                    }
                    else{
                        Registry registry;
                        //System.out.println(serverMap.get(k));
                        registry = LocateRegistry.getRegistry(serverMap.get(k), 8400);
                        dst = (NodeInterface) registry.lookup("Node" + k);

                    }
                    Link link = new Link(A[j][k],nodes.get(j-A.length/2),dst);
                    nodes.get(j-A.length/2).addedge(link);
                }
            }
        }
        nodes.get(0).wakeup();
        //for (NodeInterface n :nodes) n.wakeup();
    }



    public static boolean waitInitialization(int number)
    {
    	Registry registry;
        try {
			registry = LocateRegistry.getRegistry(serverMap.get(number), 8400);
			registry.lookup("Node" + number);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
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
    private static void createlinks(int weight, NodeInterface node1, NodeInterface node2)  {
        Link link = new Link(weight,node1,node2);
        try {
            node1.addedge(link);
            node2.addedge(link);
        } catch (Exception e){
            System.out.println("Exception @createLinks");
            System.exit(1);
        }
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
