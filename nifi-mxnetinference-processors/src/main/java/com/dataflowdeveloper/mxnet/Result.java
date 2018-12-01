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

    public Result() {
        super();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Float.compare(result.probability, probability) == 0 &&
                rank == result.rank &&
                Float.compare(result.xmin, xmin) == 0 &&
                Float.compare(result.xmax, xmax) == 0 &&
                Float.compare(result.ymin, ymin) == 0 &&
                Float.compare(result.ymax, ymax) == 0 &&
                Objects.equals(label, result.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, probability, rank, xmin, xmax, ymin, ymax);
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