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
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    public int                  totalReviews;
    public int                  totalProducts;
    public long                 totalUsers;
    public UserBasedRecommender recommender;
    HashMap<String, Integer>    productsMap         = new HashMap();
    HashMap<Integer, String>    invertedProductsMap = new HashMap();
    HashMap<String, Long>       usersMap            = new HashMap();

//    public static void main(String[] args) throws TasteException {
//        MovieRecommender recommender = new MovieRecommender("/Users/jcordoba/Documents/test/movies/big-data-exercises/src/test/java/nearsoft/academy/bigdata/recommendation/movies.txt");
//        List<String> recommendations = recommender.getRecommendationsForUser("A141HP4LYPWMSR");
//
//    }

    public MovieRecommender(String path){
        DataModel           model;
        UserSimilarity      similarity;
        UserNeighborhood    neighborhood;

        // Buffer file
        try{
//            Using Gzip:

//            InputStream fileStream = new FileInputStream(path);
//            InputStream gzipStream = new GZIPInputStream(fileStream);
//            Reader decoder = new InputStreamReader(gzipStream, "US-ASCII");
//            BufferedReader br = new BufferedReader(decoder);

//            Reading txt file
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

        try {
            model = new FileDataModel(new File("data.csv"));

            // Create similarity
            try{
                similarity = new PearsonCorrelationSimilarity(model);
                //Threshold Similarity:
                neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

                // Create Recomender
                try{
                    this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);


                } catch (Exception e){
                    System.out.println("Exception found while creating recommender: " + e.getMessage());
                }
            } catch (Exception e){
                System.out.println("Exception found while creating similarity or neighborhood: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Exception found while creating model: " + e.getMessage());
        }





    }

    public int getTotalReviews(){
        return this.totalReviews;
    }

    public int getTotalProducts(){
        return this.totalProducts;
    }

    public long getTotalUsers(){
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws TasteException {

        // List with recommended results
        List<String> results = new ArrayList<String>();

        /*
         *   PARAMS recommend(l, i):
         *
         *   l => userID
         *   i => amount of recommended items we want to be returned
         */
//        long id = Long.parseLong(this.usersMap.get(userID).toString());
        long id = usersMap.get(userID);
        List<RecommendedItem> recommendations = recommender.recommend(id, 3);
        for (RecommendedItem r : recommendations) {

            results.add(invertedProductsMap.get((int)r.getItemID()));
        }

        return results;

    }
}
