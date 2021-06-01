package ca.uhn.fhir.jpa.starter.interceptors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import ca.uhn.fhir.context.ConfigurationException;

/**
 * A class for loading a static resource from a file
 */
public class ResourceLoader {
    /**
     * Loads the resource from a static file as a string
     * @param  resource the resource to load
     * @return          file loaded from the resource containing the list of file path
     */
    public static String loadResource(String resource) {
        try {
          InputStream in = ResourceLoader.class.getClassLoader().getResourceAsStream(resource);
          String text = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
          return text;
        } catch (Exception e) {
            throw new ConfigurationException("Could not load " + resource, e);
        }
    }
}
