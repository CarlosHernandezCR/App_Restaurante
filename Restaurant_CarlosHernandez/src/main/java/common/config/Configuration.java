package common.config;
import common.constants.Constants;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private Properties p;
    private Configuration() {
        try {
            p = new Properties();
            p.loadFromXML(Configuration.class.getClassLoader().getResourceAsStream(Constants.PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return p.getProperty(key);
    }
}