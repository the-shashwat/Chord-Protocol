import java.io.IOException;
import java.net.Socket;

public class RemoteProcedureCall implements Runnable{

    private Node localNode;
    private Socket connectionSocket;
    public RemoteProcedureCall(Node localNode,Socket connectionSocket)
    {
        this.connectionSocket=connectionSocket;
        this.localNode=localNode;
        new Thread(this).start();
    }
    @Override
    public void run() {
        String request= null;
        try {
            request = UtilityClass.getStringFromInputStream(connectionSocket.getInputStream());

            String response="Default_Case";
            if(request.startsWith("QueryId"))
            {
                int id = Integer.parseInt(request.substring(request.indexOf("_")+1));
                response = localNode.findPredecessor(id).toString();
            }
            else if(request.startsWith("FindSuccessor"))
            {
                int id = Integer.parseInt(request.substring(request.indexOf("_")+1));
                response = localNode.findSuccessor(id).toString();
            }
            else if(request.startsWith("GetPredecessor"))
            {
                response=localNode.getPredecessor().toString();
            }
            else if(request.startsWith("CheckPredecessor")){
                request = request.substring(request.indexOf("_")+1);
                IpHashedId pred = new IpHashedId(request);
                localNode.notified(pred);
            }
            else if(request.startsWith("SetPredecessor"))
            {
                String predString = request.substring(request.indexOf("_")+1);
                IpHashedId pred = new IpHashedId(predString);
                localNode.setPredecessor(pred);
            }
            else if(request.startsWith("Update"))
            {
                int entryNo=Integer.parseInt(request.substring(request.lastIndexOf("_")+1));
                String s = request.substring(request.indexOf("_")+1,request.lastIndexOf("_"));
                IpHashedId node = new IpHashedId(s);
                localNode.updateFingersOfPredecessors(node,entryNo);
            }
            else if(request.startsWith("GetSuccessor"))
            {
                response=localNode.getSuccessor().toString();
            }
            else if(request.startsWith("TransferFiles"))
            {
                int nodeId1=Integer.parseInt(request.substring(request.indexOf("_")+1,request.lastIndexOf("_")));
                int nodeId2=Integer.parseInt(request.substring(request.lastIndexOf("_")+1));
                response=localNode.transferFiles(nodeId1,nodeId2);
            }
            else if(request.startsWith("AddKeys")){
                request = request.substring(request.indexOf("_")+1);
                String dr = request.substring(0,request.indexOf("#"));
                IpHashedId node = new IpHashedId(dr);
                request = request.substring(request.indexOf("#")+1);
                String fileArray[]=request.split("@");
                localNode.addFiles(fileArray,node);
            }
            else if(request.startsWith("ReadContent")){
                String fileName = request.substring(request.indexOf("_")+1);
                response = UtilityClass.readFile(fileName,localNode.getNodeDirectoryPath());
            }
            else if(request.startsWith("SetIthFinger")) {
                IpHashedId node = new IpHashedId(request.substring(request.indexOf("_") + 1, request.lastIndexOf("_")));
                int i = Integer.parseInt(request.substring(request.lastIndexOf("_") + 1));
                localNode.updateFingerEntry(node, i);
            }
//          System.out.println("Result of the procedure call :: "+response);
            response+="\n\n";
            connectionSocket.getOutputStream().write(response.getBytes("UTF-8"));
        } catch (IOException e) {
           // System.out.println("Remote Procedure Call failed!!");

        }
    }
}
