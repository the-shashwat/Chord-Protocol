import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer implements Runnable{
    private boolean isAlive;
    private Node localNode;
    private ServerSocket localServer;
    public NodeServer(Node localNode)
    {
        System.out.println("Node Server started for node address : "+localNode.getLocalAddress());
        this.localNode=localNode;
        try {
            this.localServer=new ServerSocket(localNode.getLocalAddress().getPort());
        } catch (IOException e) {
            //e.printStackTrace();
        }
        this.isAlive=true;
        new Thread(this).start();
    }
    @Override
    public void run() {
        while(this.isAlive)
        {
            try {
                Socket connectionSocket=this.localServer.accept();
                RemoteProcedureCall rpc=new RemoteProcedureCall(localNode,connectionSocket);
            } catch (IOException e) {
              //  System.out.println("Node id : "+this.localNode.getLocalId()+" with address : "+this.localNode.getLocalAddress()+" unable to accept connection");
              //  e.printStackTrace();
            }
        }
    }
    public void stopServer(){
        this.isAlive=false;
    }
}
