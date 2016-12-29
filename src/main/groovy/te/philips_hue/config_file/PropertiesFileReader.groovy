package te.philips_hue.config_file

import groovy.transform.CompileStatic

@CompileStatic
class PropertiesFileReader {
    static Properties read(File propFile) {
        def properties = new Properties()
        propFile.withInputStream { inputStream ->
            properties.load(inputStream)
        }
        return properties
    }
}
