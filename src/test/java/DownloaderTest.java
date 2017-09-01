import com.bbridge.test.Downloader;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.method;

/**
 * Created by shambala on 01.09.17.
 */
public class DownloaderTest {
    private Downloader downloader = new Downloader();
    private static final int PORT = 7777;

    @Test
    public void downloadFromTwitter() {
        StubServer stubServer = null;
        try {
            stubServer = getStubServer(stringContent("pong"));
            String result = downloader.downloadFromTwitter("http://localhost:" + PORT + "/ping");
            Assert.assertEquals("pong\n", result);
        } finally {
            if (stubServer != null) {
                stubServer.stop();
            }
        }
    }

    @Test
    public void downloadFromBbridge() {
        StubServer stubServer = null;
        try {
            stubServer = getStubServer(Action.stringContent("{ \"hello\": \"world\" }"));
            JSONObject result = downloader.downloadFromBBridge("http://localhost:" + PORT + "/ping", new JSONObject());
            Assert.assertEquals(new JSONObject(){{put("hello", "world");}}.toString(), result.toString());
        } finally {
            if (stubServer != null) {
                stubServer.stop();
            }
        }
    }

    private StubServer getStubServer(Action action) {
        StubServer stubServer = new StubServer(PORT).run();
        whenHttp(stubServer).match(method(Method.GET).startsWithUri("/ping")).then(action);
        return stubServer;
    }

    @Test
    public void getProfiling() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtZXNjYXNlIiwiY3JlYXRlZCI6MTUwNDIxODc3OTY2NiwiZXhwIjoxNTA0ODIzNTc5fQ.S6m1-HlnFwtO8Bv3kCCh9RZlMnCxZwyeIC98o3r7CJDIa09tD-EjDMq3i5xkth-Bqt84HmM0WIp6UgTrYeM-IQ";
        downloader = new Downloader(token);
        List<Number> followers = downloader.getFollowers("helloworld");
        JSONArray profiling = downloader.getFollowersProfiling("helloworld");
        Assert.assertEquals(followers.size(), profiling.length());
        HashSet<Number> hashFollowers = new HashSet<>(followers);
        for (Object object : profiling) {
            Assert.assertTrue(hashFollowers.contains(((JSONObject) object).getLong("id")));
        }
    }
}
