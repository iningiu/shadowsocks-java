package com.saum.crypto;

/**
 * @Author saum
 * @Description: 不加密
 */
public class NoneCrypto implements Crypto {

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data;
    }
}
