package mx.nic.rdap.client.wallet;

public class RdapCredential {

	Long id;

	String username;

	String password;

	String serverId;

	public RdapCredential() {
		super();
	}

	public RdapCredential(Long id, String username, String password, String serverId) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.serverId = serverId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

}
