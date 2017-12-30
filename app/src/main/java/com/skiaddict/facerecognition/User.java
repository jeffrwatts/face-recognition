package com.skiaddict.facerecognition;

/**
 * Created by jewatts on 12/29/17.
 */

public class User {
    public String name;
    public Embedding embedding;

    public User (String name, Embedding embedding) {
        this.name = name;
        this.embedding = embedding;
    }
}
