package com.adham_omran;

import javafx.scene.image.Image;

// Class to imitate the Topic from SuperMemo

// aFactor is Absolute Factor
// https://super-memory.com/archive/help16/g.htm#A-Factor

public class Topic {
    private double aFactor;
    private double priority;
    private String content;
    // The `img` is JavaFX since I want to use it to load it into an
    // ImageView. We can have helper methods to convert later.
    private Image topicImage;

}
