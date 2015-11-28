package cecs.secureshare.security;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

/**
 * Uses Diffe-Hellman to generate public-private keys for a session. This is used for encrypting
 * and decrypting files
 * Created by Douglas on 11/15/2015.
 */
public class DHKeyGenerator {
    PublicKey pkey;
    PrivateKey skey;

    // Register BouncyCastle for use
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public DHKeyGenerator()
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

        // Send this to FileCBCCipher on initialization
        PublicKey publicKey = kp2.getPublic();
        PrivateKey privateKey = kp2.getPrivate();
    }

    public void GenerateKeys()
    {
        try {
            //ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp224k1");
            ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("prime192v1");
            KeyPairGenerator kpg = null;
            try {
                kpg = KeyPairGenerator.getInstance("ECDH","SC");
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                kpg.initialize(ecParamSpec);
            } catch (InvalidAlgorithmParameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            KeyPair kpair=kpg.generateKeyPair();

            pkey=kpair.getPublic();
            skey=kpair.getPrivate();

        }catch(Exception e){e.printStackTrace();}
    }
}
