package cn.goldencis.gbservice.core;

public class InitParameter {

    private String localIp;
    private String serverIp;

    private int localPort;
    private int serverPort;
    private String serverUsername;
    private String localUsername;
    private String realm;
    private String password;

    public InitParameter(String localIp, String serverIp, int localPort, int serverPort, String localUsername, String serverUsername, String realm, String password) {
        this.localIp = localIp;
        this.serverIp = serverIp;
        this.localPort = localPort;
        this.serverPort = serverPort;
        this.localUsername = localUsername;
        this.serverUsername = serverUsername;
        this.realm = realm;
        this.password = password;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerUsername() {
        return serverUsername;
    }

    public void setServerUsername(String serverUsername) {
        this.serverUsername = serverUsername;
    }

    public String getLocalUsername() {
        return localUsername;
    }

    public void setLocalUsername(String localUsername) {
        this.localUsername = localUsername;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
