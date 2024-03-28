package org.springframework.core.parametername;

public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {

    public DefaultParameterNameDiscoverer() {
        // addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        // addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
    }

}
