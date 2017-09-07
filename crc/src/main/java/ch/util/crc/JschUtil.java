package ch.util.crc;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
public class JschUtil {
/*
git@gitlab.e104.com.tw:NCC-Plus-AP/1731.git
https://gitlab.com/jimmyhsuz3gl/ch-zhao-a.git
git@gitlab.com:jimmyhsuz3gl/ch-zhao-a.git
*/
	private final static String passphrase;
	private final static String private_key_path;
	private final static String user_name;
	private final static String password;
	static {
		java.io.BufferedReader br = null;
		java.util.Map<String, String> map = new java.util.HashMap<String, String>();
		try {
			br = new java.io.BufferedReader(new java.io.FileReader("D:\\vm\\config.prop"));
			String line;
				while((line = br.readLine()) != null){
					int i = line.indexOf('=');
					if (i != -1)
						map.put(line.substring(0, i), line.substring(i + 1));
				}
		} catch (java.io.FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
		}
		passphrase = map.get("passphrase");
		private_key_path = map.get("private_key_path");
		user_name = map.get("user_name");
		password = map.get("password");
	}
	private static TransportConfigCallback config;
	public static TransportConfigCallback config(){
		if (config != null)
			return config;
		// .setCredentialsProvider(new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider("user", "password"));
		final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(Host host, Session session) {
				// session.setPassword( "password" );
			}
			@Override
			protected JSch createDefaultJSch( FS fs ) throws JSchException {
				JSch defaultJSch = super.createDefaultJSch(fs);
				defaultJSch.addIdentity(private_key_path, passphrase);
				return defaultJSch;
			}
		};
		TransportConfigCallback transportConfigCallback = new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				if (transport instanceof SshTransport){
					SshTransport sshTransport = (SshTransport) transport;
					sshTransport.setSshSessionFactory(sshSessionFactory);
				}
			}
		};
		config = transportConfigCallback;
		return config;
	}
	public static org.eclipse.jgit.transport.CredentialsProvider getCredentialsProvider(){
		return new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(user_name, password);
	}
}