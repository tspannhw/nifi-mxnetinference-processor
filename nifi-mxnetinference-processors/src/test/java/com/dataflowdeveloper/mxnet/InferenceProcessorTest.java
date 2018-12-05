package com.dataflowdeveloper.mxnet;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import java.nio.file.Paths;

/**
 *
 */
public class InferenceProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(InferenceProcessor.class);
    }

    private String pathOfResource(String name) throws URISyntaxException {
        URL r = this.getClass().getClassLoader().getResource(name);
        URI uri = r.toURI();
        return Paths.get(uri).toAbsolutePath().getParent().toString();
    }

    private void runAndAssertHappy() {
        testRunner.setValidateExpressionUsage(false);
        testRunner.run();
        testRunner.assertValid();
        testRunner.assertAllFlowFilesTransferred(InferenceProcessor.REL_SUCCESS);
        List<MockFlowFile> successFiles = testRunner.getFlowFilesForRelationship(InferenceProcessor.REL_SUCCESS);

        for (MockFlowFile mockFile : successFiles) {
            assertEquals("person", mockFile.getAttribute("label_1"));
            assertEquals("0.59", mockFile.getAttribute("probability_1"));

            System.out.println("Size:" +             mockFile.getSize() ) ;
			Map<String, String> attributes =  mockFile.getAttributes();

			 for (String attribute : attributes.keySet()) {
				 System.out.println("Attribute:" + attribute + " = " + mockFile.getAttribute(attribute));
			 }
        }

    }

    @Test
    public void testProcessor() throws Exception {

        java.io.File resourcesDirectory = new java.io.File("src/test/resources");
        System.out.println(resourcesDirectory.getAbsolutePath());

        testRunner.setProperty(InferenceProcessor.MODEL_DIR, resourcesDirectory.getAbsolutePath() + "/" + "resnet50_ssd_model");
        testRunner.enqueue(this.getClass().getClassLoader().getResourceAsStream("montreal.jpg"));

        runAndAssertHappy();
    }



}