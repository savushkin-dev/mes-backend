package com.host.SpringBootAutomationProduction.util;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class Encryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "1234567890123456"; // 16 байт для AES-128
    private static final String IV = "1234567890123456"; // 16 байт для IV

    public static String encrypt(String data)  {
        try{
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(IV.getBytes());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e){
            log.error("Encryption failed", e);
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedData)  {
        try{
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(IV.getBytes());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e){
            log.error("Decryption failed", e);
            throw new RuntimeException(e);
        }
    }

}
