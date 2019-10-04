package nearsoft.academy.bigdata.recommendation;


import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MovieRecommender {
    public int                  totalReviews;
    public int                  totalProducts;
    public long                 totalUsers;
    public UserBasedRecommender recommender;
    HashMap<String, Integer>    productsMap         = new HashMap();
    HashMap<Integer, String>    invertedProductsMap = new HashMap();
    HashMap<String, Long>       usersMap            = new HashMap();

    public MovieRecommender(String path){
        DataModel           model;
        UserSimilarity      similarity;
        UserNeighborhood    neighborhood;

        // Buffer file
        try {
            File            file    = new File(path);
            BufferedReader  br      = new BufferedReader(new FileReader(file));
            FileWriter      writer  = new FileWriter("data.csv");
            BufferedWriter  bw      = new BufferedWriter(writer);
            long    cantUsers       = 0;
            int     cantProducts    = 0;
            String  csvLine         = "";

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.startsWith("product/productId")) {
                    String productId = line.split(" ")[1];
                    this.totalReviews++;

                    if (this.productsMap.containsKey(productId)) {
                        csvLine = this.productsMap.get(productId) + ",";
                    }
                    else {
                        this.productsMap.put(productId, cantProducts);
                        this.invertedProductsMap.put(cantProducts,productId);
                        csvLine = cantProducts + ",";
                        cantProducts++;
                    }
                }
                else if (line.startsWith("review/userId")) {
                    String userId = line.split(" ")[1];

                    if (this.usersMap.containsKey(userId)) {
                        csvLine = this.usersMap.get(userId) + "," + csvLine;
                    }
                    else {
                        this.usersMap.put(userId, cantUsers);
                        csvLine = cantUsers + "," + csvLine;
                        cantUsers++;
                    }
                }
                else if (line.startsWith("review/score")) {
                    String reviewScore = line.split(" ")[1];
                    csvLine += reviewScore + "\n";
                    bw.write(csvLine);
                }
            }
            bw.close();
            this.totalUsers = cantUsers;
            this.totalProducts = cantProducts;
        } catch (Exception e) {
            System.out.println("Could not read file. Exception: " + e.getMessage());
        }

        // Build Recommender
        try {
            // Data Model:
            model = new FileDataModel(new File("data.csv"));
            try {
                //Threshold Similarity:
                similarity      = new PearsonCorrelationSimilarity(model);
                neighborhood    = new ThresholdUserNeighborhood(0.1, similarity, model);
                try {
                    // Create Recommender
                    this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

                } catch (Exception e) {
                    System.out.println("Exception found while creating recommender: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("Exception found while creating similarity or neighborhood: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Exception found while creating model: " + e.getMessage());
        }

    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts() {
        return this.totalProducts;
    }

    public long getTotalUsers() {
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws TasteException {

        // List to be filled with recommended results
        List<String> results = new ArrayList<>();

        /*
         *   PARAMS recommend(l, i):
         *
         *   l => userID
         *   i => amount of recommended items we want returned
         */
        long id = usersMap.get(userID);
        List<RecommendedItem> recommendations = recommender.recommend(id, 3);
        for (RecommendedItem r : recommendations) {

            results.add(invertedProductsMap.get((int)r.getItemID()));
        }

        return results;

    }
}
