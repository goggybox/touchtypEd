package com.example.touchtyped.firestore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirestoreClient {

    private static Firestore db;

    public static Firestore getFirestore() throws IOException {
        if (db == null) {
            // load credentials from JSON file
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/com/example/touchtyped/firestore/privatekey.json"));

            FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();

            db = options.getService();
        }
        return db;
    }
}
