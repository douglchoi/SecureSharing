package cecs.secureshare.security;

import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

/**
 * Uses Diffe-Hellman to generate public-private keys for a session. This is used for encrypting
 * and decrypting files
 * Created by Douglas on 11/15/2015.
 */
public class UnusedDHKeyGenerator {

    public static KeyPair generateKeys() {
        try {
            ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp224k1");
            //ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("prime192v1");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH","SC");
            kpg.initialize(ecParamSpec);
            return kpg.generateKeyPair();

        }catch(Exception e){
            Log.e("Keygen", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /*
    public UnusedDHKeyGenerator()
            throws NoSuchAlgorithmException,
                InvalidParameterSpecException,
                InvalidAlgorithmParameterException,
                InvalidKeyException {

        // TODO: Update this for actual implementation
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(512); // number of bits
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);

        BigInteger p512 = dhSpec.getP();
        BigInteger g512 = dhSpec.getG();

        KeyPairGenerator bkpg = KeyPairGenerator.getInstance("DiffieHellman");

        DHParameterSpec param2 = new DHParameterSpec(p512, g512);
        System.out.println("Prime: " + p512);
        System.out.println("Base: " + g512);
        bkpg.initialize(param2);
        KeyPair kp2 = bkpg.generateKeyPair();

        KeyAgreement bKeyAgree = KeyAgreement.getInstance("DiffieHellman");

        bKeyAgree.init(kp2.getPrivate());

        // Send this to UnusedFileCBCCipher on initialization
        PublicKey publicKey = kp2.getPublic();
        PrivateKey privateKey = kp2.getPrivate();
    }
*/

}
