package com.adham_omran;

public class RectangleData {
    private int itemId;
    private int pdfPage;
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public RectangleData(int itemId, int pdfPage, double x1, double y1, double x2, double y2) {
        this.itemId = itemId;
        this.pdfPage = pdfPage;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getPdfPage() {
        return pdfPage;
    }

    public void setPdfPage(int pdfPage) {
        this.pdfPage = pdfPage;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getWidth() {
        return Math.abs(x2 - x1);
    }

    public double getHeight() {
        return Math.abs(y2 - y1);
    }

    public double getMinX() {
        return Math.min(x1, x2);
    }

    public double getMinY() {
        return Math.min(y1, y2);
    }

    @Override
    public String toString() {
        return String.format("Rectangle[item=%d, page=%d, (%.3f,%.3f)-(%.3f,%.3f)]", 
                           itemId, pdfPage, x1, y1, x2, y2);
    }
}