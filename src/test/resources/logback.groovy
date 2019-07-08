import ch.qos.logback.classic.encoder.PatternLayoutEncoder

statusListener(NopStatusListener)

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss} [%-5p] %c{1}:%L - %m%n"
    }
}

logger('org.apache.http.client.protocol.ResponseProcessCookies', ERROR)

root(INFO, ["CONSOLE"])