package pluralsight.m3.controller;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Foo {

    @Test
    public void should() throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "ls someDirectory; touch hacked.txt"});

        // Create a BufferedReader to read the subprocess's output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            // Output each line of the subprocess's output
            System.out.println(line);
        }

        // Close the reader
        reader.close();

    }

}
