package te.philips_hue.config_file

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.model.PHBridge
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import wslite.rest.RESTClient


/**
 * Handles the creation/updating/reading of the config file that stores the Hue bridge credentials.
 */
@Slf4j
@CompileStatic
@Singleton(strict = false)
class ConfigFileHandler {
    private static final String CONFIG_FILE_NAME = "cached-hue-bridge-credentials.properties"
    private static final String CONFIG_FILE_COMMENTS = "Philips Hue Credentials - created by simple-hue-bridge-service."
    private static final String IP_ADDRESS_PROP = 'ip_address'
    private static final String USERNAME_PROP = 'username'
    private static final File CONFIG_FILE = new File(System.getProperty("java.io.tmpdir"), CONFIG_FILE_NAME)

    // strict=false above allows us to have a private constructor
    private ConfigFileHandler() {
        if(!CONFIG_FILE.exists()) {
            FileUtils.writeStringToFile(CONFIG_FILE, "", false)
        }
    }

    void updateConfigFile(PHBridge bridge, String username) {
        Properties configFileProps = PropertiesFileReader.read(CONFIG_FILE)
        configFileProps.putAll([
                (USERNAME_PROP)  : username,
                (IP_ADDRESS_PROP): bridge.resourceCache.bridgeConfiguration.ipAddress
        ])

        log.debug("Updating config file contents to:\n\t{}", configFileProps.collect{"$it.key: $it.value"}.join("\n\t"))

        CONFIG_FILE.withOutputStream { outputStream ->
            configFileProps.store(outputStream, CONFIG_FILE_COMMENTS)
        }
    }

    PHAccessPoint readAccessPointFromConfigFile() {
        Properties props = PropertiesFileReader.read(CONFIG_FILE)
        return new PHAccessPoint(
                ipAddress: props.getProperty(IP_ADDRESS_PROP),
                username : props.getProperty(USERNAME_PROP)
        )
    }

    boolean configFileIsStillValid() {
        Properties props = PropertiesFileReader.read(CONFIG_FILE)
        return credentialsExist(props) && credentialsStillValid(props)
    }

    private static boolean credentialsExist(Properties props) {
        return props.get(IP_ADDRESS_PROP) && props.get(USERNAME_PROP)
    }

    private static boolean credentialsStillValid(Properties props) {
        try {
            String username = props.get(USERNAME_PROP)
            String ipAddress = props.get(IP_ADDRESS_PROP)
            return !new RESTClient("http://$ipAddress/api/$username").get().propertyMissing('json')['error']
        } catch (Throwable ignored) {
            return false
        }
    }
}
