package mx.nic.rdap.client.wallet;

import javax.crypto.SecretKey;

import mx.nic.rdap.client.dao.object.EncryptedWalletKey;
import mx.nic.rdap.client.dao.object.RdapClientUser;

public class WalletUser {

	private RdapClientUser user;

	private EncryptedWalletKey encryptedWalletKey;

	private SecretKey pbeKey;

	private SecretKey userWalletKey;

	public WalletUser(RdapClientUser user, EncryptedWalletKey encryptedWalletKey, SecretKey pbeKey,
			SecretKey userWalletKey) {
		super();
		this.user = user;
		this.encryptedWalletKey = encryptedWalletKey;
		this.pbeKey = pbeKey;
		this.userWalletKey = userWalletKey;
	}

	public RdapClientUser getUser() {
		return user;
	}

	public void setUser(RdapClientUser user) {
		this.user = user;
	}

	public EncryptedWalletKey getEncryptedWalletKey() {
		return encryptedWalletKey;
	}

	public void setEncryptedWalletKey(EncryptedWalletKey encryptedWalletKey) {
		this.encryptedWalletKey = encryptedWalletKey;
	}

	public SecretKey getPbeKey() {
		return pbeKey;
	}

	public void setPbeKey(SecretKey pbeKey) {
		this.pbeKey = pbeKey;
	}

	public SecretKey getUserWalletKey() {
		return userWalletKey;
	}

	public void setUserWalletKey(SecretKey userWalletKey) {
		this.userWalletKey = userWalletKey;
	}

}
