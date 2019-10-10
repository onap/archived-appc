package org.onap.appc.services.dmaapService;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import org.onap.appc.listener.AppcEventListenerActivator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App 
{
    public static void main( String[] args )
    {
        
        System.out.println("Starting");
        AppcEventListenerActivator event = new AppcEventListenerActivator();
        try {
            event.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        SpringApplication.run(App.class, args);
    }
    

}
