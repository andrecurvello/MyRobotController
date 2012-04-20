package com.zerokol.myrobotcontroller;

import java.util.ArrayList;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BluetoothManageActivity extends Activity {
	// Constants
	private final int MANAGE_BLUETOOTH = 0;
	private final int SCAN_DEVICES = 1;
	// Variables
	private ListView bluetoothDevicesListView;
	private Resources myResources;
	private BluetoothAdapter bluetooth;
	private String dStarted = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
	private String dFinished = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
	private ArrayList<String> bluetoothDeviceNames = new ArrayList<String>();
	private ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, bluetoothDeviceNames);
	private ArrayList<BluetoothDevice> remoteDevices = new ArrayList<BluetoothDevice>();
	@SuppressWarnings("unused")
	private AdapterContextMenuInfo acmi = null;

	private BroadcastReceiver discoveryResult = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			BluetoothDevice remoteDevice;
			remoteDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			remoteDevices.add(remoteDevice);
			String remoteDeviceName = intent
					.getStringExtra(BluetoothDevice.EXTRA_NAME);
			if (remoteDeviceName == "") {
				remoteDeviceName = remoteDevice.getName();
			}
			Toast.makeText(getApplicationContext(),
					R.string.bluetooth_discovered_lab + remoteDeviceName,
					Toast.LENGTH_SHORT).show();
			bluetoothDeviceNames.add(0, remoteDeviceName);
			aa.notifyDataSetChanged();
		}
	};

	private BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (dStarted.equals(intent.getAction())) {
				// Discovery has started.
				Toast.makeText(getApplicationContext(),
						R.string.bluetooth_dis_start_lab, Toast.LENGTH_SHORT)
						.show();
			} else if (dFinished.equals(intent.getAction())) {
				// Discovery has completed.
				Toast.makeText(getApplicationContext(),
						R.string.bluetooth_dis_comp_lab, Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bluetooth_manage);

		myResources = getResources();

		bluetoothDevicesListView = (ListView) findViewById(R.id.bluetoothDevicesListView);

		bluetooth = BluetoothAdapter.getDefaultAdapter();

		String toastText;
		if (bluetooth.isEnabled()) {
			toastText = ((String) myResources
					.getText(R.string.bluetooth_on_lab))
					+ " "
					+ bluetooth.getName() + " : " + bluetooth.getAddress();
		} else {
			toastText = (String) myResources
					.getText(R.string.bluetooth_off_lab);
		}
		Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

		registerReceiver(discoveryMonitor, new IntentFilter(dStarted));
		registerReceiver(discoveryMonitor, new IntentFilter(dFinished));

		registerReceiver(discoveryResult, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));

		bluetoothDevicesListView.setAdapter(aa);

		bluetoothDevicesListView
				.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						acmi = (AdapterContextMenuInfo) menuInfo;
						menu.add(0, 0, 0, R.string.select_device_lab);
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MANAGE_BLUETOOTH, 0,
				myResources.getText(R.string.turn_bluetooth_lab)).setIcon(
				R.drawable.bluetooth);
		menu.add(0, SCAN_DEVICES, 0,
				myResources.getText(R.string.scan_devices_lab)).setIcon(
				R.drawable.scan_robot);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MANAGE_BLUETOOTH:
			if (bluetooth.isEnabled()) {
				bluetooth.disable();
			} else {
				bluetooth.enable();
			}
			return true;
		case SCAN_DEVICES:
			if (!bluetooth.isDiscovering()) {
				// Limpando a linta de estações encontradas previamente
				remoteDevices.clear();
				bluetooth.startDiscovery();
			} else {
				bluetooth.cancelDiscovery();
			}
			break;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		// String name = bluetoothDeviceNames.get(acmi.position);

		return true;
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(discoveryMonitor);
		unregisterReceiver(discoveryResult);
		super.onDestroy();
	}
}
