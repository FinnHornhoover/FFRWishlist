/*
 * MIT License
 *
 * Copyright (c) 2018 FinnHornhoover
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * Copies and derivative works of the Software which contain significant portions
 * of the data contained within the files that are licensed under the CC-BY-NC 4.0
 * license (specified in LICENSE-CC-BY-NC file) must also satisfy the conditions
 * of the CC-BY-NC 4.0 license, specifically non-commercial use and attribution.
 * Therefore, such copies or derivative works may not be used for commercial
 * purposes, and must include acceptable attribution.
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package finnhh.ffrwishlist.web;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import finnhh.ffrwishlist.model.database.DatabaseManager;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;

public class WebUpdater {
    private static final String JSON_URL = "https://dl.dropboxusercontent.com/s/4y8psrl66n9qvnh/ffrw.json?dl=1";

    private int targetVersion;
    private Queue<UpdateSegment> updateSegmentQueue;

    public WebUpdater() {
        updateSegmentQueue = new ArrayDeque<>();
    }

    public void connectAndFetchData(int currentDatabaseVersion) throws IOException, HTTPException {
        URL url = new URL(JSON_URL);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        int responseCode = urlConnection.getResponseCode();

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream())) {
                JsonObject updateJsonObject = Json.parse(inputStreamReader).asObject();

                targetVersion = updateJsonObject.get("version").asInt();

                updateJsonObject.get("update_segments").asArray().values().stream()
                        .map(s -> new UpdateSegment(s.asObject()))
                        .filter(s -> s.getVersionTag() > currentDatabaseVersion && s.getVersionTag() <= targetVersion)
                        .sorted(Comparator.comparingInt(UpdateSegment::getVersionTag))
                        .forEachOrdered(updateSegmentQueue::add);
            }
        } else {
            throw new HTTPException(responseCode);
        }
    }

    public void updateOnce(DatabaseManager databaseManager) {
        if (!updateSegmentQueue.isEmpty()) {
            UpdateSegment updateSegment = updateSegmentQueue.remove();

            databaseManager.rawUpdate(updateSegment.getUpdateQueue());
            databaseManager.setDatabaseVersion(updateSegment.getVersionTag());
        }
    }

    public int getUpdatesRemaining() {
        return updateSegmentQueue.size();
    }

    private static class UpdateSegment {
        private int versionTag;
        private Queue<String> updateQueue;

        UpdateSegment(JsonObject jsonObject) {
            this.versionTag = jsonObject.get("version_tag").asInt();
            this.updateQueue = new ArrayDeque<>();

            jsonObject.get("update_strings").asArray().values().stream()
                    .map(JsonValue::asString)
                    .forEachOrdered(updateQueue::add);
        }

        int getVersionTag() {
            return versionTag;
        }

        Queue<String> getUpdateQueue() {
            return updateQueue;
        }
    }
}
