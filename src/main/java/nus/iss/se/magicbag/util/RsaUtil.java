package nus.iss.se.magicbag.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Getter
@Slf4j
public class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaUtil(String privateKeyPath, String publicKeyPath) throws Exception {
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    public String encrypt(String plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedBase64) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // 内部方法
    private PublicKey loadPublicKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = Files.readString(Paths.get(path));
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private PrivateKey loadPrivateKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = Files.readString(Paths.get(path));
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static void generateIfNotExists(String privateKeyPath, String publicKeyPath) throws Exception {
        Path privPath = Paths.get(privateKeyPath);
        Path pubPath = Paths.get(publicKeyPath);

        // 如果密钥已存在，跳过
        if (Files.exists(privPath) && Files.exists(pubPath)) {
            log.info("use existed RSA private key: {}",privPath);
            return;
        }

        // 创建目录
        Files.createDirectories(privPath.getParent());

        // 生成密钥对
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        writePem("PRIVATE KEY", keyPair.getPrivate().getEncoded(), privPath);
        writePem("PUBLIC KEY", keyPair.getPublic().getEncoded(), pubPath);

        // 设置权限：仅所有者可读写（Linux/Mac）
        try {
            Files.setPosixFilePermissions(privPath, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignore) {
            // Windows 不支持
        }

        log.info("new keyPair has generated: ");
        log.info("private key path : {}", privPath);
        log.info("public key: {}",pubPath);
    }

    private static void writePem(String type, byte[] data, Path path) throws IOException {
        String encoded = Base64.getEncoder().encodeToString(data);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("-----BEGIN " + type + "-----\n");
            for (int i = 0; i < encoded.length(); i += 64) {
                writer.write(encoded.substring(i, Math.min(i + 64, encoded.length())));
                writer.write("\n");
            }
            writer.write("-----END " + type + "-----\n");
        }
    }
}
