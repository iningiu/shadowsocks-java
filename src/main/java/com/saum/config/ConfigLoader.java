package com.saum.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @Author saum
 * @Description:
 */
public class ConfigLoader {

    private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    public static Config loadConfig(String filePath)  {
        try(InputStream  in = new FileInputStream(filePath)) {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")));
            Config config = new Gson().fromJson(reader, Config.class);
            reader.close();
            return config;
        } catch (IOException e) {
            logger.error("配置加载失败，{}", e);
        }
        return null;
    }
}
