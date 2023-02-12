package com.example.paeser;

import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {
    public static void main(String[] args) throws Exception {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/");
        MongoDatabase database = mongoClient.getDatabase("parser");
        MongoCollection<Document> collection = database.getCollection("prices");

        int page = 2;
        int itemsScraped = 0;

        while (itemsScraped < 1000) {
            String website = "https://www.technodom.kz/catalog/smartfony-i-gadzhety/smartfony-i-telefony/smartfony?page=" + page;
            org.jsoup.nodes.Document doc = Jsoup.connect(website).get();

            Elements URLs = doc.select(".category-page-list__item-link");

            int itemsInThisPage = Math.min(URLs.size(), 1000);

            for (int i = 0; i < itemsInThisPage; i++) {
                Element url = URLs.get(i);
                String link = "https://www.technodom.kz" + url.attr("href");
                org.jsoup.nodes.Document secondURL = Jsoup.connect(link).get();

                Elements brandPhone = secondURL.select(".product-info__header");
                Elements prices = secondURL.select(".product-info__prices.product-prices");
                Elements year = secondURL.select(".product-description__item");
                Elements DisplayDiagonalInch = secondURL.select(".product-description__item");

                Elements DisplayResolution = secondURL.select(".product-description__item");
                Elements MatrixType = secondURL.select(".product-description__item");
                Elements InternalMemoryCapacityGB = secondURL.select(".product-description__item");

                String text = brandPhone.text();
                String[] words = text.split(" ");
                String brand = words[1];


                String color = "";
                int index = text.indexOf("Артикул:");
                if (index != -1) {
                    String afterArticul = text.substring(0, index).trim();
                    String[] ColorWords = afterArticul.split("\\s+");
                    if (ColorWords.length > 0) {
                        String firstWord = ColorWords[ColorWords.length - 1];
                        color += firstWord;
                    }
                }
                
                
                String model = "";
                String[] ModelWords = text.split(" ");
                String thirdWord = ModelWords[2];
                String fourthWord = ModelWords[3];
                model += thirdWord + " " + fourthWord;

                Element secondPTag = year.select("p").get(1);
                Element secondPTag2 = DisplayDiagonalInch.select("p").get(3);
                Element secondPTag3 = DisplayResolution.select("p").get(5);
                Element secondPTag4 = MatrixType.select("p").get(7);
                Element secondPTag6 = InternalMemoryCapacityGB.select("p").get(11);

                String years = secondPTag.text();
                String DisplayDiagonalInchs = secondPTag2.text();
                String DisplayResolutions = secondPTag3.text();
                String MatrixTypes = secondPTag4.text();
                String InternalMemoryCapacityGBs = secondPTag6.text();

                String price = "";
                Pattern pattern = Pattern.compile("(\\d+ \\d+)");
                Matcher matcher = pattern.matcher(prices.text());
                if (matcher.find()) {
                    String res = matcher.group(1);
                    price += res;
                }

                Document document = new Document("Brand", brand)
                        .append("Price", price)
                        .append("Year", years)
                        .append("Color", color)
                        .append("Display Diagonal Inch", DisplayDiagonalInchs)
                        .append("Display Resolution", DisplayResolutions)
                        .append("Matrix Type", MatrixTypes)
                        .append("Internal  Memory Capacity GB", InternalMemoryCapacityGBs);

                collection.insertOne(document);
                itemsScraped++;

                if (itemsScraped >= 1000) {
                    break;
                }
            }
            page++;
        }
    }
}
