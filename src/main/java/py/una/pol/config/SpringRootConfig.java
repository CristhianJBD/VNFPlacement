package py.una.pol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import py.una.pol.service.VnfService;

@Configuration
@ComponentScan({ "py.una.pol.service", "py.una.pol.util" })
public class SpringRootConfig {

    @Autowired
    private VnfService vnf;

    @Bean(name = "NFV")
    public boolean NFV() {
        return vnf.placement();
    }

}
