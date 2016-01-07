package me.calebjones.spacelaunchnow.content.loader;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.calebjones.spacelaunchnow.LaunchApplication;
import me.calebjones.spacelaunchnow.content.models.Launch;
import me.calebjones.spacelaunchnow.content.models.Location;
import me.calebjones.spacelaunchnow.content.models.LocationAgency;
import me.calebjones.spacelaunchnow.content.models.Mission;
import me.calebjones.spacelaunchnow.content.models.Pad;
import me.calebjones.spacelaunchnow.content.models.Rocket;
import me.calebjones.spacelaunchnow.content.models.RocketAgency;
import timber.log.Timber;

/**
 * Class that parses the next 10 upcoming launches.
 */
public class DetailLoader extends AsyncTask<String, Void, Launch> {
    public static Launch detailLaunch;
    public static String cache;


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Launch doInBackground(String... params) {
        InputStream inputStream = null;
        Integer result = 0;
        HttpURLConnection urlConnection = null;

        try {
            Timber.d(params[0]);

            /* forming th java.net.URL object */
            URL url = new URL(params[0]);

            urlConnection = (HttpURLConnection) url.openConnection();

                /* for Get request */
            urlConnection.setRequestMethod("GET");

            int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
            if (statusCode == 200) {

                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    response.append(line);
                }

                return parseResult(response.toString());
            } else {
                return null;
            }

        } catch (Exception e) {
            Timber.d(e.getLocalizedMessage());
        }
        return null; //"Failed to fetch data!";
    }

    public static Launch getWords() {
        if (detailLaunch == null) {
        }
        return detailLaunch;
    }

    @Override
    protected void onPostExecute(Launch result) {
            /* Download complete. Lets update UI */
        if (result != null) {
//                BlogFragment.setList(feedItemList);
            Timber.d("Succeeded fetching data! - Launcher Loader");
        } else Timber.e("Failed to fetch data!");
    }

    public Launch parseResult(String result) throws JSONException {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray launchesArray = response.optJSONArray("launches");

            JSONObject launchesObj = launchesArray.optJSONObject(0);
            JSONObject rocketObj = launchesObj.optJSONObject("rocket");
            JSONObject locationObj = launchesObj.optJSONObject("location");

            Launch launch = new Launch();
            Rocket rocket = new Rocket();
            Mission mission = new Mission();
            Location location = new Location();

            launch.setName(launchesObj.optString("name"));
            launch.setId(launchesObj.optInt("id"));
            launch.setWindowstart(launchesObj.optString("windowstart"));
            launch.setWindowend(launchesObj.optString("windowend"));
            launch.setWsstamp(launchesObj.optInt("wsstamp"));
            launch.setWestamp(launchesObj.optInt("westamp"));
            launch.setStatus(launchesObj.optInt("status"));
            launch.setVidURL(launchesObj.optString("vidURL"));

            Timber.d("Launch %s : %s", 0 , launch.getName());

            //Start Parsing Rockets
            if (rocketObj != null) {
                RocketAgency rocketAgency = new RocketAgency();

                rocket.setId(rocketObj.optInt("id"));
                rocket.setName(rocketObj.optString("name"));
                rocket.setFamilyname(rocketObj.optString("familyname"));
                rocket.setConfiguration(rocketObj.optString("configuration"));
                Timber.d("Launch %s : Rocket - %s", 0 , rocket.getName());

                JSONArray agencies = rocketObj.optJSONArray("agencies");
                if (agencies != null) {
                    List<RocketAgency> rocketList = new ArrayList<>();
                    for (int a = 0; a < agencies.length(); a++) {
                        JSONObject agencyObj = launchesArray.optJSONObject(a);
                        rocketAgency.setName(agencyObj.optString("name"));
                        rocketAgency.setAbbrev(agencyObj.optString("abbrev"));
                        rocketAgency.setCountryCode(agencyObj.optString("countryCode"));
                        rocketAgency.setType(agencyObj.optInt("type"));
                        rocketAgency.setInfoURL(agencyObj.optString("infoURL"));
                        rocketAgency.setWikiURL(agencyObj.optString("wikiURL"));

                        Timber.d("Launch %s : Rocket Agency - %s", 0 , rocketAgency.getName());
                        rocketList.add(rocketAgency);
                    }
                    rocket.setAgencies(rocketList);
                }
                launch.setRocket(rocket);
            }

            //Start Parsing Locations
            if (locationObj != null) {
                LocationAgency locationAgency = new LocationAgency();
                Pad locationPads = new Pad();

                JSONArray pads = locationObj.optJSONArray("pads");

                if (pads != null) {
                    List<Pad> locationPadsList = new ArrayList<>();
                    for (int a = 0; a < pads.length(); a++) {
                        JSONObject padsObj = pads.optJSONObject(a);
                        location.setId(padsObj.optInt("id"));
                        location.setName(padsObj.optString("name"));
                        Timber.d("Launch %s: Location  -  %s ",0, location.getName());
                        locationPads.setName(padsObj.optString("name"));
                        locationPads.setLatitude(padsObj.optDouble("latitude"));
                        locationPads.setLongitude(padsObj.optDouble("longitude"));
                        locationPads.setMapURL(padsObj.optString("mapURL"));
                        locationPads.setInfoURL(padsObj.optString("infoURL"));
                        locationPads.setWikiURL(padsObj.optString("wikiURL"));

                        Timber.d("Launch %s : Pad -  %s ",0, locationPads.getName());
                        JSONArray padAgencies = padsObj.optJSONArray("agencies");
                        if (padAgencies != null) {
                            List<LocationAgency> locationAgencies = new ArrayList<>();
                            for (int b = 0; b < padAgencies.length(); b++) {
                                JSONObject padAgenciesObj = padAgencies.optJSONObject(b);
                                locationAgency.setName(padAgenciesObj.optString("name"));
                                locationAgency.setAbbrev(padAgenciesObj.optString("abbrev"));
                                locationAgency.setCountryCode(padAgenciesObj.optString("countryCode"));
                                locationAgency.setType(padAgenciesObj.optInt("type"));
                                locationAgency.setInfoURL(padAgenciesObj.optString("infoURL"));
                                locationAgency.setWikiURL(padAgenciesObj.optString("wikiURL"));

                                Timber.d("Launch %s: Pad Agency - %s" , 0 , locationPads.getName());
                                locationAgencies.add(locationAgency);
                            }
                        }
                        locationPadsList.add(locationPads);
                    }
                    location.setPads(locationPadsList);
                }
                launch.setLocation(location);
            }

            // Start parsing Missions
            JSONArray missions = launchesObj.optJSONArray("missions");
            if (missions != null) {
                List<Mission> missionList = new ArrayList<>();
                for (int c = 0; c < missions.length(); c++) {
                    JSONObject missionObj = missions.optJSONObject(c);
                    mission.setId(missionObj.optInt("id"));
                    mission.setName(missionObj.optString("name"));
                    mission.setDescription(missionObj.optString("description"));

                    missionList.add(mission);
                    Timber.d("Launch %s: Mission  -  %s ", missionList.get(c).getDescription());
                }
                launch.setMissions(missionList);
            }

            detailLaunch = launch;
            return detailLaunch;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
