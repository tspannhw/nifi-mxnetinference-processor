package com.dataflowdeveloper.mxnet;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

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
//            assertEquals("giant panda", mockFile.getAttribute("label_1"));
//            assertEquals("95.23%", mockFile.getAttribute("probability_1"));

			Map<String, String> attributes =  mockFile.getAttributes();

			 for (String attribute : attributes.keySet()) {
				 System.out.println("Attribute:" + attribute + " = " + mockFile.getAttribute(attribute));
			 }
        }

    }

    @Test
    public void testProcessor() throws Exception {
        testRunner.setProperty(InferenceProcessor.MODEL_DIR, "/Volumes/TSPANN/projects/nifi-mxnetinference-processor/data/models/resnet50_ssd/resnet50_ssd_model");
        testRunner.enqueue(this.getClass().getClassLoader().getResourceAsStream("dog.jpg"));

        runAndAssertHappy();
    }



}