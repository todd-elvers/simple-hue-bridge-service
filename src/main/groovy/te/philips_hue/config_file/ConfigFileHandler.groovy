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
@Singleton(strict = false, lazy = true)
class ConfigFileHandler {
    private static final String CONFIG_FILE_NAME = "cached-hue-bridge-credentials.properties"
    private static final String CONFIG_FILE_COMMENTS = "Philips Hue Bridge Credentials - created by simple-hue-bridge-service."
    private static final String IP_ADDRESS_PROP = 'ip_address'
    private static final String USERNAME_PROP = 'username'
    private static File configFile = null

    /**
     * Initializes this ConfigFileHandler with a specific config file for storing hue bridge credentials.
     *
     * @param specificConfigFile the exact file where the hue credentials should be written to
     */
    static void initWithSpecificConfigFile(File specificConfigFile) {
        log.debug("Initializing w/ specific config file.")
        if(configFile) throw new IllegalStateException("ConfigFileHandler has already been initialized.")
        if(!specificConfigFile.exists()) throw new FileNotFoundException("Could not find file $specificConfigFile.")
        configFile = specificConfigFile
    }

    /**
     * Initializes this ConfigFileHandler with a config file located in the user's temp. directory.
     */
    static void initWithTempDirConfigFile() {
        log.debug("Initializing w/ temp. dir config file.")
        if(configFile) throw new IllegalStateException("ConfigFileHandler has already been initialized.")
        configFile = new File(System.getProperty("java.io.tmpdir"), CONFIG_FILE_NAME)
    }

    // strict=false above allows us to have a private constructor
    private ConfigFileHandler() {
        if(!configFile) throw new IllegalStateException("ConfigFileHandler was not initialized properly.")
        if(!configFile.exists()) {
            FileUtils.writeStringToFile(configFile, "", false)
        }
    }

    void updateConfigFile(PHBridge bridge, String username) {
        Properties configFileProps = PropertiesFileReader.read(configFile)
        configFileProps.putAll([
                (USERNAME_PROP)  : username,
                (IP_ADDRESS_PROP): bridge.resourceCache.bridgeConfiguration.ipAddress
        ])

        log.debug("Updating config file contents to:\n\t{}", configFileProps.collect{"$it.key: $it.value"}.join("\n\t"))

        configFile.withOutputStream { outputStream ->
            configFileProps.store(outputStream, CONFIG_FILE_COMMENTS)
        }
    }

    PHAccessPoint readAccessPointFromConfigFile() {
        Properties props = PropertiesFileReader.read(configFile)

        return new PHAccessPoint(
                ipAddress: props.getProperty(IP_ADDRESS_PROP),
                username : props.getProperty(USERNAME_PROP)
        )
    }

    boolean configFileIsStillValid() {
        println configFile
        Properties props = PropertiesFileReader.read(configFile)
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
