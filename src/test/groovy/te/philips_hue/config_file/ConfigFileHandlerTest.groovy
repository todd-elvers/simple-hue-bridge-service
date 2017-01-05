package te.philips_hue.config_file

import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class ConfigFileHandlerTest extends Specification {

    void "calling getInstance() before initialization throws an exception"() {
        when:
            ConfigFileHandler.getInstance()

        then:
            thrown(IllegalStateException)
    }

    void "calling getInstance() after initialization throws no exception"() {
        when:
            ConfigFileHandler.initWithTempDirConfigFile()
            ConfigFileHandler.getInstance()

        then:
            noExceptionThrown()
    }

    void "initializing more than once causes an exception"() {
        when:
            ConfigFileHandler.initWithTempDirConfigFile()
            ConfigFileHandler.initWithTempDirConfigFile()

        then:
            thrown(IllegalStateException)
    }
}
