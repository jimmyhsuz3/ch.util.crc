package ch.util.crc;
public class GitRepo {
	private String url;
	private String name;
	private String emailAddress;
	private String[] heads;
	public GitRepo(String url, String name, String emailAddress, String... heads) {
		this.url = url;
		this.name = name;
		this.emailAddress = emailAddress;
		this.heads = heads;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getHead(){
		return heads[heads.length - 1];
	}
}