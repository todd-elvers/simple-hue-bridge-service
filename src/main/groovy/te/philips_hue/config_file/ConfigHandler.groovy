package te.philips_hue.config_file

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import wslite.rest.RESTClient


/**
 * Handles the creation/updating/reading of the configuration file that stores the Hue bridge credentials.
 */
@Slf4j
@Singleton(strict = false)
class ConfigHandler {
    public static final String CONFIG_FILE_NAME = "cached-hue-bridge-credentials.properties"
    public static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir"
    public static final String TEMP_DIR_SYSTEM_PROPERTY_INVALID_MSG = "Could not determine your temp directory's location.\n This should never really happen... but setting the JVM arg 'java.io.tmpdir' will fix this problem."
    public static final String IP_ADDRESS_PROP = 'ip_address'
    public static final String USERNAME_PROP = 'username'

    private final File temporaryDirectory
    private final File configurationFile

    private ConfigHandler() {
        String tempDirPath = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY)
        if(!tempDirPath) throw new FileNotFoundException(TEMP_DIR_SYSTEM_PROPERTY_INVALID_MSG)

        temporaryDirectory = new File(tempDirPath)
        configurationFile = new File(temporaryDirectory, CONFIG_FILE_NAME)

        // Create the config file if it doesn't exist
        if(!configurationFile.exists()) {
            FileUtils.writeStringToFile(configurationFile, "", false)
        }
    }

    void updateConfigFile(Map propsToAdd) {
        Properties configFileProps = readConfigProperties()
        configFileProps.putAll(propsToAdd)

        log.debug("Updating config file contents to:\n\t{}", configFileProps.collect{"$it.key: $it.value"}.join("\n\t"))
        configFileProps.store(new FileOutputStream(configurationFile), "simple-hue-bridge-service's config file for caching Hue credentials")
    }

    Properties readConfigProperties() {
        PropertiesFileReader.loadPropertiesFromInputStream(FileUtils.openInputStream(configurationFile))
    }

    boolean configFileIsStillValid() {
        Properties props = readConfigProperties()
        credentialsExist(props) && credentialsStillValid(props)
    }

    private static boolean credentialsExist(Properties props) {
        props.get(IP_ADDRESS_PROP) && props.get(USERNAME_PROP)
    }

    private static boolean credentialsStillValid(Properties props) {
        try {
            String username = props.get(USERNAME_PROP)
            String ipAddress = props.get(IP_ADDRESS_PROP)
            return !(new RESTClient("http://$ipAddress/api/$username").get().json.error)
        } catch (Throwable ignored) {
            return false
        }
    }
}
