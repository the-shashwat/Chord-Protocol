import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UtilityClass {
    public static int getHashedValue(String stringToHash) {
        int identifier=0;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(stringToHash.getBytes());

            byte byteData[] = md.digest();
            StringBuilder bitString = new StringBuilder();
            for (byte b : byteData) {
                String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                bitString.append(s1);
            }
            String str = bitString.toString();
            String fiveBitString = "";

            int k = 0;
            for (int i = 0; i < 5; i++) {
                fiveBitString += str.charAt(k);
                k += 60;
            }
            identifier = Integer.parseInt(fiveBitString, 2);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return identifier;
    }
    public static String getStringFromInputStream(InputStream is) throws IOException {
        String res="";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str=br.readLine();
        while(str!=null && !str.isEmpty())
        {
            res+=str;
            str=br.readLine();
        }
        return res;
    }
    public static String executeRemoteFunction(InetSocketAddress remoteAddress,String request)
    {
        try {
           // System.out.println("Remote Address "+remoteAddress+" request "+request);
            Socket clientSocket=new Socket(remoteAddress.getAddress(),remoteAddress.getPort());
            DataOutputStream outToRemoteNode=new DataOutputStream(clientSocket.getOutputStream());
            outToRemoteNode.writeBytes(request+"\n\n");
            return getStringFromInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            //System.out.println("Remote Procedure Call Failed for request : "+request);

        }
        return null;
    }
    public static int queryRelativeId(int id,int base,int limit)
    {
        int diff=id-base;
        if(diff<=0)
        {
            diff+=(1<<limit);
        }
        return diff;
    }

    public static InetSocketAddress getAddressFromString(String str)
    {
        InetAddress address=null;
        System.out.println("Get Address from this string : "+str);
        String ip=str.substring(0,str.indexOf(":"));
        int port=Integer.parseInt(str.substring(str.indexOf(":")+1));
        try {
            address=InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.out.println("InetSocketAddress could not be created for the str");
            return null;
        }
        return new InetSocketAddress(address,port);
    }
    public static void createFile(String fileName,String path){
        File dir = new File(path);
        // attempt to create the directory here
        if(!dir.isDirectory()){
            dir.mkdir();
        }
        try{
            fileName = path+"/"+fileName;
            File f = new File(fileName);
            f.createNewFile();
        }catch (Exception exc){
            System.out.println("File Creation Failed : "+fileName);

        }
    }

    public static void writeFile(String path,String fileName,String content){
        try{
            FileWriter fw = new FileWriter(path+"/"+fileName);
            BufferedWriter bw  = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
        }catch(Exception exc){

        }
    }

    public static String readFile(String fileName,String path){
        String r="";
        try{
            FileReader fr = new FileReader(path+"/"+fileName);
            BufferedReader br  = new BufferedReader(fr);
            String line=br.readLine();
            while(line!=null){
                r+=line;
                line = br.readLine();
            }
            br.close();
            fr.close();
            return r;
        }catch(Exception exc){

        }
        return r;
    }
    public static void main(String args[])throws Exception
    {
        System.out.println(getHashedValue("/172.16.181.113:5555"));
        System.out.println(getHashedValue("/172.16.181.113:4444"));
        System.out.println(getHashedValue("/172.16.181.113:3333"));
        System.out.println(getHashedValue("/172.16.181.113:2222"));

        System.out.println(getHashedValue("/172.16.181.113:7211"));
        System.out.println(getHashedValue("/172.16.181.113:7521"));
        System.out.println(getHashedValue("/172.16.181.113:7401"));
        System.out.println(getHashedValue("/172.16.181.113:4848"));
        System.out.println(getHashedValue("/172.16.181.113:3218"));
        System.out.println(getHashedValue("/172.16.181.113:4796"));
        System.out.println(getHashedValue("/172.16.181.113:6512"));
        System.out.println(getHashedValue("/172.16.181.113:6127"));
    }
}
//172.16.181.113
//172.16.27.88:6490 - 28



//172.16.181.113:6123 - 11
//172.16.181.113:4792 - 3
//172.16.181.113:3207 - 16
//172.16.181.113:7000 - 29
//172.16.181.113:7500 - 22
//172.16.181.113:4796 - 4
//172.16.181.113:6124 - 9
