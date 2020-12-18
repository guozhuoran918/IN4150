package mst;
/*
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
public class runner {
    //public runner(){ }
    public static Registry rmireg;
    public static void main(String[] args) throws FileNotFoundException{
        String filename = "D:\\Q2\\distributed algorithms\\ass3\\GHS_group17\\src\\graph2";
        if(args.length!=0){
            //System.out.println("Usage:java Main<inputFile");
            //System.exit(1);
            filename =args[0];
        }
        int A[][] = creatematrix(filename);
        //List<NodeInterface> nodes = new ArrayList<NodeInterface>();
        //create RMI registry
        try {
            rmireg = LocateRegistry.createRegistry(80);
        }catch (Exception e) {
            System.out.println("Exception @creatingRegistry");
            System.exit(1);
        }
        Thread[] threads = new Thread[A.length];
        List<NodeInterface> nodes = new ArrayList<>();

        for (int i = 0; i < A.length; i++) {
            Node node = new Node(i);
            threads[i] = new Thread(node);
                //threads[i].wakeup();
            threads[i].start();
            try {
                NodeInterface nodeStub = (NodeInterface) UnicastRemoteObject.exportObject(node, 0);
                nodes.add(nodeStub);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.exit(1);
                }

            }

        //create notes and register them to RMI registry
        //List<NodeInterface> nodes = new ArrayList<NodeInterface>();


        //createlinks
        for (int i = 0; i < A.length; i++) {
            for (int j = i + 1; j < A.length; j++) {
                if (A[i][j] != 0)
                    createlinks(A[i][j], nodes.get(i), nodes.get(j));
            }
        }

        try{
            for (NodeInterface n :nodes) n.wakeup();
        } catch (RemoteException e) {
            System.out.println("Exception @wakeup");
            System.exit(1);
        }
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
*/