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
	private HashMap<Integer, String> invertedProductsMap = new HashMap();
	private HashMap<String, Long> usersMap = new HashMap();

	public MovieRecommender(String path) {
		DataModel model;
		UserSimilarity similarity;
		UserNeighborhood neighborhood;

		// Buffer file
		try {
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));
			FileWriter writer = new FileWriter("data.csv");
			BufferedWriter bw = new BufferedWriter(writer);
			long cantUsers = 0;
			int cantProducts = 0;
			String csvLine = "";
			HashMap<String, Integer> productsMap = new HashMap();

			// Iterate through .txt to create each line of the new .csv file
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (line.startsWith("product/productId")) {
					// We read a new product so we create a new csv
					String productId = line.split(" ")[1];
					this.totalReviews++;

					if (productsMap.containsKey(productId)) {
						csvLine = productsMap.get(productId) + ",";
					} else {
						productsMap.put(productId, cantProducts);
						this.invertedProductsMap.put(cantProducts, productId);
						csvLine = cantProducts + ",";
						cantProducts++;
					}
				} else if (line.startsWith("review/userId")) {
					String userId = line.split(" ")[1];

					if (this.usersMap.containsKey(userId)) {
						csvLine = this.usersMap.get(userId) + "," + csvLine;
					} else {
						this.usersMap.put(userId, cantUsers);
						csvLine = cantUsers + "," + csvLine;
						cantUsers++;
					}
				} else if (line.startsWith("review/score")) {
					String reviewScore = line.split(" ")[1];
					csvLine += reviewScore + "\n";
					bw.write(csvLine);
				}
			}
			bw.close();
			this.totalUsers = cantUsers;
			this.totalProducts = cantProducts;
		} catch (IOException e) {
			System.out.println("Could not read file. Exception: " + e.getMessage());
			System.out.println("Stack Trace: " + e.getStackTrace().toString());
		}

		// Build Recommender
		try {
			// Data Model:
			model = new FileDataModel(new File("data.csv"));
			//Threshold Similarity:
			similarity = new PearsonCorrelationSimilarity(model);
			neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			// Create Recommender
			this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

		} catch (TasteException e) {
			System.out.println("Exception found while creating similarity or neighborhood: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Exception found while creating model: " + e.getMessage());
		}

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

	List<String> getRecommendationsForUser(String userId) throws TasteException {

		// List to be filled with recommended results
		List<String> results = new ArrayList<>();
		/*
		 *   PARAMS recommend(l, i):
		 *
		 *   l => userID
		 *   i => amount of recommended items we want returned
		 */
		long id = usersMap.get(userId);
		List<RecommendedItem> recommendations = recommender.recommend(id, 3);
		for (RecommendedItem r : recommendations) {
			results.add(invertedProductsMap.get((int) r.getItemID()));
		}
		return results;
	}
}
