package com.example.weatheralert;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.ui.AppBarConfiguration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlertCitiesActivity extends AppCompatActivity implements OnMapReadyCallback {
    public class AlertObject {
        public List<String> capLinks;

        public String areaDesc;

        public String event;
        public String severity;
        public String certainty;
        public String audience;
    }
    private AppBarConfiguration appBarConfiguration;
    private List<LatLng> latLngList = new ArrayList<>();
    private GoogleMap mMap;
    private TextView textViewGuelph;
    private TextView textViewToronto;
    private TextView textViewWaterloo;
    private TextView textViewWindsor;
    private TextView textViewDemo;
    private Button impactedPolygonButton;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // this is the starting point of the camera
        LatLng coordinate = new LatLng(43,-79);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 5));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_cities);

        textViewGuelph = findViewById(R.id.city_guelph);
        textViewToronto = findViewById(R.id.city_toronto);
        textViewWaterloo = findViewById(R.id.city_waterloo);
        textViewWindsor = findViewById(R.id.city_windsor);
        textViewDemo = findViewById(R.id.city_demo);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        DownloadWebpageTask myTask = new DownloadWebpageTask();
        myTask.execute("https://dd.weather.gc.ca/alerts/cap/");


        impactedPolygonButton = findViewById(R.id.button_impacted_polygon);
        impactedPolygonButton.setText("Waiting for the API call result...");
        impactedPolygonButton.setEnabled(false);

        Button back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(AlertCitiesActivity.this, MainActivity.class);
                startActivity(startIntent);
            }
        });
    }

    private void drawPolygon() {
        if (mMap != null && !latLngList.isEmpty()) {
            LatLng startPosition = new LatLng(latLngList.get(0).latitude,latLngList.get(0).longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 5));

            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(latLngList)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(255, 255, 0, 0));

            mMap.addPolygon(polygonOptions);

            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    public static boolean isInsidePolygon(List<LatLng> polygon, LatLng point) {
        int numVertices = polygon.size();
        if (numVertices < 3) {
            return false;
        }

        boolean isInside = false;

        for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
            LatLng vertex1 = polygon.get(i);
            LatLng vertex2 = polygon.get(j);

            double xi = vertex1.latitude;
            double yi = vertex1.longitude;
            double xj = vertex2.latitude;
            double yj = vertex2.longitude;

            if ((yi > point.longitude) != (yj > point.longitude) &&
                    point.latitude < (xj - xi) * (point.longitude - yi) / (yj - yi) + xi) {
                isInside = !isInside;
            }
        }

        return isInside;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {
            try {
                String[] list = downloadUrl("url");

                AlertObject alertObject = processCapLinks(list);

                // TODO: following are the attributes to display in the alter page
                String areaDesc = alertObject.areaDesc;
                String event = alertObject.event;
                String severity = alertObject.severity;
                String certainty = alertObject.certainty;
                String audience = alertObject.audience;
                List<String> lists = alertObject.capLinks;

                // ensure not duplicate coordinates added
                Set<String> latLngSet = new HashSet<>();

                if (lists != null && lists.size() ==1 )
                {
                    String coordinates = lists.get(0);
                    String[] latLngPairs = coordinates.split(" ");

                    for (String latLngPair : latLngPairs) {
                        String[] latLngValues = latLngPair.split(",");
                        if (latLngValues.length == 2) {
                            double latitude = Double.parseDouble(latLngValues[0]);
                            double longitude = Double.parseDouble(latLngValues[1]);

                            String key = latitude + "_" + longitude;

                            if (!latLngSet.contains(key)) {
                                latLngSet.add(key);
                                LatLng latLng = new LatLng(latitude, longitude);
                                latLngList.add(latLng);
                            }
                        }
                    }
                }

                Log.d("!!!! latLngList 222: ", areaDesc);
                Log.d("!!!@ latLngList 222: ", alertObject.toString());

            } catch (IOException e) {
                return new String[]{"Unable to retrieve web page. URL may be invalid."};
            }

            return new String[0];
        }
        @Override
        protected void onPostExecute(String[] result) {
            impactedPolygonButton.setEnabled(true);
            impactedPolygonButton.setText("Press to check an affected region (if any)");

            LatLng guelph_coordinate = new LatLng(43.54466179547056, -80.24832672542367);
            LatLng toronto_coordinate = new LatLng(43, -79);
            LatLng waterloo_coordinate = new LatLng(43.463498172877344, -80.52218253067909);
            LatLng windsor_coordinate = new LatLng(42.31430903792992, -83.04133517299202);
            LatLng demo_coordinate = new LatLng(46.32945571837806, -63.185310053397316);

            boolean isInGuelph = isInsidePolygon(latLngList, guelph_coordinate);
            boolean isInToronto = isInsidePolygon(latLngList, toronto_coordinate);
            boolean isInWaterloo = isInsidePolygon(latLngList, waterloo_coordinate);
            boolean isInWindsor = isInsidePolygon(latLngList, windsor_coordinate);
            boolean isInDemo = isInsidePolygon(latLngList, demo_coordinate);

            textViewGuelph.setText("Guelph: " + isInGuelph);
            textViewToronto.setText("Toronto: " + isInToronto);
            textViewWaterloo.setText("Waterloo: " + isInWaterloo);
            textViewWindsor.setText("Windsor: " + isInWindsor);
            textViewDemo.setText("Prince Edward Island: " + isInDemo);

            // check if the coordinates is inside the polygon or not
            LatLng coordinate1 = new LatLng(67.8000, -115.3000);
            boolean flag1 = isInsidePolygon(latLngList, coordinate1);

            System.out.println("------------------");
            LatLng coordinate2 = new LatLng(67.850629, -10.155679);
            boolean flag2 = isInsidePolygon(latLngList, coordinate2);

            impactedPolygonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawPolygon();
                }
            });
        }

        public AlertObject processCapLinks(String[] result){

            List<String> capLinks = new ArrayList<>(Arrays.asList(result));
            int fileLimit = 10; // Replace with your file limit
            int fileCounter = 0;
            List<String> capData = new ArrayList<>();

            OkHttpClient client = new OkHttpClient();
            Pattern datePattern = Pattern.compile("(\\d{8})");

            for (int index = 0; index < capLinks.size(); index++) {
                String link = capLinks.get(index);

                if (index >= fileLimit) {
                    break;
                }

                String capUrl = link;

                Request request = new Request.Builder().url(capUrl).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        continue;
                    }

                    Document capSoup = Jsoup.parse(response.body().string(), "", org.jsoup.parser.Parser.xmlParser());
                    Matcher matcher = datePattern.matcher(link);

                    if (matcher.find()) {

                        String dateFolder = matcher.group(1);
//                    LocalDate alertDate = LocalDate.parse(dateFolder, DateTimeFormatter.ofPattern("yyyyMMdd"));
//
//                    // Extract the rest of the information from the CAP file
//                    // Replace with your logic to parse the CAP file
//                    String capInfo = parseCapFile(capSoup);
//                    capInfo += " " + alertDate.toString();

                        Elements areaDesc = capSoup.select("areaDesc");
                        System.out.println("111111 areaDesc!!!." + areaDesc.first().text());

                        Elements event = capSoup.select("event");
                        System.out.println("111111 event!!!." + event.first().text());

                        Elements severity = capSoup.select("severity");
                        System.out.println("111111 severity!!!." + severity.first().text());

                        Elements certainty = capSoup.select("certainty");
                        System.out.println("111111 certainty!!!." + certainty.first().text());

                        Elements audience = capSoup.select("audience");
                        System.out.println("111111 audience!!!." + audience.first().text());

                        Elements polygon = capSoup.select("polygon");
                        if (!polygon.isEmpty()) {
                            capData.add(polygon.text());
                        }

                        fileCounter++;

//                    capData.add(capInfo);

                        AlertObject alertObject = new AlertObject();
                        alertObject.capLinks = capData;
                        alertObject.areaDesc = areaDesc.first().text();
                        alertObject.event = event.first().text();
                        alertObject.severity = severity.first().text();
                        alertObject.certainty = certainty.first().text();
                        alertObject.audience = audience.first().text();

                        return alertObject;
                    } else {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        private String[] downloadUrl(String myurl) throws IOException {

            String baseUrl = "https://dd.weather.gc.ca/alerts/cap/";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Document doc1 = Jsoup.parse(response.body().string());
                String html = doc1.body().html();

                Pattern pattern = Pattern.compile("\\d{8}/");
                Matcher matcher = pattern.matcher(html);

                ArrayList<String> dateFolders = new ArrayList<>();
                while (matcher.find()) {
                    dateFolders.add(matcher.group());
                }

                if (dateFolders.isEmpty()) {
                    System.out.println("No date folders found in the CAP URL.");
                } else {
                    String latestFolderDate = dateFolders.get(dateFolders.size() - 1).replace("/", "");

                    List<String> subFolders = getSubFolders(baseUrl + latestFolderDate + '/');

                    subFolders.forEach(System.out::println);

                    List<String> capLinks = new ArrayList<>();

                    // Fetch .cap links from the sub-folders of the latest date folder
                    for (String subFolder : subFolders) {
                        client = new OkHttpClient();
                        request = new Request.Builder()
                                .url(baseUrl + latestFolderDate + "/" + subFolder)
                                .build();
                        try (Response response1 = client.newCall(request).execute()) {
                            Document doc = Jsoup.parse(response1.body().string());

                            Elements links = doc.select("a[href]");

                            for (Element link : links) {
                                String href = link.attr("href");
                                if (href.endsWith(".cap")) {
                                    capLinks.add(baseUrl + latestFolderDate + "/" + subFolder + href);

                                } else {
                                    request = new Request.Builder()
                                            .url(baseUrl + latestFolderDate + "/" + subFolder + href)
                                            .build();

                                    try (Response response2 = client.newCall(request).execute()) {
                                        Document innerDoc = Jsoup.parse(response2.body().string());

                                        Elements innerLinks = innerDoc.select("a[href]");

                                        for (Element innerLink : innerLinks) {
                                            String innerHref = innerLink.attr("href");

                                            if (innerHref.endsWith(".cap")) {
                                                capLinks.add(baseUrl + latestFolderDate + "/" + subFolder + href + innerHref);
                                            }
                                        }
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    capLinks.forEach(System.out::println);
                    return capLinks.toArray(new String[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String[0];


        }

        private List<String> getSubFolders(String url) throws IOException {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            List<String> subFolders = new ArrayList<>();

            for (org.jsoup.nodes.Element link : links) {
                String href = link.attr("href");
                if (href.contains("/") && !href.endsWith(".cap")) {
                    subFolders.add(href);
                }
            }
            return subFolders;
        }
    }


}