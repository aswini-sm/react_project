package com.example.demo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FirestoreService {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreService.class);

    /**
     * Gets the Firestore database instance.
     * Accessible across the application for direct queries.
     *
     * @return Firestore instance
     */
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Helper method to insert data into a specific collection and document.
     *
     * @param collectionName Name of the Firestore collection
     * @param documentId     Name of the Firestore document
     * @param data           Map containing the data to insert
     * @return A string with the update time, or an error message
     * @throws Exception If an error occurs during the writing process
     */
    public String insertData(String collectionName, String documentId, Map<String, Object> data) throws Exception {
        Firestore db = getFirestore();
        logger.info("Connecting to Firestore to insert data into collection: '{}' at document: '{}'", collectionName,
                documentId);

        // Asynchronously write data to Firestore
        ApiFuture<WriteResult> collectionsApiFuture = db.collection(collectionName).document(documentId).set(data);

        // future.get() blocks on response
        String updateTime = collectionsApiFuture.get().getUpdateTime().toString();

        logger.info("Successfully inserted data. Update time: {}", updateTime);
        return updateTime;
    }
}
