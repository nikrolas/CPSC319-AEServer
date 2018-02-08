package com.discovery.channel;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class Application extends ResourceConfig {
    public Application() {
        // Define the package which contains the service classes.
        packages("com.discovery.channel.resource");
    }
}