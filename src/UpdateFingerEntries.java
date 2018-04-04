
public class UpdateFingerEntries implements Runnable{

    private Node localNode;
    private int limit;
    private boolean isAlive;
    private int i;
    public UpdateFingerEntries(Node localNode,int limit)
    {
        this.limit=limit;
        this.localNode=localNode;
        isAlive=true;
        i=0;
        new Thread(this).start();
    }

    @Override
    public void run() {

        while(isAlive)
        {
            try {
                i = i % limit;
                int fingerStart = (localNode.getLocalId() + (1 << (i))) % (1 << limit);
                IpHashedId succ = localNode.findSuccessor(fingerStart);
                localNode.updateFingerEntry(succ, i + 1);
                Thread.sleep(500);
            }
            catch (Exception e)
            {

            }
            i++;
        }
    }
    public void onDie() {
        isAlive = false;
    }
}
