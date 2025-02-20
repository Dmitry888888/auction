//package auction;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//public class InetAddressMain {
//    public static void main(String[] args) {
//        InetAddress currentIp;
//        InetAddress epamIp;
//        try {
//            currentIp = InetAddress.getLocalHost();
//// output IP address of local computer
//            System.out.println("current IP -> " + currentIp.getHostAddress());
//            epamIp = InetAddress.getByName("epam.com");
//            System.out.println("EPAM IP address -> " + epamIp.getHostAddress());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }
//}