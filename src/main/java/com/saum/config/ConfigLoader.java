package com.saum.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class ConfigLoader {

    public static Config loadConfig(String filePath)  {
        try(InputStream  in = new FileInputStream(filePath)) {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")));
            Config config = new Gson().fromJson(reader, Config.class);
            reader.close();
            return config;
        } catch (IOException e) {
            log.error("配置加载失败，{}", e);
        }
        return null;
    }
}
