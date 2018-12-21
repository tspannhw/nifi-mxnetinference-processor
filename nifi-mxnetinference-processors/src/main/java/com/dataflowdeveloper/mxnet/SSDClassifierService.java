package com.dataflowdeveloper.mxnet;


import org.apache.mxnet.infer.javaapi.ObjectDetector;
import org.apache.mxnet.infer.javaapi.ObjectDetectorOutput;
import org.apache.mxnet.javaapi.Context;
import org.apache.mxnet.javaapi.DType;
import org.apache.mxnet.javaapi.DataDesc;
import org.apache.mxnet.javaapi.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// scalastyle:off
// scalastyle:on

/**
 * https://mxnet.incubator.apache.org/tutorials/java/ssd_inference.html
 * <p>
 * From MXNet Example
 * https://github.com/apache/incubator-mxnet/blob/master/scala-package/examples/src/main/java/org/apache/mxnetexamples/javaapi/infer/objectdetector/SSDClassifierExample.java
 * https://github.com/apache/incubator-mxnet/tree/master/scala-package/examples/src/main/java/org/apache/mxnetexamples/javaapi/infer/objectdetector
 * see scala ./get_ssd_data.sh
 * https://github.com/apache/incubator-mxnet/tree/master/scala-package/examples/scripts/infer/objectdetector
 * </p>
 */
public class SSDClassifierService {

    /**
     * logging
     **/
    private final static Logger logger = LoggerFactory.getLogger(SSDClassifierService.class);

    /**
     * See:   https://mxnet.incubator.apache.org/tutorials/java/ssd_inference.html
     * run object detection single
     * String inputImagePath
     * @param modelPathPrefix
     * @param imageBytes
     * @param context
     * @return Object Data
     */
    public List<List<ObjectDetectorOutput>>
    runObjectDetectionSingle(String modelPathPrefix, byte[] imageBytes, List<Context> context) {
        Shape inputShape = new Shape(new int[]{1, 3, 512, 512});
        List<DataDesc> inputDescriptors = new ArrayList<DataDesc>();
        inputDescriptors.add(new DataDesc("data", inputShape, DType.Float32(), "NCHW"));

        InputStream in = new ByteArrayInputStream(imageBytes);
        BufferedImage img = null;

        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( img != null ) {
            ObjectDetector objDet = new ObjectDetector(modelPathPrefix, inputDescriptors, context, 0);
            return objDet.imageObjectDetect(img, 5);
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

        List<Result> results = new ArrayList<Result>();
        List<Context> context = new ArrayList<Context>();

        if (System.getenv().containsKey("SCALA_TEST_ON_GPU") &&
                Integer.valueOf(System.getenv("SCALA_TEST_ON_GPU")) == 1) {
            context.add(Context.gpu());
        } else {
            context.add(Context.cpu());
        }

        try {
            // TODO:  make shape an input attribute
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
                            result.setXmin(i.getXMin() * width ); //
                            result.setXmax(i.getXMax() * width ); //
                            result.setYmin(i.getYMin() * height ); //
                            result.setYmax(i.getYMax() * height ); //
                            result.setWidth(width);
                            result.setHeight(height);
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