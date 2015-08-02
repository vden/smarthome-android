package name.voskvitsov.smarthome.model;

/**
 * Created by vden on 02/08/15.
 */
public interface ResponseListener {
    void onDeviceResponse(int currentLevel);
    void onDeviceError(String reason);
}
