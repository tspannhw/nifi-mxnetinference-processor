package com.dataflowdeveloper.mxnet;

import org.apache.mxnet.infer.javaapi.ObjectDetectorOutput;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.mxnet.javaapi.*;
import org.apache.mxnet.infer.javaapi.ObjectDetector;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

// scalastyle:off
import java.awt.image.BufferedImage;
// scalastyle:on

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.File;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

/**
 * https://mxnet.incubator.apache.org/tutorials/java/ssd_inference.html
 * <p>
 * From MXNet Example
 * https://github.com/apache/incubator-mxnet/blob/master/scala-package/examples/src/main/java/org/apache/mxnetexamples/javaapi/infer/objectdetector/SSDClassifierExample.java
 * https://github.com/apache/incubator-mxnet/tree/master/scala-package/examples/src/main/java/org/apache/mxnetexamples/javaapi/infer/objectdetector
 * see scala ./get_ssd_data.sh
 * https://github.com/apache/incubator-mxnet/tree/master/scala-package/examples/scripts/infer/objectdetector
 * <p>
 * Build annotated image https://www.baeldung.com/java-images
 * https://imagej.nih.gov/ij/index.html
 * <dependency>
 * <groupId>net.imagej</groupId>
 * <artifactId>ij</artifactId>
 * <version>1.51h</version>
 * </dependency>
 * <p>
 * http://openimaj.org/
 * <p>
 * https://commons.apache.org/proper/commons-imaging/
 */
public class SSDClassifierService {

    /**
     * input model directory and prefix of the model
     **/
    private String modelPathPrefix = "/Volumes/TSPANN/projects/nifi-mxnetinference-processor/data/models/resnet50_ssd/resnet50_ssd_model";
    /**
     * the input image
     **/
    private String inputImagePath = "/Volumes/TSPANN/projects/nifi-mxnetinference-processor/data/images/dog.jpg";
    /**
     * the input batch of images directory
     **/
    private String inputImageDir = "/Volumes/TSPANN/projects/nifi-mxnetinference-processor/data/images/";

    /**
     * logging
     **/
    private final static Logger logger = LoggerFactory.getLogger(SSDClassifierService.class);

    private Map<Path, byte[]> modelCache = new HashMap<Path, byte[]>();
    private Map<Path, List<String>> labelCache = new HashMap<Path, List<String>>();

    /**
     * cache labels
     *
     * @param path
     * @return
     */
    private List<String> getOrCreateLabels(Path path) {
        if (labelCache.containsKey(path)) {
            return labelCache.get(path);
        }
        labelCache.put(path, readAllLinesOrExit(path));
        return labelCache.get(path);
    }

    /**
     * cache path bytes
     *
     * @param path
     * @return
     */
    private byte[] getOrCreate(Path path) {
        if (modelCache.containsKey(path)) {
            return modelCache.get(path);
        }
        byte[] graphDef = readAllBytesOrExit(path);
        modelCache.put(path, graphDef);
        return modelCache.get(path);
    }

    private static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private static List<String> readAllLinesOrExit(Path path) {
        try {
            return Files.readAllLines(path, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    /**
     * run object detection single
     * String inputImagePath
     * @param modelPathPrefix
     * @param imageBytes
     * @param context
     * @return Object Data
     */
    static List<List<ObjectDetectorOutput>>
    runObjectDetectionSingle(String modelPathPrefix, byte[] imageBytes, List<Context> context) {
        Shape inputShape = new Shape(new int[]{1, 3, 512, 512});
        List<DataDesc> inputDescriptors = new ArrayList<DataDesc>();
        inputDescriptors.add(new DataDesc("data", inputShape, DType.Float32(), "NCHW"));

        // image from file
        //BufferedImage img = ObjectDetector.loadImageFromFile(inputImagePath);
        InputStream in = new ByteArrayInputStream(imageBytes);
        BufferedImage img = null;

        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( img != null ) {
            ObjectDetector objDet = new ObjectDetector(modelPathPrefix, inputDescriptors, context, 0);
            return objDet.imageObjectDetect(img, 3);
        }
        else {
            return null;
        }
    }



    /**
     * ssd classify with MXNet
     * @param modelPath   path to models
     * @param imageBytes  bytes
     * @return List of Result for class and probabilities
     */
    public List<Result> ssdClassify(String modelPath, byte[] imageBytes) {
        if ( modelPath == null || imageBytes == null ) {
            return new ArrayList<Result>();
        }

//        SSDClassifierService inst = new SSDClassifierService();
        List<Result> results = new ArrayList<Result>();
        List<Context> context = new ArrayList<Context>();

        if (System.getenv().containsKey("SCALA_TEST_ON_GPU") &&
                Integer.valueOf(System.getenv("SCALA_TEST_ON_GPU")) == 1) {
            context.add(Context.gpu());
        } else {
            context.add(Context.cpu());
        }

        try {
            Shape inputShape = new Shape(new int[]{1, 3, 512, 512});
            Shape outputShape = new Shape(new int[]{1, 6132, 6});

            int width = inputShape.get(2);
            int height = inputShape.get(3);

            List<List<ObjectDetectorOutput>> output
                    = runObjectDetectionSingle(modelPath, imageBytes, context);

            int rank = 1;
            if ( output != null ) {
                for (List<ObjectDetectorOutput> ele : output) {
                    for (ObjectDetectorOutput i : ele) {
                        if ( i != null ) {
                            Result result = new Result();

                            result.setLabel(i.getClassName());
                            result.setProbability(i.getProbability());
                            result.setXmin(i.getXMin() * width);
                            result.setXmax(i.getXMax() * height);
                            result.setYmin(i.getYMin() * width);
                            result.setYmax(i.getYMax() * height);
                            result.setRank(rank);

                            results.add(result);
                            rank++;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return results;
    }
}