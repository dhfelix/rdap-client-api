package mx.nic.rdap.client.wallet;

import javax.crypto.SecretKey;

import mx.nic.rdap.client.dao.object.WalletUser;

public class User {

	private WalletUser walletUser;

	private SecretKey pbeKey;

	private SecretKey userWalletKey;

	public User(WalletUser walletUser, SecretKey pbeKey, SecretKey userWalletKey) {
		super();
		this.walletUser = walletUser;
		this.pbeKey = pbeKey;
		this.userWalletKey = userWalletKey;
	}

	public WalletUser getWalletUser() {
		return walletUser;
	}

	public void setWalletUser(WalletUser walletUser) {
		this.walletUser = walletUser;
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
