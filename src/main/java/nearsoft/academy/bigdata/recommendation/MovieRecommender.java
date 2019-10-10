package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class MovieRecommender {
  private int totalReviews = 0;
  private int totalProducts = 0;
  private long totalUsers = 0;
  private UserBasedRecommender recommender;
  private HashMap<Integer, String> invertedProductsMap = new HashMap<>();
  private HashMap<String, Long> usersMap = new HashMap<>();
  private HashMap<String, Integer> productsMap = new HashMap<>();
  private String productPortion = "";
  private String userPortion = "";
  /**
   * <p>
   *   Constructor that builds .csv file from amazon recommendations .txt file and
   *   creates object "recommender" that can be used later by class's methods.
   * </p>
   * @param path path where .txt file is located.
   * @since 1.0
   * */
  
  public MovieRecommender(String path) throws IOException, TasteException {
    // Call method to generate .csv file
    csvBuilder(path);
    
    // Build Recommender
    buildRecommender();
  }
  
  /**
   * <p> Creates a GenericUserBasedRecommender from a .csv file in root folder.</p>
   * @throws IOException FileDataModel handles IOExceptions
   * @throws TasteException PearsonCorrelationSimilarity handles TasteExceptions
   * @since 1.0
   * */
  
  private void buildRecommender() throws IOException, TasteException {
    DataModel model;
    UserSimilarity similarity;
    UserNeighborhood neighborhood;
    // Data Model:
    model = new FileDataModel(new File("data.csv"));
    //Threshold Similarity:
    similarity = new PearsonCorrelationSimilarity(model);
    neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
    // Create Recommender
    this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    
  }
  
  /**
   * <p> Generates .csv file in root folder from a .txt file.</p>
   * @param path path of the .txt file with amazon reviews.
   * @throws IOException recommender handles IOExceptions.
   * @since 1.0
   * */
  
  private void csvBuilder(String path) throws IOException {
    File file = new File(path);
    BufferedReader br = new BufferedReader(new FileReader(file));
    FileWriter writer = new FileWriter("data.csv");
    BufferedWriter bw = new BufferedWriter(writer);
    // Iterate through .txt to create each line of the new .csv file
    for (String line = br.readLine(); line != null; line = br.readLine()) {
      
      if (line.startsWith("review/score")) {
        String reviewScore = line.split(" ")[1];
        bw.write(this.userPortion + this.productPortion + reviewScore + "\n");
      } else {
        // Call method to check for the remaining components of .csv line
        buildCsvLine(line);
      }
      
    }
    
    bw.close();
  }
  
  /**
   * <p>Inserts values in "productPortion" and "userPortion" for new .csv line.</p>
   * @param line current line in .txt file.
   * @since 1.0
   * */
  
  private void buildCsvLine(String line) {
    if (line.startsWith("product/productId")) {
      // Read new product. Create new csv line
      String productId = line.split(" ")[1];
      this.totalReviews++;
    
      if (this.productsMap.containsKey(productId)) {
        // Use the key in hash map
        this.productPortion = this.productsMap.get(productId) + ",";
      } else {
        // Add new key to hash maps
        this.productsMap.put(productId, this.totalProducts);
        this.invertedProductsMap.put(this.totalProducts, productId);
        this.productPortion = this.totalProducts + ",";
        this.totalProducts++;
      }
    } else if (line.startsWith("review/userId")) {
      String userId = line.split(" ")[1];
    
      if (this.usersMap.containsKey(userId)) {
        // Use the key in hash map
        this.userPortion = this.usersMap.get(userId) + ",";
      } else {
        // Add new key to hash map
        this.usersMap.put(userId, this.totalUsers);
        userPortion = this.totalUsers + ",";
        this.totalUsers++;
      }
    }
  }
  
  
  /**
   * <p>Returns 3 product recommendations for an specific user.</p>
   * @param userId user that we are recommending 3 products.
   * @return List containing Strings of product ids.
   * @throws TasteException recommender handles TasteExceptions
   * @since 1.0
   * */
  
  List<String> getRecommendationsForUser(String userId) throws TasteException {
    List<String> results = new ArrayList<>();
    long id = usersMap.get(userId);
    List<RecommendedItem> recommendations = recommender.recommend(id, 3);
    
    for (RecommendedItem recommendedItem : recommendations) {
      // Retrieve the original product id form our hash map and add it to results
      results.add(invertedProductsMap.get((int) recommendedItem.getItemID()));
    }
    return results;
  }
  
  int getTotalReviews() {
    return this.totalReviews;
  }
  
  int getTotalProducts() {
    return this.totalProducts;
  }

  long getTotalUsers() {
    return this.totalUsers;
  }
  
}
