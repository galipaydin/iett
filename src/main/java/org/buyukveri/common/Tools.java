/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.common;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author galip
 */
public class Tools {
    
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
    
    public void duplicateRemover(String path){
        try {
            FileWriter fw = new FileWriter(path.substring(0, path.indexOf("."))+"_x.txt");
            Scanner s = new Scanner(new File(path));
            while(s.hasNext()){
                String line = s.nextLine();
                String [] a = line.split(";");
                String id1 = a[0];
                if(s.hasNext()){
                String [] b = s.nextLine().split(";");
                String id2 = a[0];
                    if(id1.equals(id2))
                    System.out.println(id1 + "=" + id2);
                    fw.write(line+"\n");
                    fw.flush();
                }
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Tools t = new Tools();
        t.duplicateRemover("/Users/galip/NetBeansProjects/gtfs/gtfs-files/duraksaatleri_2.txt");
    }
}
