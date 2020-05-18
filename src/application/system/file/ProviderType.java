package application.system.file;

public enum ProviderType {
	NONE, LOCAL, WEBDAV, FTP;

	public boolean isLocal() {
		return equals(LOCAL);
	}

	public boolean isWebDav() {
		return equals(WEBDAV);
	}

	public boolean isFTP() {
		return equals(FTP);
	}

	public boolean isOnNetwork() {
		return equals(WEBDAV) || equals(FTP);
	}

}
