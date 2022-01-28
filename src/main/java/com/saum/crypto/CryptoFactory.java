package com.saum.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author saum
 * @Description:
 */
public class CryptoFactory {

    private final static Logger logger = LoggerFactory.getLogger(CryptoFactory.class);

    public static Crypto create(String method, String password){
        logger.info("loading crypto [" + method + "]");
        method = method.toLowerCase();
        switch (method){
            case "ase-256-cfb":
                return null;
            case "simple":
                return null;
            case "none":
                return new NoneCrypto();
        }
        return null;
    }
}
