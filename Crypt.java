import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

//contains all crypting and hashing functions
public class Crypt {
	public String symAlg;
	public String asymAlg;
	public String hashAlg;

	public Crypt(String symAlg,String asymAlg,String hashAlg){
		this.symAlg=symAlg;
		this.asymAlg=asymAlg;
		this.hashAlg=hashAlg;
	}

	public KeyPair generateAsymmetricKey() throws NoSuchAlgorithmException {//RSA
		KeyPairGenerator generator = KeyPairGenerator.getInstance(this.asymAlg);
		generator.initialize(4096);
		KeyPair keyPair = generator.generateKeyPair();
		return keyPair;
	}

	public byte[] encryptAsym(PublicKey key, byte[] plaintext) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    // Decrittazione asimmetrica
    public byte[] decryptAsym(PrivateKey key, byte[] ciphertext) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm() + "/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }

	public SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {//AES
		KeyGenerator generator = KeyGenerator.getInstance(this.symAlg);
		generator.init(256);
		SecretKey key = generator.generateKey();
		return key;
	}

	 public byte[] encryptSym(java.security.Key key, byte[] iv, byte[] plaintext)
				throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(plaintext);
    }

    // Decrittazione simmetrica
    public byte[] decryptSym(java.security.Key key, byte[] iv, byte[] ciphertext)
			throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

	public byte[] generateInitVector() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
	}
	
	public byte[] hash(byte[] input) throws NoSuchAlgorithmException {//SHA-256
		// and an hash
		// algorithm. gives out
		// the hash
		MessageDigest md = MessageDigest.getInstance(this.hashAlg);

		byte[] output = md.digest(input);
		return output;
	}

	public byte[] saltGen(){//GENERATES SALT
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[2];
        random.nextBytes(salt);
        return salt;
    }

}