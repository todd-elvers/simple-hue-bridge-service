package te.philips_hue.config_file

import groovy.transform.CompileStatic

//TODO: Look around for utility classes that read/write properties files
@CompileStatic
class PropertiesFileHandler {

    static Properties read(File propFile) {
        Properties props = new Properties()
        propFile.withInputStream { inputStream ->
            props.load(inputStream)
        }

        return props
    }

    static void write(File propFile, Properties properties, String comments) {
        propFile.withOutputStream { outputStream ->
            properties.store(outputStream, comments)
        }
    }

}
