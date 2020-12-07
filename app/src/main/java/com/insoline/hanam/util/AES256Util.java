package com.insoline.hanam.util;

import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.security.SecureRandom;
import java.security.MessageDigest;

/**
 * AES256Util.java : AES 256 암호화<BR/>
 * Code는 팅크웨어에서 제공.
 */
public class AES256Util
{
    public static String skey = "com.insoline.hanam";

    // base64 문자열을 aes복호화하여 문자열 리턴
    public static String decode(String base64Text, String skey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        if(base64Text == null || base64Text.trim().isEmpty())
        {
            return base64Text;
        }

        byte[] key = skey.getBytes();
//        byte[] inputArr = Base64.getUrlDecoder().decode(base64Text);
        byte[] inputArr = Base64.decode(base64Text, Base64.URL_SAFE);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key);
        byte[] tmp = md.digest();
        byte[] newKey = new byte[16];
        System.arraycopy(tmp, 0, newKey, 0, 16);

        //SecretKeySpec skSpec = new SecretKeySpec(key, "AES");
        SecretKeySpec skSpec = new SecretKeySpec(newKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        int blockSize = cipher.getBlockSize();

        IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(inputArr, blockSize));
        byte[] dataToDecrypt = Arrays.copyOfRange(inputArr, blockSize, inputArr.length);
        cipher.init(Cipher.DECRYPT_MODE, skSpec, iv);
        byte[] result = cipher.doFinal(dataToDecrypt);

        if(result != null)
        {
            return new String(result, StandardCharsets.UTF_8);
        }
        else
        {
            return null;
        }
    }

    //// 문자열을 aes 암호화하여 base64 스트링으로 반환
    public static String encode(String oriText, String skey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
    {
        if(oriText == null || oriText.trim().isEmpty())
        {
            return oriText;
        }

        // key sha256해시값중 앞 16바이트를 key값으로 사용
        byte[] key = skey.getBytes();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key);
        byte[] tmp = md.digest();
        byte[] newKey = new byte[16];
        System.arraycopy(tmp, 0, newKey, 0, 16);

        SecureRandom rand = new SecureRandom();
        SecretKeySpec key_spec = new SecretKeySpec(newKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        byte[] encoded_payload = oriText.getBytes();
        int block_size = cipher.getBlockSize();
        byte[] buffer = new byte[block_size];
        rand.nextBytes(buffer);
        IvParameterSpec iv = new IvParameterSpec(buffer);
        buffer = Arrays.copyOf(buffer, block_size + encoded_payload.length);
        cipher.init(Cipher.ENCRYPT_MODE, key_spec, iv);
        try
        {
            cipher.doFinal(encoded_payload, 0, encoded_payload.length, buffer, block_size);
        }
        catch(Exception e)
        {

        }
//        String encoded = Base64.getUrlEncoder().encodeToString(buffer);
        String encoded = Base64.encodeToString(buffer, Base64.URL_SAFE);

        return encoded;
    }
}