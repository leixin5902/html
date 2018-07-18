package cc.lotuscard;

import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

public class LotusCardDemoActivity extends Activity {
	private LotusCardDriver mLotusCardDriver;

	private UsbManager m_UsbManager = null;
	private UsbDevice m_LotusCardDevice = null;
	private UsbInterface m_LotusCardInterface = null;
	private UsbDeviceConnection m_LotusCardDeviceConnection = null;
	private final int m_nVID = 65535;
	private final int m_nPID = 53;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 设置USB读写回调 串口可以不用此操作
		SetUsbCallBack();

		 LotusCardParam tLotusCardParam = new LotusCardParam();
		mLotusCardDriver = new LotusCardDriver();
       // int nDeviceHandle = mLotusCardDriver.OpenDevice("/dev/ttyTCC1", 0,
	//	0, false);
		// int nDeviceHandle = mLotusCardDriver.OpenDevice("/dev/ttyS3", 0, 0, false);

		int nDeviceHandle = mLotusCardDriver.OpenDevice("", 0, 0,true);

		if (nDeviceHandle > 0) {
			testIcCardReader(nDeviceHandle);
			mLotusCardDriver.CloseDevice(nDeviceHandle);
		}
	}

	private void testIcCardReader(int nDeviceHandle) {
		boolean bResult = false;
		int nRequestType;
		LotusCardParam tLotusCardParam1 = new LotusCardParam();
		bResult = mLotusCardDriver.Beep(nDeviceHandle, 10);
		bResult = mLotusCardDriver.Beep(nDeviceHandle, 10);
		if (!bResult)
			return;
		nRequestType = LotusCardDriver.RT_NOT_HALT;
		//以下3个函数可以用GetCardNo替代
//		bResult = mLotusCardDriver.Request(nDeviceHandle, nRequestType,
//				tLotusCardParam1);
//		if (!bResult)
//			return;
//		bResult = mLotusCardDriver.Anticoll(nDeviceHandle, tLotusCardParam1);
//		if (!bResult)
//			return;
//		bResult = mLotusCardDriver.Select(nDeviceHandle, tLotusCardParam1);
//		if (!bResult)
//			return;
		bResult = mLotusCardDriver.GetCardNo(nDeviceHandle, nRequestType,
				tLotusCardParam1);
		if (!bResult)
			return;	
		tLotusCardParam1.arrKeys[0] = (byte) 0xff;
		tLotusCardParam1.arrKeys[1] = (byte) 0xff;
		tLotusCardParam1.arrKeys[2] = (byte) 0xff;
		tLotusCardParam1.arrKeys[3] = (byte) 0xff;
		tLotusCardParam1.arrKeys[4] = (byte) 0xff;
		tLotusCardParam1.arrKeys[5] = (byte) 0xff;
		tLotusCardParam1.nKeysSize = 6;
		bResult = mLotusCardDriver.LoadKey(nDeviceHandle, LotusCardDriver.AM_A,
				0, tLotusCardParam1);
		if (!bResult)
			return;
		bResult = mLotusCardDriver.Authentication(nDeviceHandle,
				LotusCardDriver.AM_A, 0, tLotusCardParam1);
		if (!bResult)
			return;
		bResult = mLotusCardDriver.Read(nDeviceHandle, 1, tLotusCardParam1);
		if (!bResult)
			return;

		tLotusCardParam1.arrBuffer[0] = (byte) 0x10;
		tLotusCardParam1.arrBuffer[1] = (byte) 0x01;
		tLotusCardParam1.arrBuffer[2] = (byte) 0x02;
		tLotusCardParam1.arrBuffer[3] = (byte) 0x03;
		tLotusCardParam1.arrBuffer[4] = (byte) 0x04;
		tLotusCardParam1.arrBuffer[5] = (byte) 0x05;
		tLotusCardParam1.arrBuffer[6] = (byte) 0x06;
		tLotusCardParam1.arrBuffer[7] = (byte) 0x07;
		tLotusCardParam1.arrBuffer[8] = (byte) 0x08;
		tLotusCardParam1.arrBuffer[9] = (byte) 0x09;
		tLotusCardParam1.arrBuffer[10] = (byte) 0x0a;
		tLotusCardParam1.arrBuffer[11] = (byte) 0x0b;
		tLotusCardParam1.arrBuffer[12] = (byte) 0x0c;
		tLotusCardParam1.arrBuffer[13] = (byte) 0x0d;
		tLotusCardParam1.arrBuffer[14] = (byte) 0x0e;
		tLotusCardParam1.arrBuffer[15] = (byte) 0x0f;
		tLotusCardParam1.nBufferSize = 16;
		bResult = mLotusCardDriver.Write(nDeviceHandle, 1, tLotusCardParam1);
		if (!bResult)
			return;
	}

	private void SetUsbCallBack() {
		PendingIntent pendingIntent;
		pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		m_UsbManager = (UsbManager) getSystemService(USB_SERVICE);
		if (null == m_UsbManager)
			return;

		HashMap<String, UsbDevice> deviceList = m_UsbManager.getDeviceList();
		if (!deviceList.isEmpty()) {
			for (UsbDevice device : deviceList.values()) {
				if ((m_nVID == device.getVendorId())
						&& (m_nPID == device.getProductId())) {
					m_LotusCardDevice = device;
					break;
				}
			}
		}
		if (null == m_LotusCardDevice)
			return;
		m_LotusCardInterface = m_LotusCardDevice.getInterface(0);
		if (null == m_LotusCardInterface)
			return;
		if (false == m_UsbManager.hasPermission(m_LotusCardDevice)) {
			m_UsbManager.requestPermission(m_LotusCardDevice, pendingIntent);
		}
		UsbDeviceConnection conn = null;
		if (m_UsbManager.hasPermission(m_LotusCardDevice)) {
			conn = m_UsbManager.openDevice(m_LotusCardDevice);
		}

		if (null == conn)
			return;

		if (conn.claimInterface(m_LotusCardInterface, true)) {
			m_LotusCardDeviceConnection = conn;
		} else {
			conn.close();
		}
		if (null == m_LotusCardDeviceConnection)
			return;
		// 把上面获取的对性设置到接口中用于回调操作
		LotusCardDriver.m_UsbDeviceConnection = m_LotusCardDeviceConnection;
		if (m_LotusCardInterface.getEndpoint(1) != null) {
			LotusCardDriver.m_OutEndpoint = m_LotusCardInterface.getEndpoint(1);
		}
		if (m_LotusCardInterface.getEndpoint(0) != null) {
			LotusCardDriver.m_InEndpoint = m_LotusCardInterface.getEndpoint(0);
		}

	}

}