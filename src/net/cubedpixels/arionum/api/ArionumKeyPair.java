package net.cubedpixels.arionum.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.encoders.Base64;

import javafx.stage.Stage;
import net.cubedpixels.arionum.ui.Modal;

@SuppressWarnings("deprecation")
public class ArionumKeyPair {
 
    private String publicKey;
    private String privateKey;
    private KeyPair keyPair;
 
    public ArionumKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
        try {
            String der = privateKeyToDER(keyPair.getPrivate());
            String ders = publicKeyToDER(keyPair.getPublic());

            boolean firstCheck = check(der);
            boolean secondCheck = check(der, ders, keyPair.getPublic());
            boolean thirdCheck = check(keyPair);
            

            System.out.println(der + "CHECK: " + firstCheck);
            System.out.println(ders + "CHECK Bidur: " + secondCheck);
            System.out.println("WTFCheck: " + thirdCheck);
            
            if(!firstCheck)
            {
            	new Modal("Error while generating Wallet", "Check1 equals false! Report to @Cuby").show(new Stage());
            	return;
            }
            if(!secondCheck)
            {
            	new Modal("Error while generating Wallet", "Check2 equals false! Report to @Cuby").show(new Stage());
            	return;
            }
            if(!thirdCheck)
            {
            	new Modal("Error while generating Wallet", "Check3 equals false! Report to @Cuby").show(new Stage());
            	return;
            }
 
            this.privateKey = Base58.pem2coin(der);
            this.publicKey = Base58.pem2coin(ders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static boolean check(String der) {
        try {
            PEMParser parser = new PEMParser(new StringReader(der));
            PEMKeyPair kp = (PEMKeyPair) parser.readObject();
            parser.close();
            AsymmetricKeyParameter privKey = PrivateKeyFactory.createKey(kp.getPrivateKeyInfo());
            AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(null, privKey);
 
            ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(Base58.getPrivateKeyFromECBigIntAndCurve(privateKey.getD(), "secp256k1"));
            signature.update("test".getBytes());
            signature.sign();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 
    public static boolean check(String der, String ders, PublicKey pub) {
        try {
            PEMParser parser = new PEMParser(new StringReader(der));
            PEMKeyPair kp = (PEMKeyPair) parser.readObject();
            parser.close();
            AsymmetricKeyParameter privKey = PrivateKeyFactory.createKey(kp.getPrivateKeyInfo());
            AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(null, privKey);
 
            ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(Base58.getPrivateKeyFromECBigIntAndCurve(privateKey.getD(), "secp256k1"));
            signature.update("test".getBytes());
            byte[] signdata = signature.sign();
           
            Signature verify = Signature.getInstance("SHA256withECDSA", "BC");
            verify.initVerify(pub);
            verify.update("test".getBytes());
            boolean verify1 = verify.verify(signdata);
            System.out.println("Internal first check:" + verify1);
           
            PEMParser parser2 = new PEMParser(new StringReader(ders));
            SubjectPublicKeyInfo kp2 = (SubjectPublicKeyInfo) parser2.readObject();
            parser2.close();
 
            AsymmetricKeyParameter pubKey = PublicKeyFactory.createKey(kp2);
            AsymmetricCipherKeyPair keyPair2 = new AsymmetricCipherKeyPair(pubKey, null);
           
            ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair2.getPublic();
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey truPubKey = keyFactory.generatePublic(new ECPublicKeySpec(publicKey.getQ(), ECNamedCurveTable.getParameterSpec("secp256k1")));
            Signature verify2 = Signature.getInstance("SHA256withECDSA", "BC");
            verify2.initVerify(truPubKey);
            verify2.update("test".getBytes());
            boolean verify2a = verify2.verify(signdata);
            System.out.println("Internal second check:" + verify2a);
           
            return verify1 && verify2a;
           
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
   
    public static boolean check(KeyPair keys) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(keys.getPrivate());
            signature.update("test".getBytes());
            byte[] signdata = signature.sign();
            System.out.println("Sign: " + Base64.toBase64String(signdata));
            Signature verify = Signature.getInstance("SHA256withECDSA", "BC");
            verify.initVerify(keys.getPublic());
            verify.update("test".getBytes());
            return verify.verify(signdata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
   
    public static String privateKeyToDER(PrivateKey key) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PEMWriter pemWriter = new PEMWriter(new OutputStreamWriter(bos));
        pemWriter.writeObject(key);
        pemWriter.close();
        return new String(bos.toByteArray());
    }
 
    public static String publicKeyToDER(PublicKey key) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PEMWriter pemWriter = new PEMWriter(new OutputStreamWriter(bos));
        pemWriter.writeObject(key);
        pemWriter.close();
        return new String(bos.toByteArray());
    }
 
    public KeyPair getKeyPair() {
        return keyPair;
    }
 
    public String getPrivateKey() {
        return privateKey;
    }
 
    public String getPublicKey() {
        return publicKey;
    }
 
}