package LibraryBot_v1;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;

public class Library {
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    //connects to local mongodb
    public Library() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.database = mongoClient.getDatabase("LibraryDB");
        this.collection = database.getCollection("books");
    }


    public List<Document> getAllBooks() {
        return collection.find().into(new ArrayList<>());
    }

    public List<Document> findByAuthor(String author) {
        return collection.find(new Document("author", author)).into(new ArrayList<>());
    }

    public List<Document> findByTitle(String title) {
        return collection.find(new Document("title", title)).into(new ArrayList<>());
    }

    public List<Document> findByHolder(String name) {
        name = "held by " + name;
        return collection.find(new Document("holder", name)).into(new ArrayList<>());
    }

    public void updateHolder(String name, Document book){
        Document upd = new Document("holder", name);
        collection.updateOne(book, new Document("$set", upd));
    }

    //adds a new book to the database
    public boolean addBook(String title, String author) {
        if (Objects.equals(title, "") || Objects.equals(author, "")) {
            return false;
        }
        Document book = new Document("title", title).append("author", author).append("holder", "at library");
        collection.insertOne(book);
        return true;
    }


}
