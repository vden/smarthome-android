package name.voskvitsov.smarthome;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import name.voskvitsov.smarthome.model.AvailableDevices;
import name.voskvitsov.smarthome.model.ResponseListener;

public class MainActivity extends AppCompatActivity
        implements DeviceItemFragment.OnFragmentInteractionListener {

    final static String domain = "lamp.local";
    // final static String domain = "192.168.1.216";
    final static String TAG = "SmartHome:Main";

    private String baseUrl = domain;
    private RequestQueue requestQueue;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager mNsdManager;

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success " + service);
                if (service.getServiceName().contains("lamp")){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost " + service);

                AvailableDevices.removeItemByIP(service.getServiceName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyCurrentDeviceListAdapter();
                    }
                });
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();

                String ip = host + ":" + port;

                if (AvailableDevices.ITEM_MAP.containsKey(ip))
                    return;

                AvailableDevices.DeviceItem deviceItem =
                        new AvailableDevices.DeviceItem(ip, serviceInfo.getServiceName(),
                                requestQueue,
                                new ResponseListener() {
                                    @Override
                                    public void onDeviceResponse(int currentLevel) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyCurrentDeviceListAdapter();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onDeviceError(String reason) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyCurrentDeviceListAdapter();
                                            }
                                        });
                                    }
                                });

                AvailableDevices.addItem(deviceItem);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyCurrentDeviceListAdapter();
                    }
                });
            }
        };
    }

    private void notifyCurrentDeviceListAdapter() {
        DeviceItemFragment fragment = (DeviceItemFragment) getFragmentManager().findFragmentByTag("device-list");
        if (fragment != null) {
            DeviceListAdapter adapter = (DeviceListAdapter)fragment.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
            requestQueue = Volley.newRequestQueue(this);

            // Create a new Fragment to be placed in the activity layout
            DeviceItemFragment fragment = new DeviceItemFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, "device-list").commit();

            initializeResolveListener();
            initializeDiscoveryListener();

            mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.i(TAG, "Fragment interaction with " + id);
    }
}
