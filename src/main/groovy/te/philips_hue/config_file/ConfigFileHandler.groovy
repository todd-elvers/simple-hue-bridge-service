package te.philips_hue.config_file

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.model.PHBridge
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

/**
 * Handles the creation/updating/reading of the config file that stores the Hue bridge credentials.
 */
@Slf4j
@CompileStatic
@SuppressWarnings("GrMethodMayBeStatic")
//TODO: Is this actually a CredentialsFileHandler?  Could this be expanded to be more general?
//TODO: Simplify to only what is currently necessary
//TODO: Could these be generalized to strategies or something?  So someone could quickly supplement their own.
class ConfigFileHandler {
    public static final String CONFIG_FILE_NAME = "cached-hue-bridge-credentials.properties"
    public static final String CONFIG_FILE_COMMENTS = "Philips Hue Bridge Credentials - created by simple-hue-bridge-service."
    public static final String IP_ADDRESS_PROP = 'ip_address'
    public static final String USERNAME_PROP = 'username'
    private static ConfigFileHandler instance = null
    private static File configFile = null

    /**
     * Initializes this ConfigFileHandler with a specific config file for storing hue bridge credentials.
     *
     * @param specificConfigFile the exact file where the hue credentials should be written to
     */
    static void initWithSpecificConfigFile(File specificConfigFile) {
        if (configFile) throw new IllegalStateException("ConfigFileHandler has already been initialized.")
        if (!specificConfigFile.exists()) throw new FileNotFoundException("Could not find file $specificConfigFile.")

        configFile = specificConfigFile
    }

    /**
     * Initializes this ConfigFileHandler with a config file located in the user's temp. directory.
     */
    static void initWithTempDirConfigFile() {
        if (configFile) throw new IllegalStateException("ConfigFileHandler has already been initialized.")

        configFile = new File(FileUtils.getTempDirectory(), CONFIG_FILE_NAME)
    }

    //TODO: Why did we end up going away from the @Singleton AST transformation?
    static ConfigFileHandler getInstance() {
        if (!instance) instance = new ConfigFileHandler()
        return instance
    }

    private ConfigFileHandler() {
        if (!configFile) throw new IllegalStateException("ConfigFileHandler was not initialized properly.")

        if (!configFile.exists()) {
            FileUtils.touch(configFile)
        }
    }

    void updateConfigFile(PHBridge bridge, String username) {
        Properties configFileProps = PropertiesFileHandler.read(configFile)
        configFileProps.putAll([
                (USERNAME_PROP)  : username,
                (IP_ADDRESS_PROP): bridge.resourceCache.bridgeConfiguration.ipAddress
        ])

        log.debug("Updating config file contents to:\n\t{}", configFileProps.collect { "$it.key: $it.value" }.join("\n\t"))

        PropertiesFileHandler.write(configFile, configFileProps, CONFIG_FILE_COMMENTS)
    }

    PHAccessPoint readAccessPointFromConfigFile() {
        Properties props = PropertiesFileHandler.read(configFile)

        return new PHAccessPoint(
                username: props.getProperty(USERNAME_PROP),
                ipAddress: props.getProperty(IP_ADDRESS_PROP)
        )
    }

    boolean configFileIsStillValid() {
        Properties props = PropertiesFileHandler.read(configFile)

        return credentialsExist(props) && credentialsStillValid(props)
    }

    void shutdown() {
        instance = null
        configFile = null
    }

    private static boolean credentialsExist(Properties props) {
        return props.get(IP_ADDRESS_PROP) && props.get(USERNAME_PROP)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    private static boolean credentialsStillValid(Properties props) {
        try {
            String username = props.get(USERNAME_PROP)
            String ipAddress = props.get(IP_ADDRESS_PROP)

            return !new JsonSlurper().parse("http://$ipAddress/api/$username".toURL()).error
        } catch (Throwable ignored) {
            return false
        }
    }
}
