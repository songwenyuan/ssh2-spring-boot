package platform.work.pluto.config;

import platform.work.pluto.utils.SSHConnection;

//@Configuration
public class SSHConfig {
    //@Bean
    public SSHConnection sshConnection() {
        SSHConnection conexionssh = null;
        System.out.println("Context initialized ... !");
        try {
            conexionssh = new SSHConnection();
        } catch (Throwable e) {
            e.printStackTrace(); // error connecting SSH server
        }
        return conexionssh;
    }

}
