/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.ibb;

import java.io.FileWriter;
import java.util.Properties;
import org.buyukveri.common.PropertyLoader;
import org.buyukveri.common.WebPageDownloader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author galip
 */
public class OtobusHatlari {

    private Properties p;
    private String dir;
    private JSONObject hatJson;

    public OtobusHatlari() {
        System.out.println("---LOAD PROPS---");
        p = PropertyLoader.loadProperties("gtfs");
        this.dir = p.getProperty("gtfs.dir");
        System.out.println("this.dir = " + this.dir);

        hatJson = new JSONObject();
    }

    //http://www.iett.istanbul/tr/main/hatlar
    public void hatListesi(String url) {
        try {
            FileWriter hatFW = new FileWriter(this.dir + "/hatlistesi.txt");
            Document doc = WebPageDownloader.getPage(url);
            //                <li class="DetailContent" id="StationBusList">
            Elements els = doc.getElementsByAttributeValue("class", "DetailLi");
            System.out.println("els = " + els.size());

            JSONArray hatArray = new JSONArray();

            for (Element el : els) {


//                System.out.println(el.toString());
                String hatAdi = el.attr("data-hat-name");
                String hatKodu = el.attr("data-hat-code");

                Elements as = el.getElementsByTag("a");
                Element a = as.first();
                String href = a.attr("href");
                System.out.println("Hat Adı: " + hatAdi);
                System.out.println("Hat Kod: " + hatKodu);
                System.out.println("URL    : " + href);

                JSONObject el1 = new JSONObject();
                el1.put("hatadi", hatAdi);
                el1.put("hatkodu", hatKodu);
                el1.put("url", href);
                hatArray.put(el1);

//                duraklar(href);
//                guzergah(href);
                System.out.println("");
            }
            
                    hatJson.put("hatlar", hatArray);

        System.out.println(hatJson.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void duraklar(String url) {
        try {
            Document doc = WebPageDownloader.getPage(url);
            System.out.println("url = " + url);
            Elements els = doc.getElementsByAttributeValue("id", "LineStation");
            System.out.println(els.size());
            for (Element el : els) {
                Elements gidisDonus = el.getElementsByAttribute("data-hat-yon");
                for (Element gd : gidisDonus) {
                    String yon = gd.attr("data-hat-yon");
                    String hatKodu = gd.attr("data-hat-code");
                    System.out.println("hatKodu = " + hatKodu);
                    String title = gd.getElementsByAttributeValue("class", "DetailTable_title").first().text();
                    System.out.println("\tyon = " + yon);
                    System.out.println("\ttitle = " + title);

                    Elements stations = gd.getElementsByAttributeValue("class", "LineStation");
                    for (Element station : stations) {
                        String durakKodu = station.attr("data-durak-code");
                        String durakAdi = station.getElementsByAttributeValue("class", "LineStation_name").first().text();
                        String durakSemt = station.getElementsByAttributeValue("class", "LineStation_location").first().text();
                        String link = "";
                        Elements aa = station.getElementsByAttributeValueContaining("class", "LineStation_action-detail");
                        if (aa.size() > 0) {
                            link = aa.first().attr("href");
                            System.out.println("\t\tdurakKodu = " + durakKodu);
                            System.out.println("\t\tdurakAdi = " + durakAdi);
//                        System.out.println("\t\tdurakLokasyon = " + durakSemt);
                            durakDetay(link);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void durakDetay(String url) {
        try {
            Document doc = WebPageDownloader.getPage(url);
            Element stationmap = doc.getElementById("map-canvas");
            String enlem = stationmap.attr("data-map-lat");
            String boylam = stationmap.attr("data-map-lng");
            System.out.println(enlem + "-" + boylam);
            //Duraktan geçen otobüsler
            Elements buses = doc.getElementsByAttributeValue("class", "StationBus");
            for (Element bus : buses) {
                String hatkodu = bus.attr("data-hat-code");
                String hatyonu = bus.attr("data-hat-yon");
                System.out.println(hatkodu + "-" + hatyonu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guzergah(String url) {
        try {
            Document doc = WebPageDownloader.getPage(url);

            Elements linemap = doc.getElementsByAttributeValue("id", "LineMap");
            Elements trips = linemap.first().getElementsByAttributeValueContaining("style", "display:none");
            for (Element trip : trips) {
                String id = trip.attr("id");
                System.out.println(id + " " + trip.text().replaceAll("new google.maps.LatLng", ""));
            }
        } catch (Exception e) {
        }
    }

    public String createJson() {
//        JSONObject j = new JSONObject();
//        JSONObject hat = new JSONObject();
//        JSONObject hatlar = new JSONObject();
//
//        j.put("hat-adi", "aa");
//        j.put("hat-kodu", "aa");
//        j.put("url", "aa");
//        hat.put("hat", j);
//        hatlar.put("hatlar", hat);
////        j = new JSONObject();
//        hat = new JSONObject();
//        j.put("hat-adi", "bb");
//        j.put("hat-kodu", "bb");
//        j.put("url", "bb");
//        hat.put("hat", j);

//        hatlar.put("hatlar", hat);
//        System.out.println(hatlar.toString(2));
        JSONObject object = new JSONObject();

        JSONArray array1 = new JSONArray();

        JSONObject el1 = new JSONObject();
        el1.put("name", "ABC");
        el1.put("type", "STRING");
        array1.put(el1);

        el1 = new JSONObject();
        el1.put("name", "DEF");
        el1.put("type", "INT");
        array1.put(el1);

        object.put("hatlar", array1);

        System.out.println(object.toString(2));

        return object.toString(3);
    }

    public static void main(String[] args) {
        OtobusHatlari o = new OtobusHatlari();
        o.createJson();
//        o.hatListesi("http://www.iett.istanbul/tr/main/hatlar");
        o.duraklar("http://www.iett.istanbul/tr/main/hatlar/2/BOSTANCI%20-%20%C3%9CSK%C3%9CDAR-%C4%B0ETT-Otob%C3%BCs-Sefer-Saatleri-ve-Duraklar%C4%B1");
//        o.guzergah("http://www.iett.istanbul/tr/main/hatlar/1/KİRAZLITEPE - ACIBADEM - KADIKÖY-İETT-Otobüs-Sefer-Saatleri-ve-Durakları\n");
    }
}
