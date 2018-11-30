package com.dataflowdeveloper.mxnet;

import java.io.IOException;
import java.io.InputStream;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

@EventDriven
@SupportsBatching
@SideEffectFree
@Tags({ "mxnet", "inference", "computer vision", "image" })
@CapabilityDescription("Run Apache MXNet Image Recognition")
@SeeAlso({})
@WritesAttributes({ @WritesAttribute(attribute = "probilities", description = "The probabilites and labels") })
/**
 *
 * @author tspann  Timothy Spann
 *
 */
public class InferenceProcessor extends AbstractProcessor {

    public static final String ATTRIBUTE_OUTPUT_NAME = "tf.probabilities";
    public static final String MODEL_DIR_NAME = "modeldir";
    public static final String PROPERTY_NAME_EXTRA = "Extra Resources";

    public static final PropertyDescriptor MODEL_DIR = new PropertyDescriptor.Builder().name(MODEL_DIR_NAME)
            .description("Model Directory").required(true).expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("Successfully determined image.").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("Failed to determine image.").build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    // private TensorFlowService service;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(MODEL_DIR);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        //service = new TensorFlowService();
        return;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            flowFile = session.create();
        }
        try {
            flowFile.getAttributes();

            // read all bytes of the flowfile (tensor requires whole image)
            String modelDir = flowFile.getAttribute(MODEL_DIR_NAME);
            if (modelDir == null) {
                modelDir = context.getProperty(MODEL_DIR_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            if (modelDir == null) {
                modelDir = "/models";
            }
            final String model = modelDir;

            try {
                final HashMap<String, String> attributes = new HashMap<String, String>();

                session.read(flowFile, new InputStreamCallback() {
                    @Override
                    public void process(InputStream input) throws IOException {
                        byte[] byteArray = IOUtils.toByteArray(input);
                        getLogger().debug(
                                String.format("read %d bytes from incoming file", new Object[] { byteArray.length }));
//                        List<InceptionResult> results = service.getInception(byteArray, model);
//
//                        if (results != null) {
//                            getLogger().debug(String.format("Found %d results", new Object[] { results.size() }));
//
//                            for (InceptionResult inceptionResult : results) {
//                                attributes.put(String.format("label_%d", inceptionResult.getDisplayRank()), inceptionResult.getLabel() );
//                                attributes.put(String.format("probability_%d", inceptionResult.getDisplayRank()), inceptionResult.getProbability());
//                            }
//                        }
                    }
                });
                if (attributes.size() == 0) {
                    session.transfer(flowFile, REL_FAILURE);
                } else {
                    flowFile = session.putAllAttributes(flowFile, attributes);
                    session.transfer(flowFile, REL_SUCCESS);
                }
            } catch (Exception e) {
                throw new ProcessException(e);
            }

            session.commit();
        } catch (

                final Throwable t) {
            getLogger().error("Unable to process Apache MXNet Processor file " + t.getLocalizedMessage());
            throw new ProcessException(t);
        }
    }
}