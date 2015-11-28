package cecs.secureshare.security;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * This loads Spongycastle for the entire applciation
 * Created by Douglas on 11/27/2015.
 */
public class CyptoManager {

    // Register BouncyCastle for use
    public static void initialize() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
