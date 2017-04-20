/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.gis;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 *
 * @author galip
 */
public class PostGisTools {

//You're ready to start developing with Google Maps JavaScript API!
//YOUR API KEY
//AIzaSyDs_aM0vHalre9Iqg7WR6KwxJq6NOJo_f8
    
    public PostGisTools() {
        connect();
    }
    private java.sql.Connection conn;

    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
//            String url = "jdbc:postgresql://193.255.124.98:5432/ibb";
//            String url = "jdbc:postgresql://10.1.1.27:5432/ibb";
            String url = "jdbc:postgresql://127.0.0.1:5432/ibb";
            setConn(DriverManager.getConnection(url, "postgres", "123456"));

            ((org.postgresql.PGConnection) getConn()).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection) getConn()).addDataType("box3d", Class.forName("org.postgis.PGbox3d"));
        } catch (Exception e) {
        }

    }

    public void closeConnection() {
        try {
            this.getConn().close();
        } catch (SQLException ex) {

        }
    }

     /**
     * @return the conn
     */
    public java.sql.Connection getConn() {
        return conn;
    }

    /**
     * @param conn the conn to set
     */
    public void setConn(java.sql.Connection conn) {
        this.conn = conn;
    }
    
//http://www.geodatasource.com/developers/java

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    public static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        try {
            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;

            dist = dist * 1.609344 * 1000;
            DecimalFormat df2 = new DecimalFormat(".###");
            return (Double.parseDouble(df2.format(dist)));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
 /*::	This function converts decimal degrees to radians						 :*/
 /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
 /*::	This function converts radians to decimal degrees						 :*/
 /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
    
    public static void main(String[] args) {
        PostGisTools p = new PostGisTools();
        p.closeConnection();
    }    
}
