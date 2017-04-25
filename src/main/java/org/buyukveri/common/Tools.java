/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.common;

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
}
