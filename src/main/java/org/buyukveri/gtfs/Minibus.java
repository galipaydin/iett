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
public class Minibus {
//route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color
//hatkodu,1,hatkodu,hat_adi,,hat_tipi,,,,    

    private PostGisTools pgt;
    private java.sql.Connection conn;
    private Properties p;
    private String dir;

    public Minibus() {
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
            ResultSet r = s.executeQuery("SELECT distinct(tubs_id) from minibus order by tubs_id");

            ArrayList<String> routes = new ArrayList<>();

            while (r.next()) {
                String hat = r.getString("hatid");
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
//route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color

//route_id,agency_id,route_short_name,route_long_name,route_type,route_url
//route_id,agency_id,route_short_name,route_long_name,route_type,route_url
//1,TRIMET,1,Vermont,3,http://trimet.org//schedules/r001.htm,400
//"41",1,"41","Columbia Pike-Ballston-Court House",3,"http://www.arlingtontransit.com/pages/routes/art-41/"

            FileWriter fw = new FileWriter(this.dir + "/minibus/routes.txt");

            ArrayList hatkodlari = getIETTRouteCodes();
            int i = 0;

            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("select distinct(hatid), hat_adi, hatno from minibus order by hatid");
            while (r.next()) {
                String hatid = r.getString("hatid");
                String hatadi = r.getString("hat_adi");
                hatadi = hatadi.replaceAll(",", "-");
                String hatno = r.getString("hatno");
                String line = hatid + ",MINIBUS," + hatno + ","+ hatadi +","+"2,www.ibb.gov.tr";
                System.out.println(i++ + " = " + line);
                fw.write(line + "\n");
                fw.flush();
            }

            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void createTripsFile() {
        try {
//route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id
//route_id,service_id,trip_id,direction_id,block_id,shape_id,trip_type
//hatid,,tubs_id,tubs_kod,gidis_donu,,

            FileWriter fw = new FileWriter(this.dir + "/minibus/trips2.txt");

            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT distinct(tubs_id),hatid,tubs_kod,gidis_donu from minibus order by tubs_id");
            int i = 0;
            while (r.next()) {
                String tubs_id = r.getString("tubs_id");
                String hatid = r.getString("hatid");
                String tubs_kod = r.getString("tubs_kod");
                String gidis_donu = r.getString("gidis_donu");
                String line = hatid + ",2," + tubs_id + "," + tubs_kod + "," + gidis_donu + ",,"+tubs_id;
                //System.out.println(i++ + " = " + line);
                fw.write(line + "\n");
                fw.flush();
            }

            fw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void createShapesFile() {
        try {
//shape_id,shape_pt_lon,shape_pt_lat,shape_pt_sequence,shape_dist_traveled
            FileWriter fw = new FileWriter(this.dir + "/minibus/shapes.txt");

            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT tubs_id, geom from minibus");
            while (r.next()) {
                String guzergahkodu = r.getString("tubs_id");
                System.out.println("guzergahkodu = " + guzergahkodu);
                PGgeometry ge = (PGgeometry) r.getObject(2);
                System.out.println(" ge.getGeometry().getTypeString() = " + ge.getGeometry().getTypeString());
                if (ge != null) {
                    if (ge.getGeometry() != null) {
                        MultiLineString mls = (MultiLineString) ge.getGeometry();
                        //System.out.println("mls.numLines() = " + mls.numLines());
                        int j = 0;
                        double x1, y1, x2, y2;
                        x1 = x2 = y1 = y2 = 0;

                        for (int i = 0; i < mls.numLines(); i++) {
                            LineString ls = mls.getLine(i);
                            for (Point point : ls.getPoints()) {
                                x1 = point.getX();
                                y1 = point.getY();
//shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled
                                
                                String ln = guzergahkodu + "," + x1 + "," + y1 + "," + j++ + ","
                                        + PostGisTools.distanceInKm(x1, y1, x2, y2);
                                x2 = x1;
                                y2 = y1;
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
     
SELECT minibus.tubs_id, minibus_durak.durak_adi as durakadi, minibus_durak.durakkodu as durakid, minibus_durak.geom as durakgeom
FROM minibus INNER JOIN minibus_durak 
ON ST_DWithin(minibus_durak.geom::geography, minibus.geom::geography,  5) 
where minibus.tubs_id='20-6-0_G' order by minibus_durak.durakkodu      
    */
    public void createStopTimesFile() {
        try {
            //trip_id,arrival_time,departure_time,stop_id,stop_sequence
            FileWriter fw = new FileWriter(this.dir  + "/minibus/hat_durak.txt");
            FileWriter fw1 = new FileWriter(this.dir + "/minibus/hatali.txt");

            Statement s = conn.createStatement();

            ResultSet r = s.executeQuery("SELECT tubs_id from minibus order by tubs_id");
            while (r.next()) {
                String guzergahkodu = r.getString("tubs_id");
                System.out.println("tubs_id = " + guzergahkodu);
                if (!guzergahkodu.equals("null")) {
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT minibus.tubs_id, "
                            + "minibus_durak.durak_adi as durakadi, minibus_durak.durakkodu as durakid\n, minibus_durak.geom as durakgeom"
                            + " FROM minibus INNER JOIN minibus_durak \n"
                            + "   ON ST_DWithin(minibus_durak.geom::geography, minibus.geom::geography,  5) \n"
                            + "           where minibus.tubs_id='" + guzergahkodu + "' order by minibus_durak.durakkodu;");

                    boolean check = false;

                    while (rs.next()) {
                        check = true;
                        String durakid = rs.getString("durakid");
                        String durakadi = rs.getString("durakadi");
                        System.out.println("\n" + durakid +" " + durakadi);
                        PGgeometry ge = (PGgeometry) rs.getObject(4);

                        if (ge.getGeometry().getTypeString().equals("POINT")) {
                            Point poi = (Point) ge.getGeometry();
            //trip_id,arrival_time,departure_time,stop_id,stop_sequence
                            String line = durakid + "," + durakadi + "," + poi.getY() + "," + poi.getX() + ",";
                            String ln = guzergahkodu + "," + durakid;
                            fw.write(ln + "\n");
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
            FileWriter fw = new FileWriter(this.dir + "/minibus/stops.txt");
            fw.write("stop_id,stop_name,stop_lat,stop_lon,stop_url\n");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT durakkodu, durak_adi, geom from minibus_durak order by durakkodu;");

            boolean check = false;

            while (rs.next()) {
                check = true;
                String durakid = rs.getString("durakkodu");
                String durakadi = rs.getString("durak_adi");
                //System.out.println(durakid +" " + durakadi);
                PGgeometry ge = (PGgeometry) rs.getObject(3);

                if (ge.getGeometry().getTypeString().equals("POINT")) {
                    Point poi = (Point) ge.getGeometry();
//stop_id,stop_code,stop_name,stop_lat,stop_lon,stop_url
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
        Minibus r = new Minibus();
        r.createStopTimesFile();
    }

}
