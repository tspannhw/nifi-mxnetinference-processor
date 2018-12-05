package com.dataflowdeveloper.mxnet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

import javax.imageio.ImageIO;

@EventDriven
@SupportsBatching
@SideEffectFree
@Tags({ "mxnet", "inference", "computer vision", "image", "ssd", "object detection", "Apache MXNet", "deep learning" })
@CapabilityDescription("Run Apache MXNet Object Detection / SSD")
@SeeAlso({})
@WritesAttributes({ @WritesAttribute(attribute = "label", description = "The x, y, probabilities and labels") })
/**
 *
 * @author tspann  Timothy Spann
 *
 */
public class InferenceProcessor extends AbstractProcessor {

    public static final String MODEL_DIR_NAME = "modeldir";

    public static final PropertyDescriptor MODEL_DIR = new PropertyDescriptor.Builder().name(MODEL_DIR_NAME)
            .description("Model Directory").required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("Successfully determined image.").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("Failed to determine image.").build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    private SSDClassifierService service;

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
        service = new SSDClassifierService();
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
                modelDir = "data/models/resnet50_ssd/resnet50_ssd_model";
            }
            final String model = modelDir;

            try {
                final HashMap<String, String> attributes = new HashMap<String, String>();

                flowFile = session.write(flowFile, new StreamCallback() {
                    @Override
                    public void process(final InputStream input, final java.io.OutputStream out) throws IOException {

                        byte[] byteArray = IOUtils.toByteArray(input);
                        getLogger().debug(
                                String.format("read %d bytes from incoming file", new Object[] { byteArray.length }));

                        List<Result> results = service.ssdClassify(model, byteArray);

                        if (results != null) {
                            getLogger().debug(String.format("Found %d results", new Object[] { results.size() }));

                            int i = 1;
                            InputStream in = new ByteArrayInputStream(byteArray);
                            BufferedImage img = null;

                            try {
                                img = ImageIO.read(in);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (Result result : results) {
                                attributes.put(String.format("label_%d", i), result.getLabel() );
                                attributes.put(String.format("probability_%d",i), String.format("%.2f", result.getProbability()));

                                if ( result.getXmin() > 0) {
                                    attributes.put(String.format("xmin_%d", i), String.format("%.2f", result.getXmin()));
                                    attributes.put(String.format("xmax_%d", i), String.format("%.2f", result.getXmax()));
                                    attributes.put(String.format("ymin_%d", i), String.format("%.2f", result.getYmin()));
                                    attributes.put(String.format("ymax_%d", i), String.format("%.2f", result.getYmax()));
                                    attributes.put(String.format("height_%d", i), String.format("%d", result.getHeight()));
                                    attributes.put(String.format("width_%d", i), String.format("%d", result.getWidth()));
                                }

                                Graphics2D g2d = (Graphics2D) img.createGraphics();

                                try {
                                    // Draw on the buffered image
                                    g2d.setStroke(new BasicStroke(3));
                                    g2d.setColor(java.awt.Color.black);
                                    g2d.drawRect(Math.round(result.getXmax()), Math.round(result.getYmax()),
                                            Math.round(result.getWidth()),
                                            Math.round(result.getHeight()));
                                    g2d.dispose();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                finally
                                {
                                    g2d.dispose();
                                }



                                i++;
                            }
                            ImageIO.write(img, "jpg", out);
                        }
                    }
                });
                System.out.println("Attributessize:" + attributes.size());
                if (attributes.size() == 0) {
                    session.transfer(flowFile, REL_FAILURE);
                } else {
                    flowFile = session.putAllAttributes(flowFile, attributes);

                    /// Add a new changed image with boxes
                    session.transfer(flowFile, REL_SUCCESS);
                }
            } catch (Exception e) {
                throw new ProcessException(e);
            }

            session.commit();
        } catch (final Throwable t) {
            getLogger().error("Unable to process Apache MXNet Processor file " + t.getLocalizedMessage());
            throw new ProcessException(t);
        }
    }
}