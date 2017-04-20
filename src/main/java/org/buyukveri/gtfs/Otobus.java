/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buyukveri.gtfs;

import org.buyukveri.gis.PostGisTools;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import org.buyukveri.common.PropertyLoader;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;

/**
 *
 * @author galip
 */
public class Otobus {
//route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color
//hatkodu,1,hatkodu,hat_adi,,hat_tipi,,,,    

    private PostGisTools pgt;
    private java.sql.Connection conn;
    private Properties p;
    private String dir;

    public Otobus() {
        pgt = new PostGisTools();
        conn = pgt.getConn();
        System.out.println("---LOAD PROPS---");
        p = PropertyLoader.loadProperties("gtfs");
        this.dir = p.getProperty("gtfs.dir");
        System.out.println("this.dir = " + this.dir);
    }

    public ArrayList getIETTRouteCodes() {
        try {
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT distinct(hatkodu) from iett_guzergah order by hatkodu");

            ArrayList<String> routes = new ArrayList<>();

            while (r.next()) {
                String hat = r.getString("hatkodu");
                if (hat != null) {
                    routes.add(hat);
                }

            }

//            for (String route : routes) {
//                System.out.println(route);
//            }
            s.close();
            return routes;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void createRoutesFile() {
        try {
            FileWriter fw = new FileWriter(this.dir + "/routes.txt");

            ArrayList hatkodlari = getIETTRouteCodes();
            int i = 0;

            for (Object object : hatkodlari) {
                String hatkodu = (String) object;
                Statement s = conn.createStatement();
                
                ResultSet r = s.executeQuery("SELECT hat_adi, hat_tipi from iett_guzergah "
                        + "where hatkodu='" + hatkodu + "' limit 1");
                while (r.next()) {
                    String hat_adi = r.getString("hat_adi");
                    String hat_tipi = r.getString("hat_tipi");
                    String line = hatkodu + ",IETT," + hatkodu + "," + hat_adi + ",," + hat_tipi + ",,,";
                    System.out.println(i++ + " = " + line);
                    fw.write(line + "\n");
                    fw.flush();
                }
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void createTripsFile() {
        try {
//route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id
            FileWriter fw = new FileWriter(this.dir + "/trips.txt");

            ArrayList hatkodlari = getIETTRouteCodes();
            int i = 0;

            for (Object object : hatkodlari) {
                String hatkodu = (String) object;
                Statement s = conn.createStatement();
                ResultSet r = s.executeQuery("SELECT guzergahko, hat_tipi,yon from iett_guzergah where hatkodu='" + hatkodu + "' order by hatkodu");
                while (r.next()) {
                    String guzergahkodu = r.getString("guzergahko");
                    String hat_tipi = r.getString("hat_tipi");
                    String yon = r.getString("yon");
                    String line = hatkodu + "," + i++ + "," + guzergahkodu + ",," + yon + ",," + guzergahkodu;
                    //System.out.println(i++ + " = " + line);
                    fw.write(line + "\n");
                    fw.flush();
                }
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void createShapesFile() {
        try {
            System.out.println("HERE");
//shape_id,shape_pt_lon,shape_pt_lat,shape_pt_sequence,shape_dist_traveled
            FileWriter fw = new FileWriter(this.dir + "/shapes.txt");

            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT guzergahko, geom from iett_guzergah order by guzergahko");
            while (r.next()) {
                String guzergahkodu = r.getString("guzergahko");
                System.out.println("guzergahkodu = " + guzergahkodu);
                PGgeometry ge = (PGgeometry) r.getObject(2);
                System.out.println(" ge.getGeometry().getTypeString() = " +  ge.getGeometry().getTypeString());
                if (ge != null) {
                    if (ge.getGeometry() != null) {
                        MultiLineString mls = (MultiLineString) ge.getGeometry();
                        // System.out.println("mls.numLines() = " + mls.numLines());
                        int j = 0;
                        double x1,y1,x2,y2;
                        x1=x2=y1=y2=0;
                        for (int i = 0; i < mls.numLines(); i++) {
                            LineString ls = mls.getLine(i);
                            for (Point point : ls.getPoints()) {
                                x1=point.getX();
                                y1=point.getY();
                                String ln = guzergahkodu + "," + x1 + "," + y1 + "," + j++ + "," + 
                                        PostGisTools.distanceInKm(x1, y1, x2, y2);
                                x2=x1;
                                y2=y1;
                                System.out.println("ln = " + ln);
                                fw.write(ln + "\n");
                                fw.flush();
                            }
                        }
                    }
                }
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    /*
SELECT iett_guzergah.guzergahko, iett_durak.durakadi, iett_durak.id
 FROM iett_guzergah INNER JOIN iett_durak 
   ON ST_DWithin(iett_durak.geom, iett_guzergah.geom,  0.0000001) 
           where iett_guzergah.guzergahko='146T_D_D0' order by iett_durak.id;
     */
    public void createStopsFile__() {
        try {
            //stop_id,stop_name,stop_lon,stop_lat,stop_url
            FileWriter fw = new FileWriter(this.dir + "/stops.txt");
            FileWriter fw1 = new FileWriter(this.dir + "/hatali.txt");

            Statement s = conn.createStatement();

            ResultSet r = s.executeQuery("SELECT guzergahko from iett_guzergah order by guzergahko");
            while (r.next()) {
                String guzergahkodu = r.getString("guzergahko");
                System.out.println("guzergahkodu = " + guzergahkodu);
                if (!guzergahkodu.equals("null")) {
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT iett_guzergah.guzergahko, "
                            + "iett_durak.durakadi as durakadi, iett_durak.id as durakid\n, iett_durak.geom as durakgeom"
                            + " FROM iett_guzergah INNER JOIN iett_durak \n"
                            + "   ON ST_DWithin(iett_durak.geom::geography, iett_guzergah.geom::geography,  5) \n"
                            + "           where iett_guzergah.guzergahko='" + guzergahkodu + "' order by iett_durak.id;");

                    boolean check = false;

                    while (rs.next()) {
                        check = true;
                        String durakid = rs.getString("durakid");
                        String durakadi = rs.getString("durakadi");
                        //System.out.println(durakid +" " + durakadi);
                        PGgeometry ge = (PGgeometry) rs.getObject(4);

                        if (ge.getGeometry().getTypeString().equals("POINT")) {
                            Point poi = (Point) ge.getGeometry();
                            
                            String line = durakid + "," + durakadi + "," + poi.getY() + "," + poi.getX() + ",";
                            fw.write(line + "\n");
                            fw.flush();
//                            System.out.println(line);
                        }
//                    fw.write(line + "\n");
//                    fw.flush();
                    }
                    if (!check) {
                        fw1.write(guzergahkodu + "\n");
                        fw1.flush();
                    }

                    rs.close();
                }
            }
            fw1.close();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    public void createStopsFile() {
        try {
            //stop_id,stop_name,stop_lon,stop_lat,stop_url
            FileWriter fw = new FileWriter(this.dir + "/stops.txt");

            Statement s = conn.createStatement();

                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT durakadi as durakadi, id as durakid, geom"
                            + " from iett_durak order by iett_durak.id;");

                    boolean check = false;

                    while (rs.next()) {
                        check = true;
                        String durakid = rs.getString("durakid");
                        String durakadi = rs.getString("durakadi");
                        //System.out.println(durakid +" " + durakadi);
                        PGgeometry ge = (PGgeometry) rs.getObject(3);

                        if (ge.getGeometry().getTypeString().equals("POINT")) {
                            Point poi = (Point) ge.getGeometry();
                            
                            String line = durakid + "," + durakadi + "," + poi.getY() + "," + poi.getX() + ",";
                            fw.write(line + "\n");
                            fw.flush();
//                            System.out.println(line);
                        }
//                    fw.write(line + "\n");
//                    fw.flush();
            }
                    rs.close();
            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }    
    public static void main(String[] args) {
        Otobus r = new Otobus();
//        r.createRoutesFile();
        r.createShapesFile();
    }

}
