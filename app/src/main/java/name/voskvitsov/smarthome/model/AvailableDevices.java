package name.voskvitsov.smarthome.model;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class AvailableDevices {

    public static List<DeviceItem> ITEMS = new ArrayList<>();
    public static Map<String, DeviceItem> ITEM_MAP = new HashMap<>();

    public static void addItem(DeviceItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.ip, item);
    }

    public static void removeItemByIP(String domain) {
        for (DeviceItem item : ITEMS) {
            if (item.domain.equals(domain)) {
                ITEMS.remove(item);
                break;
            }
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DeviceItem {
        public String ip;
        public String content;
        public String domain;
        public int progress;

        private final RequestQueue queue;
        private final ResponseListener responseListener;

        public DeviceItem(String ip, String domain,
                          RequestQueue queue, ResponseListener responseListener) {
            this.ip = ip;
            this.content = "domain: " + domain + ", ip: " + ip;
            this.domain = domain;
            this.queue = queue;
            this.responseListener = responseListener;

            this.progress = 0;
        }

        public void sendDeviceRequest() {
            String url = "http:/" + ip + "/";
            sendDeviceRequest(url);
        }

        public void sendDeviceRequest(int level) {
            String url = "http:/" + ip + "/?b=" + level;
            sendDeviceRequest(url);
        }

        private void sendDeviceRequest(String url) {
            JsonObjectRequest stringRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    "{}",
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                content = "domain: " + domain + ", ip: " + ip;
                                progress = response.getInt("b");
                                progress = progress >= 9 ? 0: progress;
                                responseListener.onDeviceResponse(progress);

                            } catch (JSONException e) {
                                content = e.getMessage();
                                responseListener.onDeviceResponse(0);
                                responseListener.onDeviceError(content);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                            public void onErrorResponse (VolleyError error){
                            content = "Device not found";
                            responseListener.onDeviceResponse(0);
                            responseListener.onDeviceError(content);
                        }
                    }
            );

            queue.add(stringRequest);
        }


        @Override
        public String toString() {
            return ip + ", " + content;
        }
    }
}
