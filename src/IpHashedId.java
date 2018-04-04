import java.net.InetSocketAddress;

public class IpHashedId {
    private int id;
    private InetSocketAddress ip;

    public IpHashedId(String node) {
        this.id=Integer.parseInt(node.substring(0,node.indexOf("_")));
        node=node.substring(node.indexOf("/")+1);
        this.ip=new InetSocketAddress(node.substring(0,node.indexOf(":")),Integer.parseInt(node.substring(node.indexOf(":")+1)));
    }

    public InetSocketAddress getIp() {
        return ip;
    }

    public void setIp(InetSocketAddress ip) {
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IpHashedId(int localId, InetSocketAddress ip) {
        this.ip = ip;
        this.id=localId;
    }
    public String toString(){
        return id+"_"+ip.toString();
    }
}
