package com.bbridge.test;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by shambala on 26.08.17.
 */
class Downloader {
    private static final String CONSUMER_KEY = "OHbGB3E5SzqFjjzZgth70bY4X";
    private static final String CONSUMER_SECRET = "8vua0CqABCXyrbs3TJEaSZcsiUemBFtSMUKSm92N2At5D07Gl3";

    private static final String ACCESS_TOKEN = "3028604098-qr8F4HaKFrYxNI6Hq8U5nwu2wK6xPBo0KQz7244";
    private static final String ACCESS_TOKEN_SECRET = "czyMchqoNJJ5UCjxLmlYBhqW2sxsxqpKdiSTDF9NZkJum";

    private static OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    static {
        consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
    }
    private String authorizationToken;

    Downloader() {
    }

    Downloader(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    String authorize(final String username, final String password) {
        JSONObject auth = downloadFromBBridge("http://bbridgeapi.cloudapp.net/v1/auth", new JSONObject() {
            {
                put("username", username);
                put("password", password);
            }
        });
        if (auth != null) {
            return auth.optString("token", null);
        } else return null;
    }

    private String downloadFromTwitter(String inputUrl) {
        try {
            URL url = new URL(inputUrl);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            consumer.sign(request);
            request.connect();
            String result = readFromStream(request.getInputStream());
            request.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Number> getFollowers(String name) {
        String r = downloadFromTwitter("https://api.twitter.com/1.1/followers/ids.json?screen_name="+name);
        List<Number> result = new ArrayList<>();
        if (r == null) {
            return result;
        }
        JSONObject response = new JSONObject(r);
        JSONArray ids = response.getJSONArray("ids");
        for (Object id : ids.toList()) {
            result.add((Number) id);
        }
        return result;
    }

    private JSONObject getUserProfiling(Number id) {
        JSONObject content = new JSONObject();
        String r = downloadFromTwitter("https://api.twitter.com/1.1/statuses/user_timeline.json?user_id="+id.longValue());
        if (!isJSONArrayValid(r) || r == null) {
            return null;
        }
        JSONArray response = new JSONArray(r);
        if (response.toList().isEmpty()) {
            return null;
        }
        for (Object element : response) {
            JSONObject tweet = (JSONObject) element;
            content.append("text", tweet.getString("text"));
            JSONArray media = tweet.getJSONObject("entities").optJSONArray("media");
            if (media != null) {
                for (Object image : media) {
                    content.append("image_urls", ((JSONObject) image).getString("media_url"));
                }
            }
        }
        JSONObject requestID = downloadFromBBridge("http://bbridgeapi.cloudapp.net/v1/profiling/personal", content);
        return requestID == null ? null : getBbridgeResponse(requestID.optString("request_id", null));
    }

    private boolean isJSONArrayValid(String test) {
        try {
            new JSONArray(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }


    JSONArray getFollowersProfiling(String name) {
        List<Number> followers = getFollowers(name);
        JSONArray result = new JSONArray();
        for (Number id : followers) {
            JSONObject profiling = getUserProfiling(id);
            if (profiling != null) {
                profiling.put("id", id);
                result.put(profiling);
            }
        }
        return result;
    }


    private JSONObject downloadFromBBridge(String inputUrl, JSONObject jsonData) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(inputUrl + (authorizationToken == null ? "" : "?"+getPostDataString(new ArrayList<StringPair>() {
                {
                    add(new StringPair("lang", "en"));
                    add(new StringPair("attr", "gender"));
                    add(new StringPair("attr", "age_group"));
                    add(new StringPair("attr", "relationship"));
                    add(new StringPair("attr", "education_level"));
                    add(new StringPair("attr", "income"));
                    add(new StringPair("attr", "occupation"));
                }
            })));
            request.setHeader("Content-type", "application/json");
            if (authorizationToken != null) {
                request.setHeader("Authorization", authorizationToken);
            }
            request.setEntity(new ByteArrayEntity(jsonData.toString().getBytes("UTF-8")));
            HttpResponse response = client.execute(request);
            String result = readFromStream(response.getEntity().getContent());
            return new JSONObject(result);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getBbridgeResponse(String id) {
        try {
            HttpClient client = HttpClientBuilder.create().disableAuthCaching().build();
            HttpGet request = new HttpGet("http://bbridgeapi.cloudapp.net/v1/response?id="+id);
            request.setHeader("Authorization", authorizationToken);
            request.setHeader("Cache-Control", "no-cache");
            HttpResponse response = client.execute(request);
            while (response.getStatusLine().getStatusCode() == 204) {
                response = client.execute(request);
            }
            String result = readFromStream(response.getEntity().getContent());
            return new JSONObject(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPostDataString(ArrayList<StringPair> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(StringPair entry : params){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.second, "UTF-8"));
        }

        return result.toString();
    }

    String readFromStream(InputStream is) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder buffer = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
                buffer.append("\n");
            }
            in.close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    class StringPair {
        String first;
        String second;

        StringPair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
