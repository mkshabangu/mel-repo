package telkom.eai.env.tools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class LogFinderTest {
	private static InputStream in=null;
	static final String serverEndpoint = "http://nbrl-eai-tapp3.telkom.co.za:9050";
	static final String filename = "searchlogs.properties";
	public static void main(String [] args) {
		
	
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e1) {
		
		}
		
		try {
			URL u = new URL(serverEndpoint+"/"+filename);
			in = u.openStream();
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, "Failed to load configuration from:\n"+serverEndpoint, "Error: ", JOptionPane.INFORMATION_MESSAGE);
		}
		
		Properties props=null;
		if(in != null) {
			props = new Properties();
			try {
				props.load(in);
				
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(null, "Failed to load configuration from:\n"+serverEndpoint, "Error: ", JOptionPane.INFORMATION_MESSAGE);
			}
			
		}
		
		SearchLogsFrame frame = new SearchLogsFrame(props);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		});
		
		frame.setSize(1200, 800);
		frame.setVisible(true);
	}

}
