package com.saum.crypto;

/**
 * @Author saum
 * @Description:
 */
public interface Crypto {
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
