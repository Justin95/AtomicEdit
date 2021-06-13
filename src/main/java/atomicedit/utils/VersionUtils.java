
package atomicedit.utils;

import atomicedit.logging.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Utility code for determining if an updated version of AtomicEdit is available.
 * @author Justin Bonner
 */
public class VersionUtils {
    
    private static final String CHECK_RELEASES_URL = "https://api.github.com/repos/Justin95/AtomicEdit/releases/latest";
    private static final String DOWNLOAD_PAGE_URL = "https://github.com/Justin95/AtomicEdit/releases";
    private static final String VERSION;
    private static final String NEWEST_VERSION;
    
    static {
        VERSION = readVersion();
        NEWEST_VERSION = checkForNewestVersion();
    }
    
    private static String readVersion() {
        try {
            return FileUtils.readResourceFile("/version.txt").trim();
        } catch (IOException e) {
            Logger.error("Cannot read version.", e);
            return null;
        }
    }
    
    public static String getCurrentVersion() {
        return VERSION;
    }
    
    private static String checkForNewestVersion() {
        //https://api.github.com/repos/Justin95/AtomicEdit/releases/34093177
        String responseJsonStr;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(CHECK_RELEASES_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                Logger.error("Bad response checking for new version at " + CHECK_RELEASES_URL);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                responseJsonStr = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            Logger.error("Could not look for newer AtomicEdit version on Github at " + CHECK_RELEASES_URL, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        JsonObject versionJson = new JsonParser().parse(responseJsonStr).getAsJsonObject();
        if (!versionJson.has("tag_name")) {
            Logger.warning("Github API response does not contain `tag_name`.");
            return null;
        }
        String newestTag = versionJson.get("tag_name").getAsString();
        Logger.info("Found newest available AtomicEdit version: " + newestTag);
        return newestTag;
    }
    
    public static String getNewestAvailableVersion() {
        return NEWEST_VERSION;
    }
    
    public static boolean isUpdateAvailable() {
        if (VERSION == null || NEWEST_VERSION == null) {
            return false;
        }
        String[] currVersNo = VERSION.split("\\.");
        String[] remoteVersNo = NEWEST_VERSION.split("\\.");
        for (int i = 0; i < currVersNo.length && i < remoteVersNo.length; i++) {
            try {
                int curr = Integer.parseInt(currVersNo[i]);
                int remote = Integer.parseInt(remoteVersNo[i]);
                if (remote > curr) {
                    return true;
                }
            } catch (NumberFormatException e) {
                Logger.warning("Could not parse version numbers.", e);
                return false;
            }
        }
        return false;
    }
    
    public static void openAtomicEditDownloadPage() {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI url = new URI(DOWNLOAD_PAGE_URL);
            desktop.browse(url);
        } catch (Exception e) {
            Logger.error("Could not open browser.", e);
        }
    }
    
}
