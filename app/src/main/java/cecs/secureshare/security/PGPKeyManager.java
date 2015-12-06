package cecs.secureshare.security;

import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.bcpg.sig.Features;
import org.spongycastle.bcpg.sig.KeyFlags;
import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPKeyRingGenerator;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.bc.BcPGPKeyPair;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.lang.Exception;
import java.util.Iterator;


/**
 * Created by Harshal on 12/5/2015.
 */
public class PGPKeyManager {

    public PGPPublicKey pk;
    public PGPSecretKey sk;

    public void GenerataKeys(String UniqueID, char[] pass) {
        //"alice@example.com"char pass[] = {'h', 'e', 'l', 'l', 'o'};
        try {
            PGPKeyRingGenerator krgen = generateKeyRingGenerator(UniqueID.toString(), pass);

            // Generate public key ring, dump to file.
            PGPPublicKeyRing pkr = krgen.generatePublicKeyRing();
            // Generate private key, dump to file.
            PGPSecretKeyRing skr = krgen.generateSecretKeyRing();

            Iterator<PGPPublicKey> pkIt = pkr.getPublicKeys();
            pkIt.next();
            pk  = pkIt.next();

            Iterator<PGPSecretKey> skIt = skr.getSecretKeys();
            skIt.next();
            sk  = skIt.next();


            //PGPKeyRingGenerator krgen = generateKeyRingGenerator("alice@example.com", pass);

            // Generate public key ring, dump to file.
            //PGPPublicKeyRing pkr = krgen.generatePublicKeyRing();

            /*ArmoredOutputStream pubout = new ArmoredOutputStream(new BufferedOutputStream(new FileOutputStream("/home/user/dummy.asc")));
            pkr.encode(pubout);
            pubout.close();*/

            // Generate private key, dump to file.
            //PGPSecretKeyRing skr = krgen.generateSecretKeyRing();
            /*BufferedOutputStream secout = new BufferedOutputStream(new FileOutputStream("/home/user/dummy.skr"));
            skr.encode(secout);
            secout.close();*/
        }
        catch (Exception e) {

        }
    }


    private  PGPKeyRingGenerator generateKeyRingGenerator
            (String id, char[] pass)
            throws Exception {
        return generateKeyRingGenerator(id, pass, 0xc0);
    }

    private final PGPKeyRingGenerator generateKeyRingGenerator
            (String id, char[] pass, int s2kcount)
            throws Exception {
        // This object generates individual key-pairs.
        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        // Boilerplate RSA parameters, no need to change anything
        // except for the RSA key-size (2048). You can use whatever
        // key-size makes sense for you -- 4096, etc.
        kpg.init
                (new RSAKeyGenerationParameters
                        (BigInteger.valueOf(0x10001),
                                new SecureRandom(), 2048, 12));

        // First create the master (signing) key with the generator.
        PGPKeyPair rsakp_sign =
                new BcPGPKeyPair
                        (PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
        // Then an encryption subkey.
        PGPKeyPair rsakp_enc =
                new BcPGPKeyPair
                        (PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

        // Add a self-signature on the id
        PGPSignatureSubpacketGenerator signhashgen =
                new PGPSignatureSubpacketGenerator();

        // Add signed metadata on the signature.
        // 1) Declare its purpose
        signhashgen.setKeyFlags
                (false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);
        // 2) Set preferences for secondary crypto algorithms to use
        //    when sending messages to this key.
        signhashgen.setPreferredSymmetricAlgorithms
                (false, new int[]{
                        SymmetricKeyAlgorithmTags.AES_256,
                        SymmetricKeyAlgorithmTags.AES_192,
                        SymmetricKeyAlgorithmTags.AES_128
                });
        signhashgen.setPreferredHashAlgorithms
                (false, new int[]{
                        HashAlgorithmTags.SHA256,
                        HashAlgorithmTags.SHA1,
                        HashAlgorithmTags.SHA384,
                        HashAlgorithmTags.SHA512,
                        HashAlgorithmTags.SHA224,
                });
        // 3) Request senders add additional checksums to the
        //    message (useful when verifying unsigned messages.)
        signhashgen.setFeature
                (false, Features.FEATURE_MODIFICATION_DETECTION);

        // Create a signature on the encryption subkey.
        PGPSignatureSubpacketGenerator enchashgen =
                new PGPSignatureSubpacketGenerator();
        // Add metadata to declare its purpose
        enchashgen.setKeyFlags
                (false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);

        // Objects used to encrypt the secret key.
        PGPDigestCalculator sha1Calc =
                new BcPGPDigestCalculatorProvider()
                        .get(HashAlgorithmTags.SHA1);
        PGPDigestCalculator sha256Calc =
                new BcPGPDigestCalculatorProvider()
                        .get(HashAlgorithmTags.SHA256);

        // bcpg 1.48 exposes this API that includes s2kcount. Earlier
        // versions use a default of 0x60.
        PBESecretKeyEncryptor pske =
                (new BcPBESecretKeyEncryptorBuilder
                        (PGPEncryptedData.AES_256, sha256Calc, s2kcount))
                        .build(pass);

        // Finally, create the keyring itself. The constructor
        // takes parameters that allow it to generate the self
        // signature.
        PGPKeyRingGenerator keyRingGen =
                new PGPKeyRingGenerator
                        (PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign,
                                id, sha1Calc, signhashgen.generate(), null,
                                new BcPGPContentSignerBuilder
                                        (rsakp_sign.getPublicKey().getAlgorithm(),
                                                HashAlgorithmTags.SHA256),
                                pske);

        // Add our encryption subkey, together with its signature.
        keyRingGen.addSubKey
                (rsakp_enc, enchashgen.generate(), null);
        return keyRingGen;
    }
}
