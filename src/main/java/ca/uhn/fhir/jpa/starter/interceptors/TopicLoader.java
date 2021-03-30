package ca.uhn.fhir.jpa.starter.interceptors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import ca.uhn.fhir.context.ConfigurationException;

public class TopicLoader {
    public static final String TOPIC_LIST = "topic-list.json";
    /**
     *
     * @return json loaded from the topic-list.json containing the list of file url for bulk topic-list.
     */
    public static String loadTopics() {
        try {
          InputStream in = TopicLoader.class.getClassLoader().getResourceAsStream(TOPIC_LIST);
          String text = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
          return text;
        } catch (Exception e) {
            throw new ConfigurationException("Could not load topic-list json", e);
        }
    }
}
