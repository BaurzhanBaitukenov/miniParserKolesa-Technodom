package com.example.paeser;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Main {
    public static void main(String[] args) throws Exception {
        // TECHNODOM
//        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
//        MongoDatabase database = mongoClient.getDatabase("parser2");
//        MongoCollection<org.bson.Document> collection = database.getCollection("prices2");
//
//        int page = 2;
//        int itemsScraped = 0;
//
//        while (itemsScraped < 1000) {
//            String website = "https://www.technodom.kz/catalog/smartfony-i-gadzhety/smartfony-i-telefony/smartfony?page=" + page;
//            Document doc = Jsoup.connect(website).get();
//            Elements prices = doc.select(".ProductCardV__PaymentWrapper");
//            Elements titles = doc.select(".ProductCardV__TitleWrapper");
//
//            int itemsInThisPage = Math.min(prices.size(), 1000);
//
//            for (int i = 0; i < itemsInThisPage; i++) {
//                org.bson.Document document = new org.bson.Document("title", titles.get(i).text())
//                        .append("price", prices.get(i).text());
//                collection.insertOne(document);
//                itemsScraped++;
//
//                if (itemsScraped >= 1000) {
//                    break;
//                }
//            }
//            page++;
//        }


        // KOLESA
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/");
        MongoDatabase database = mongoClient.getDatabase("parser");
        MongoCollection<org.bson.Document> collection = database.getCollection("prices");

        int page = 2;
        int itemsScraped = 0;

        while (itemsScraped < 1000) {
            String website = "https://kolesa.kz/cars/?page=" + page;
            Document doc = Jsoup.connect(website).get();
            Elements prices = doc.select(".a-card__price");
            Elements titles = doc.select(".a-card__title");
            Elements year = doc.select(".a-card__description");
            Elements wearAndTear = doc.select(".a-card__description");
            Elements description = doc.select(".a-card__description");
            Elements volume = doc.select(".a-card__description");

            int itemsInThisPage = Math.min(prices.size(), 1000);

            for (int i = 0; i < itemsInThisPage; i++) {
                String tempYear = year.get(i).text();
                String yearSplit = tempYear.replaceAll("\\s+", "").split("\\.")[0];

                String tempWearAndTear = wearAndTear.get(i).text();
                String[] parts = tempWearAndTear.split(",");
                String carType = parts[1].trim();

                String tempVolume = volume.get(i).text();
                String[] partsVolume = tempVolume.split(",");
                String volumeRes = partsVolume[2].trim();

                String priceStr = prices.get(i).text();
                int priceInt = Integer.parseInt(priceStr.replaceAll("[^\\d.]", ""));

                int yearInt = Integer.parseInt(yearSplit.replaceAll("[^\\d.]", ""));

                org.bson.Document document = new org.bson.Document("title", titles.get(i).text())
                        .append("price", priceInt)
                        .append("year", yearInt)
                        .append("volume", volumeRes)
                        .append("car type", carType)
                        .append("description", description.get(i).text());
                collection.insertOne(document);
                itemsScraped++;

                if (itemsScraped >= 1000) {
                    break;
                }
            }
            page++;
        }



        //JSON
        FileWriter fileWriter = new FileWriter("data.json");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("parser");
        MongoCollection<org.bson.Document> collection = database.getCollection("prices");

        int itemsScraped = 0;
        int page = 2;

        while (itemsScraped < 1000) {
            String website = "https://kolesa.kz/cars/?page=" + page;
            Document doc = Jsoup.connect(website).get();
            Elements prices = doc.select(".a-card__price");
            Elements titles = doc.select(".a-card__title");

            int itemsInThisPage = prices.size();

            for (int i = 0; i < itemsInThisPage; i++) {
                org.bson.Document document = new org.bson.Document("title", titles.get(i).text())
                        .append("price", prices.get(i).text());
                collection.insertOne(document);
                bufferedWriter.write(document.toJson());
                bufferedWriter.newLine();
                itemsScraped++;

                if (itemsScraped >= 1000) {
                    break;
                }
            }

            page++;
        }

        bufferedWriter.close();
    }
}
