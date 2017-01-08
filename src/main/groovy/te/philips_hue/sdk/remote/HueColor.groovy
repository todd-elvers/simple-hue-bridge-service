package te.philips_hue.sdk.remote

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
enum HueColor {
    BLUE("xy": [0.15, 0.05], "bri": 250),
    RED("xy": [0.7, 0.25], "bri": 250),
    GREEN("xy": [0.16, 0.68], "bri": 250)

    Map mapValues
}