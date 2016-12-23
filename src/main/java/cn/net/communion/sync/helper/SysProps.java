package cn.net.communion.sync.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class SysProps {
    private static Logger logger = Logger.getLogger(SysProps.class);
    private static String path = "sys.properties";;
    private static Properties props = new Properties();


    static {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            props.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static void update(String key, String value) {
        try {
            OutputStream fos = new FileOutputStream(path);
            props.setProperty(key, value);
            props.store(fos, "last job finish at");
            fos.close();
        } catch (IOException e) {
            logger.info("update " + key + " failed");
        }
    }
}
