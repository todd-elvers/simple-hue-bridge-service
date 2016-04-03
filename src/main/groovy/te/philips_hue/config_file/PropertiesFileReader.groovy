package te.philips_hue.config_file

import groovy.transform.CompileStatic

@CompileStatic
class PropertiesFileReader {
    static Properties loadPropertiesFromInputStream(InputStream inputStream) {
        try {
            def properties = new Properties()
            properties.load(inputStream)
            return properties
        } finally {
            inputStream.close()
        }
    }
}
