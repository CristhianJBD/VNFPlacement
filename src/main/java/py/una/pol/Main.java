package py.una.pol;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import py.una.pol.config.SpringRootConfig;


public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(SpringRootConfig.class);
        context.getBean("NFV");
    }

}
