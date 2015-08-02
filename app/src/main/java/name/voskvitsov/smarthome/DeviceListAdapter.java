package name.voskvitsov.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import name.voskvitsov.smarthome.model.AvailableDevices;

/**
 * Created by vden on 02/08/15.
 */
public class DeviceListAdapter extends ArrayAdapter<AvailableDevices.DeviceItem> {
    private final Context context;
    private final List<AvailableDevices.DeviceItem> values;

    public DeviceListAdapter(Context context, List<AvailableDevices.DeviceItem> values) {
        super(context, R.layout.device_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_item, parent, false);

        TextView ipTextView = (TextView) rowView.findViewById(R.id.ipTextView);
        final SeekBar seekBar = (SeekBar) rowView.findViewById(R.id.seekBar);
        final AvailableDevices.DeviceItem deviceItem = getItem(position);

        ipTextView.setText(deviceItem.content);
        seekBar.setProgress(deviceItem.progress);
        seekBar.refreshDrawableState();

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(0);
                seekBar.refreshDrawableState();
                deviceItem.sendDeviceRequest(0);
            }
        });

        ImageView imageViewLow = (ImageView) rowView.findViewById(R.id.imageViewLow);
        imageViewLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(0);
                seekBar.refreshDrawableState();
                deviceItem.sendDeviceRequest(9);
            }
        });

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            deviceItem.sendDeviceRequest(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        return rowView;
    }
}
