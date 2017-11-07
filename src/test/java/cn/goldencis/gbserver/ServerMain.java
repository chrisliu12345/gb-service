package cn.goldencis.gbserver;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Server server = new Server();

        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
