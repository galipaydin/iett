/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.common;

import java.io.File;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author galip
 */
public class WebPageDownloader {

    public static Document getPage(String url) {
        try {
            Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();
            response.charset("UTF-8");
            Document doc = response.parse();
            
            /*
            Document doc = Jsoup.
                    parse(new URL(url).openStream(), "UTF-8", url);
            Document doc = Jsoup.parse(u, 5000);
            System.out.println(doc.html());
            return doc;
            
            String content = "";
            while ((s = dis.readLine()) != null) {
                content += s + "\n";
            }
            System.out.println("content = " + content);
            System.out.println("doc. = " + doc.title());
            */
            
            return doc;
            // return Jsoup.parse(content);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Document getFile(String filePath) {
        try {
            Document doc = Jsoup.parse(new File(filePath), "UTF-8");
            return doc;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public static void main(String[] args) {
//        WebPageDownloader.getFile("/Users/galip/NetBeansProjects/NewsDownloader/src/main/resources/commentspage.html");
        WebPageDownloader.getPage("http://www.beyazperde.com/filmler/elestiriler-beyazperde/?page=8");
    }
}
