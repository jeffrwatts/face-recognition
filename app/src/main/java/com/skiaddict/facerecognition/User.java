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

    public static User fromFileRow (String[] rowData) {
        String name = rowData[0];
        float [] floats = new float[Embedding.EMBEDDING_LENGTH];

        for (int ix = 1 ; ix < rowData.length; ix++) {
            floats[ix-1] = Float.valueOf(rowData[ix]);
        }

        User user = new User(rowData[0], new Embedding(floats));

        return user;
    }

    public String toFileRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        for (float floatIx : embedding.values) {
            sb.append(",");
            sb.append(floatIx);
        }
        return sb.toString();
    }
}
