import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class ChordMain {
    public static void main(String args[])throws IOException
    {
        if(args.length!=2)
        {
            System.out.println("Command Line Arguments Not appropriately entered. Exiting");
            return;
        }
        int localPort=Integer.parseInt(args[1]);
        int limit=5;
        InetSocketAddress localInetAddress= new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),localPort);
        Node newNode=new Node(localInetAddress,limit);
        InetSocketAddress knownAddress=UtilityClass.getAddressFromString(args[0]);
        newNode.join(knownAddress);

        Scanner sc = new Scanner(System.in);
        System.out.println("####################### Node Joined #######################");
        do{
            System.out.println("\n1 : Display Node Info\n2 : Display Node Finger Table\n3 : Display Successor Node Information\n4 : Display Predecessor Node Information\n5 : Query Key\n6 : Query File Name\n7 : Display List of Files\n8 : Exit");
            System.out.println("Enter your preference");
            int c = sc.nextInt();
            switch(c){
                case 1:newNode.displayNodeInfo();
                    break;
                case 2:newNode.displayFingerTable();
                    break;
                case 3:newNode.displatySuccessorInfo();
                    break;
                case 4:newNode.displayPredecessorInfo();
                    break;
                case 5:
                    System.out.println("Enter the key to be queried");
                    int key = sc.nextInt();
                    IpHashedId node=newNode.findSuccessor(key);
                    System.out.println("Key can be found at :: Node id "+node.getId()+" with Node ip : "+node.getIp());
                    break;
                case 6:
                    System.out.println("Enter the file name to be queried");
                    sc.nextLine();
                    String name = sc.nextLine();
                    int val=UtilityClass.getHashedValue(name);
                    IpHashedId n=newNode.findSuccessor(val);
                    System.out.println("File can be found at :: Node id "+n.getId()+" with Node ip : "+n.getIp());
                    break;
                case 7:
                    newNode.displayListOfFiles();
                    break;
                case 8:
                    newNode.nodeExit();
                    System.out.println("####################### Node exits the Network #######################");
                    System.exit(0);
                    break;
                default:System.out.println("Invalid Preference. Try again");
            }
        }while (true);
    }
}

