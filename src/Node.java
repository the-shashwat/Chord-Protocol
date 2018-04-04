import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Node {

    private int limit;
    private int localId;
    private InetSocketAddress localAddress;
    private IpHashedId predecessor;
    HashMap<Integer, IpHashedId> fingerTable;
    private IpHashedId localIpIdPair;
    private NodeServer nodeServer;
    private UpdateFingerEntries updateFingersThread;
    private String nodeDirectoryPath;
    private HashMap<Integer, ArrayList<String>> listOfFiles;

    public Node(InetSocketAddress localAddress, int limit) {
        this.localAddress = localAddress;
        this.limit = limit;
        this.localId = UtilityClass.getHashedValue(localAddress.toString());
        this.predecessor = null;
        this.nodeServer = null;
        this.fingerTable = new HashMap<Integer, IpHashedId>();
        this.localIpIdPair = new IpHashedId(this.localId, this.localAddress);
        this.listOfFiles = new HashMap<Integer, ArrayList<String>>();
        this.nodeDirectoryPath = "./Node_" + localId;
    }

    /*Getters and setters*/
    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public IpHashedId getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(IpHashedId predecessor) {
        this.predecessor = predecessor;
    }

    public HashMap<Integer, IpHashedId> getFingerTable() {
        return fingerTable;
    }

    public void setFingerTable(HashMap<Integer, IpHashedId> fingerTable) {
        this.fingerTable = fingerTable;
    }


    public IpHashedId getLocalIpIdPair() {
        return localIpIdPair;
    }

    public void setLocalIpIdPair(IpHashedId localIpIdPair) {
        this.localIpIdPair = localIpIdPair;
    }

    public String getNodeDirectoryPath() {
        return nodeDirectoryPath;
    }

    public void setNodeDirectoryPath(String nodeDirectoryPath) {
        this.nodeDirectoryPath = nodeDirectoryPath;
    }

    public HashMap<Integer, ArrayList<String>> getListOfFiles() {
        return listOfFiles;
    }

    public void setListOfFiles(HashMap<Integer, ArrayList<String>> listOfFiles) {
        this.listOfFiles = listOfFiles;
    }


    public IpHashedId getSuccessor() {
        if (fingerTable != null && fingerTable.size() > 0) {
            return fingerTable.get(1);
        }
        return null;
    }

    /*Node Methods used for the implementation of Chord Protocol*/

    public IpHashedId findSuccessor(int id) {
        IpHashedId pred = this.findPredecessor(id);
        IpHashedId succ = new IpHashedId(UtilityClass.executeRemoteFunction(pred.getIp(), "GetSuccessor"));
        return succ;
    }

    public IpHashedId findPredecessor(int id) {
        int relative_id = UtilityClass.queryRelativeId(id, localId, limit);//23
        int relative_successor_id = UtilityClass.queryRelativeId(this.getSuccessor().getId(), localId, limit);//0
        if (relative_id > 0 && relative_id <= relative_successor_id) {
            return this.localIpIdPair;
        }
        InetSocketAddress closestPredAddress = this.findClosestPredecessor(id);
        String predecessor = UtilityClass.executeRemoteFunction(closestPredAddress, "QueryId_" + id);
        return new IpHashedId(predecessor);
    }

    public InetSocketAddress findClosestPredecessor(int id) {
        InetSocketAddress closestPred = this.getSuccessor().getIp();
        for (int i = limit; i > 1; i--) {
            int relative_finger_id = UtilityClass.queryRelativeId(fingerTable.get(i).getId(), localId, limit);
            int relative_id = UtilityClass.queryRelativeId(id, localId, limit);
            if (relative_id > 0 && relative_id > relative_finger_id) {
                closestPred = fingerTable.get(i).getIp();
                break;
            }
        }
        return closestPred;
    }

    public void join(InetSocketAddress knownAddress) {
        System.out.println("Local Address : " + localAddress + " trying to contact " + knownAddress + " in order to join chord ring.");
        if (knownAddress != null && !knownAddress.equals(localAddress)) {
            System.out.println("#######################Chord Ring already exists. New Node joining#######################");
            initFingerTable(knownAddress);
            updateOtherFingerTables();
            fileDistribution();
        } else if (knownAddress != null && knownAddress.equals(localAddress)) {
            System.out.println("#######################Creating Chord Ring#######################");
            for (int i = 1; i <= limit; i++) {
                fingerTable.put(i, new IpHashedId(localId, localAddress));
            }
            this.setPredecessor(new IpHashedId(localId, localAddress));
            /*Create 100 files*/
            for (int i = 1; i <= 100; i++) {
                String fileName = "file_" + i + ".txt";
                int fileId = UtilityClass.getHashedValue(fileName);
                updateListOfFiles(fileId, fileName);
                UtilityClass.createFile(fileName, nodeDirectoryPath);
            }
        }
        displayNodeInfo();
        this.nodeServer = new NodeServer(this);
        this.updateFingersThread = new UpdateFingerEntries(this, limit);
    }

    private void fileDistribution() {
        System.out.println("................Transfering files to this node............");
        String filesToTransfer = UtilityClass.executeRemoteFunction(this.getSuccessor().getIp(), "TransferFiles_" + this.getPredecessor().getId() + "_" + this.localId);
        String fileNames[] = filesToTransfer.split("#");
        int fileId = -1;
        String fileName = "";
        for (String s : fileNames) {
            if (s.isEmpty() || s == null) {
                continue;
            }
            fileId = Integer.parseInt(s.substring(0, s.indexOf("_")));
            fileName = s.substring(s.indexOf("_") + 1);
            updateListOfFiles(fileId, fileName);
            UtilityClass.createFile(fileName, nodeDirectoryPath);
            String fileContent = UtilityClass.executeRemoteFunction(getSuccessor().getIp(), "ReadContent_" + fileName);
            UtilityClass.writeFile(nodeDirectoryPath, fileName, fileContent);
        }
        System.out.println("................All Files Successfully Transferred................");
        displayListOfFiles();
    }

    public String transferFiles(int nodeId1, int nodeId2) {
        String fileList = "";
        Set<Integer> fileIds = listOfFiles.keySet();
        ArrayList<Integer> del = new ArrayList<>();
        del.clear();
        for (Integer fileId : fileIds) {
            if ((nodeId1 < nodeId2 && fileId <= nodeId2 && fileId > nodeId1) || (nodeId2 < nodeId1 && (fileId > nodeId1 || fileId <= nodeId2))) {
                ArrayList<String> fileNames = listOfFiles.get(fileId);
                del.add(fileId);
                for (String fileName : fileNames) {
                    fileList += fileId + "_" + fileName + "#";
                }
            }
        }
        for(Integer k : del) {
            ArrayList<String> lf=this.listOfFiles.get(k);
            listOfFiles.remove(k);
            for(String x:lf)
            {
                File toDelete=new File(getNodeDirectoryPath()+"/"+x);
                if(!toDelete.delete())
                {
                    System.out.println("File Deletion Failed");
                }
            }
        }
        return fileList;
    }

    public void initFingerTable(InetSocketAddress knownAddress) {
        System.out.println("Initialising Finger table of new Node using knownAddress" + knownAddress);
        String successor = UtilityClass.executeRemoteFunction(knownAddress, "FindSuccessor_" + (localId + 1));
        fingerTable.put(1, new IpHashedId(successor));
        predecessor = new IpHashedId(UtilityClass.executeRemoteFunction(this.getSuccessor().getIp(), "GetPredecessor"));
        UtilityClass.executeRemoteFunction(this.getSuccessor().getIp(), "SetPredecessor_" + localId + "_" + localAddress);
        for (int i = 2; i <= limit; i++) {
            int id = (localId + (1 << (i - 1))) % (1 << limit);
            int relative_id = UtilityClass.queryRelativeId(id, localId, limit);
            int relative_id_previous = UtilityClass.queryRelativeId(this.fingerTable.get(i - 1).getId(), localId, limit);
            if (relative_id > 0 && relative_id <= relative_id_previous) {
                fingerTable.put(i, fingerTable.get(i - 1));
            } else {
                String nextFingerEntry = UtilityClass.executeRemoteFunction(knownAddress, "FindSuccessor_" + (id));
                fingerTable.put(i, new IpHashedId(nextFingerEntry));
            }
        }
        System.out.println("Finger table of new node initialised");
    }

    public boolean updateOtherFingerTables() {
        for (int i = 1; i <= limit; i++) {
            int prevId = localId - (1 << (i - 1));
            prevId = (prevId < 0) ? prevId + (1 << limit) : prevId;
            IpHashedId predecessor = this.findPredecessor(prevId % (1 << limit));
            IpHashedId succOfPre=null;
            if(!predecessor.getIp().equals(this.getLocalAddress()))
                succOfPre = new IpHashedId(UtilityClass.executeRemoteFunction(predecessor.getIp(),"GetSuccessor"));
            else
                succOfPre = this.getSuccessor();
            int relative_id1=UtilityClass.queryRelativeId(succOfPre.getId(),localId,limit);
            int relative_id2=UtilityClass.queryRelativeId(prevId,localId,limit);
            if(relative_id1==relative_id2)
            {
                predecessor=succOfPre;
            }
            if (!predecessor.getIp().equals(localIpIdPair.getIp()))
                UtilityClass.executeRemoteFunction(predecessor.getIp(), "Update_" + localIpIdPair.toString() + "_" + i);
        }
        return true;
    }

    public void updateFingersOfPredecessors(IpHashedId newNode, int i) {
        int relative_id = UtilityClass.queryRelativeId(newNode.getId(), localId, limit);
        int relative_finger_id = UtilityClass.queryRelativeId(this.fingerTable.get(i).getId(), localId, limit);
        if (relative_id > 0 && relative_id < relative_finger_id) {
            //System.out.println("Updating finger entry : "+i+" for node "+localId+" New finger entry : "+newNode.getId());
            fingerTable.put(i, newNode);
            InetSocketAddress pred = this.predecessor.getIp();
            if (!pred.equals(newNode.getIp()))
                UtilityClass.executeRemoteFunction(pred, "Update_" + newNode.toString() + "_" + i);
        }
    }

    synchronized public void updateFingerEntry(IpHashedId node, int i) {
        fingerTable.put(i, node);
        if (i == 1 && node != null && !node.getIp().equals(localAddress)) {
            this.notify(node);
        }
    }

    public void notify(IpHashedId successor) {
        if (successor != null && !successor.getIp().equals(localAddress))
            UtilityClass.executeRemoteFunction(successor.getIp(), "CheckPredecessor_" + localIpIdPair.toString());
    }

    public void notified(IpHashedId pred) {
        this.setPredecessor(pred);
    }


    public void updateListOfFiles(int fileId, String fileName) {
        if (listOfFiles.containsKey(fileId)) {
            ArrayList<String> list = listOfFiles.get(fileId);
            list.add(fileName);
            listOfFiles.put(fileId, list);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(fileName);
            listOfFiles.put(fileId, list);
        }
    }

    public void addFiles(String fileArray[],IpHashedId node){
        int fileId=-1;
        String fileName="";
        for(String str:fileArray){
            if(str.isEmpty() || str==null){
                continue;
            }
            fileId = Integer.parseInt(str.substring(0,str.indexOf("_")));
            fileName = str.substring(str.indexOf("_")+1);
            updateListOfFiles(fileId,fileName);
            UtilityClass.createFile(fileName,nodeDirectoryPath);
            String content = UtilityClass.executeRemoteFunction(node.getIp(),"ReadContent_"+fileName);
            UtilityClass.writeFile(nodeDirectoryPath,fileName,content);
        }
    }

    public void nodeExit() {
        /*Ensuring graceful Delete*/
        System.out.println("Starting graceful deletion......................");
        UtilityClass.executeRemoteFunction(this.getPredecessor().getIp(), "SetIthFinger_" + this.getSuccessor().toString() + "_1");
        UtilityClass.executeRemoteFunction(this.getSuccessor().getIp(), "SetPredecessor_" + this.getPredecessor());
        System.out.println("Transfer of files to successor......................");
        String s = "AddKeys_";
        s+=localIpIdPair.toString()+"#";
        Set<Integer> keys = listOfFiles.keySet();
        for(int k:keys){
            ArrayList<String> list=listOfFiles.get(k);
            for(String f:list){
                File x=new File(getNodeDirectoryPath()+"/"+f);
                x.delete();
                s+=k+"_"+f+"@";
            }
        }
        UtilityClass.executeRemoteFunction(getSuccessor().getIp(),s);
        /*Ensuring graceful Delete*/
        if (this.nodeServer != null)
            this.nodeServer.stopServer();
        if (updateFingersThread != null)
            this.updateFingersThread.onDie();
    }

    /*Methods for displaying node information*/
    public void displayNodeInfo() {
        System.out.println("\nNode id = " + this.localId + " Node ip = " + this.localAddress);
    }

    public void displayFingerTable() {
        for (int i = 1; i <= limit; i++) {
            System.out.println("Range = [" + ((localId + (1 << (i - 1))) % (1 << limit)) + "," + ((localId + (1 << i)) % (1 << limit)) + ") -----> Finger Node ID = " + fingerTable.get(i).getId() + " Finger Node IP = " + fingerTable.get(i).getIp());
        }
    }

    public void displatySuccessorInfo() {
        System.out.println("\nSuccessor id = " + this.getSuccessor().getId() + " Successor ip = " + this.getSuccessor().getIp());
    }

    public void displayPredecessorInfo() {
        System.out.println("\nPredecessor id = " + this.getPredecessor().getId() + " Predecessor ip = " + this.getPredecessor().getIp());
    }

    public void displayListOfFiles() {
        System.out.println("\nList of File Keys held by this node");
        for (Map.Entry<Integer, ArrayList<String>> file : listOfFiles.entrySet()) {
            System.out.println("File ID : " + file.getKey() + " File Names " + file.getValue());
        }
    }
}


/*
172.16.27.88

* */