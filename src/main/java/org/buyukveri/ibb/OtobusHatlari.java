/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.ibb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import org.buyukveri.common.PropertyLoader;
import org.buyukveri.common.WebPageDownloader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    private FileWriter durakFW;
    private FileWriter durakSaatleriFW;

    public OtobusHatlari() {
        try {
            System.out.println("---LOAD PROPS---");
            p = PropertyLoader.loadProperties("gtfs");
            this.dir = p.getProperty("gtfs.dir");
            System.out.println("this.dir = " + this.dir);
            this.durakFW = new FileWriter(this.dir + "/duraklar.txt");
            this.durakSaatleriFW = new FileWriter(this.dir + "/duraksaatleri.txt");

//            hatJson = new JSONObject();
        } catch (Exception e) {
        }
    }

    //http://www.iett.istanbul/tr/main/hatlar
    public void hatListesi(String url) {
        try {
            FileWriter fw = new FileWriter(this.dir + "/hatlistesi.txt");
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
//                System.out.println("Hat Adı: " + hatAdi);
//                System.out.println("Hat Kod: " + hatKodu);
//                System.out.println("URL    : " + href);

                String line = hatKodu + ";" + hatAdi + ";" + href + "\n";
                fw.write(line);
                fw.flush();
                System.out.println(line);
//                JSONObject el1 = new JSONObject();
//                el1.put("hatadi", hatAdi);
//                el1.put("hatkodu", hatKodu);
//                el1.put("url", href);
//                hatArray.put(el1);

//                duraklar(href);
//                guzergah(href);
            }

//            hatJson.put("hatlar", hatArray);
//            System.out.println(hatJson.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //http://www.iett.istanbul/tr/main/hatlar
    public void hatListesiOku(String path) {
        try {
            Scanner s = new Scanner(new File(path));
            while (s.hasNext()) {
                String ln = s.nextLine();
                String[] a = ln.split(";");
                String hatKodu = a[0];
                String hatAdi = a[1];
                String href = a[2];
                String line = hatKodu + ";" + hatAdi + ";" + href + "\n";
                System.out.println(line);
                duraklar(href);
//                guzergah(href);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void duraklar(String url) {
        try {
            Document doc = WebPageDownloader.getPage(url);
            System.out.println("url = " + url);
            Elements els = doc.getElementsByAttributeValue("id", "LineStation");
//            System.out.println(els.size());
            for (Element el : els) {
                Elements gidisDonus = el.getElementsByAttribute("data-hat-yon");
                for (Element gd : gidisDonus) {
//                    System.out.println("***************************************");
                    String yon = gd.attr("data-hat-yon");
                    String hatKodu = gd.attr("data-hat-code");
//                    System.out.println("hatKodu = " + hatKodu);
//                    String title = gd.getElementsByAttributeValue("class", "DetailTable_title").first().text();
//                    System.out.println("\tyon = " + yon);
//                    System.out.println("\ttitle = " + title);

                    Elements stations = gd.getElementsByAttributeValue("class", "LineStation");
                    for (Element station : stations) {
                        String durakKodu = station.attr("data-durak-code");
                        String durakAdi = station.getElementsByAttributeValue("class", "LineStation_name").first().text();
                        String durakSemt = station.getElementsByAttributeValue("class", "LineStation_location").first().text();
                        String link = "";
                        Elements aa = station.getElementsByAttributeValueContaining("class", "LineStation_action-detail");
                        if (aa.size() > 0) {
                            String line = durakKodu + ";" + durakAdi + ";" + durakSemt;
                            System.out.print(line);
                            durakFW.write(line);
                            durakFW.flush();

                            link = aa.first().attr("href");
//                            System.out.println("\ndurakKodu = " + durakKodu);
//                            System.out.println("\tdurakAdi = " + durakAdi);
//                            System.out.println("\tdurakLokasyon = " + durakSemt);

                            durakDetay(link, durakFW);
                            durakFW.flush();

//                            System.out.println("Saatler");
                            duraktanGecisSaatleri(hatKodu, yon, durakKodu);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void durakDetay(String url, FileWriter fw) {
        try {
            Document doc = WebPageDownloader.getPage(url);
            Element stationmap = doc.getElementById("map-canvas");
            if (stationmap != null) {
                String enlem = stationmap.attr("data-map-lat");
                String boylam = stationmap.attr("data-map-lng");
                System.out.print(";" + enlem + ";" + boylam + ";");
                fw.write(";" + enlem + ";" + boylam + ";");
                //Duraktan geçen otobüsler
//                System.out.println("\tDuraktan geçen otobüsler");
                Elements buses = doc.getElementsByAttributeValue("class", "StationBus");
                String line = "";
                for (Element bus : buses) {
                    String hatkodu = bus.attr("data-hat-code");
                    String hatyonu = bus.attr("data-hat-yon");
//                    System.out.println("\t" + hatkodu + "-" + hatyonu);
                    line += hatkodu + "-" + hatyonu + ",";
                }
                line = line.substring(0, line.length() - 1);
                fw.write(line + "\n");
                System.out.println(line);
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

    public String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String json = s.hasNext() ? s.next() : "";
        if (json.startsWith("[")) {
            json = json.substring(1, json.length());
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }
        return json;
    }

    public JSONObject getDurakJson(URL url) {
        try {
            HttpURLConnection request;
            request = (HttpURLConnection) url.openConnection();
            request.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
            request.setConnectTimeout(6000);
            request.connect();
            InputStream in = new BufferedInputStream(request.getInputStream());
            String s = convertStreamToString(in);
//            System.out.println("s = " + s);
            JSONTokener tokener = new JSONTokener(s);
            JSONObject o = new JSONObject(tokener);
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void duraktanGecisSaatleri(String hat, String yon, String durakId) {
        try {
            String url = "http://www.iett.istanbul/tr/main/tahminiGecisSaatleri/?hat=" + hat + "&durak=" + durakId + "&yon=" + yon
                    + "&format=json";
//            System.out.println(url);
            JSONObject obj = getDurakJson(new URL(url));
            JSONObject items = obj.getJSONObject("items");
//            System.out.println(items.getString("hat_code"));
//            System.out.println(items.getString("hat_name"));
//            System.out.println(items.getString("durak_code"));
//            System.out.println(items.getString("durak_name"));
//            System.out.println(items.getString("firstDurak"));
//            System.out.println(items.getString("notlar"));

            durakSaatleriFW.write(items.getString("hat_code") + ";" + yon + ";" + items.getString("durak_code") + ";");

            if (items.getJSONArray("saat") != null) {
                JSONArray saatler = items.getJSONArray("saat");
                Iterator it = saatler.iterator();
                String line = "";
                while (it.hasNext()) {
                    JSONObject next = (JSONObject) it.next();
                    String gun = next.getString("gun");
                    String saat = next.getString("saat");
//                System.out.println(gun + "-" + saat);
                    line += gun + "-" + saat + ",";
                }
                if (line.endsWith(",")) {
                    line = line.substring(0, line.length() - 1);
                }
                durakSaatleriFW.write(line);
            }
            durakSaatleriFW.write("\n");
            durakSaatleriFW.flush();
//            System.out.println(items.getJSONArray("saat").length());
//            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            try {
                durakSaatleriFW.write("\n");
                durakSaatleriFW.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        //http://www.iett.istanbul/tr/main/tahminiGecisSaatleri/?hat=2&durak=403181&yon=G&format=json
        OtobusHatlari o = new OtobusHatlari();
//        o.createJson();
        o.hatListesi("http://www.iett.istanbul/tr/main/hatlar");
//          o.hatListesiOku("/Users/galip/NetBeansProjects/gtfs/gtfs-files/hatlistesi.txt");
//        o.duraklar("http://www.iett.istanbul/tr/main/hatlar/2/BOSTANCI%20-%20%C3%9CSK%C3%9CDAR-%C4%B0ETT-Otob%C3%BCs-Sefer-Saatleri-ve-Duraklar%C4%B1");
//        o.guzergah("http://www.iett.istanbul/tr/main/hatlar/1/KİRAZLITEPE - ACIBADEM - KADIKÖY-İETT-Otobüs-Sefer-Saatleri-ve-Durakları\n");
//        o.duraktanGecisSaatleri("2", "G","403181");
    }
}
