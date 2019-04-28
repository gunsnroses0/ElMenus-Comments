package Model;

import java.util.ArrayList;
import java.util.HashMap;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Commands.Command;

public class Comment {

	private static final String COLLECTION_NAME = "comments";
	static String host = System.getenv("MONGO_URI");


	private static int DbPoolCount = 4;
	public static int getDbPoolCount() {
		return DbPoolCount;
	}
	public static void setDbPoolCount(int dbPoolCount) {
		DbPoolCount = dbPoolCount;
	}
	
	static MongoClientOptions.Builder options = null;
	static MongoClientURI uri = null;
	static MongoClient mongoClient = null; 
	
	public static void initializeDb() {
		options = MongoClientOptions.builder()
				.connectionsPerHost(DbPoolCount);
		uri = new MongoClientURI(
				host,options);
		mongoClient = new MongoClient(uri);
			
	}
	public static HashMap<String, Object> create(HashMap<String, Object> attributes, String target_id) throws ParseException {

		MongoDatabase database = mongoClient.getDatabase("El-Menus");

		// Retrieving a collection
		MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
		Document newComment = new Document();

		for (String key : attributes.keySet()) {
			newComment.append(key, attributes.get(key));
		}

		newComment.append("target_id", new ObjectId(target_id));
		
		collection.insertOne(newComment);
		JSONParser parser = new JSONParser();

		HashMap<String, Object> returnValue = Command.jsonToMap((JSONObject) parser.parse(newComment.toJson()));

		
		return returnValue;
	}


	public static ArrayList<HashMap<String, Object>> get(String commentId, String type) {
		
		MongoDatabase database = mongoClient.getDatabase("El-Menus");

		// Retrieving a collection
		MongoCollection<Document> collection = database.getCollection("comments");
		BasicDBObject query = new BasicDBObject();
		query.put("target_id", new ObjectId(commentId));

		FindIterable<Document> docs = collection.find(query);
		JSONParser parser = new JSONParser(); 
		ArrayList<HashMap<String, Object>> comments = new ArrayList<HashMap<String, Object>>();

		for (Document document : docs) {
			JSONObject json;
			try {
				json = (JSONObject) parser.parse(document.toJson());
				HashMap<String, Object> comment = Command.jsonToMap(json);	
				comments.add(comment);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        return comments;
		
	}
}
