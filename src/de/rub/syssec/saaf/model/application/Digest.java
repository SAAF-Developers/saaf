package de.rub.syssec.saaf.model.application;

/**
 * A small helper enum which wraps message digest algorithm names.
 */
public enum Digest {
	
	MD5("md5"),
	SHA256("sha-256"),
	SHA1("SHA-1"),
	FuzzyHash("Fuzzy-Hash");
	
	private final String digestName;
	
	private Digest(String digestName) {
		this.digestName = digestName;
	}
	
	@Override
	public String toString() {
		return digestName;
	}
}
