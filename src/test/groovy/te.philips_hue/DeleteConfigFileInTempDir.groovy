package te.philips_hue

import org.apache.commons.io.FileUtils
import te.philips_hue.config_file.ConfigFileHandler

class DeleteConfigFileInTempDir {
    static void main(String[] args) {
        FileUtils.deleteQuietly(ConfigFileHandler.getInstance().configFile)
    }
}
