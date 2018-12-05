package com.dataflowdeveloper.mxnet;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * Class: dog
 * Probabilties: 0.82268184
 * Coord:83.82353, 179.13997, 206.63783, 476.78754
 * i.getXMin() * width,
 *                             i.getXMax() * height, i.getYMin() * width, i.getYMax() * height
 * @author tspann
 *
 */
public class Result implements Serializable {

    // per
    private String label;
    private float probability;
    private int rank;
    private float xmin;
    private float xmax;
    private float ymin;
    private float ymax;
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Result() {
        super();
    }

    public Result(String label, float probability, int rank, float xmin, float xmax, float ymin, float ymax, int width, int height) {
        this.label = label;
        this.probability = probability;
        this.rank = rank;
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.width = width;
        this.height = height;
    }

    /**
     *
     * @param label
     * @param probability
     * @param rank
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     */
    public Result(String label, float probability, int rank, float xmin, float xmax, float ymin, float ymax) {
        super();
        this.label = label;
        this.probability = probability;
        this.rank = rank;
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Result{");
        sb.append("label='").append(label).append('\'');
        sb.append(", probability=").append(probability);
        sb.append(", rank=").append(rank);
        sb.append(", xmin=").append(xmin);
        sb.append(", xmax=").append(xmax);
        sb.append(", ymin=").append(ymin);
        sb.append(", ymax=").append(ymax);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append('}');
        return sb.toString();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getXmin() {
        return xmin;
    }

    public void setXmin(float xmin) {
        this.xmin = xmin;
    }

    public float getXmax() {
        return xmax;
    }

    public void setXmax(float xmax) {
        this.xmax = xmax;
    }

    public float getYmin() {
        return ymin;
    }

    public void setYmin(float ymin) {
        this.ymin = ymin;
    }

    public float getYmax() {
        return ymax;
    }

    public void setYmax(float ymax) {
        this.ymax = ymax;
    }
}