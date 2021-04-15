package telkom.eai.env.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JschUtil {
	
	public static String excecuteCommand(String host, String user, String password, String cmd) {
		String result="";
		JSch jsch;
		Session session = null;
		Channel channel = null;
		try{
             
            jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);;
            session.setPassword(password);
            
            
//            session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
//            session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
//            session.setConfig("compression_level", "9");
            
            
            session.connect();
             
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(cmd);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
             
            InputStream input = channel.getInputStream();
            channel.connect();
            
            System.out.println("Channel Connected to machine " + host + " server with command: " + cmd ); 
            
            try{
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);                
                result = bufferedReader.lines().collect(Collectors.joining("\n"));
                 
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                MiscUtils.printEx(ex);
            }
            
             
            channel.disconnect();
            session.disconnect();
        }catch(Exception ex){
        	if(channel != null) {
        		try {
					channel.disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					MiscUtils.printEx(e);
				}
        	}
        	if(session != null) {
        		try {
					session.disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					MiscUtils.printEx(e);
				}
        	}
        	MiscUtils.printEx(ex);
        	result = "Error while trying to connect to remote host: "+ex.getMessage();
        }
	return result;
	}

}
