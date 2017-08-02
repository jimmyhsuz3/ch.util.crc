package ch.util.crc;
public class GitRepo {
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private String url;
	private String name;
	private String emailAddress;
	private String[][] heads;
	public GitRepo(String url, String name, String emailAddress, String[]... heads) {
		this.url = url;
		this.name = name;
		this.emailAddress = emailAddress;
		this.heads = heads;
	}
	public String getUrl() {
		return url;
	}
	public String getName() {
		return name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public String getHead(){
		return heads[heads.length - 1][0];
	}
	public java.util.Date getLastCommitTime() {
		try {
			return new java.text.SimpleDateFormat(DATE_FORMAT).parse(heads[heads.length - 1][1]);
		} catch (java.text.ParseException e) {
			throw new RuntimeException(e);
		}
	}
}