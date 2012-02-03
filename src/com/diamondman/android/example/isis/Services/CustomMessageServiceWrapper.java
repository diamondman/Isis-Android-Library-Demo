package com.diamondman.android.example.isis.Services;

import android.content.Context;
import android.os.Message;

import com.diamondman.android.isis.IsisServiceWrapper;

public class CustomMessageServiceWrapper extends IsisServiceWrapper<CustomMessageService> {

	public CustomMessageServiceWrapper(Context context) {
		super(context, CustomMessageService.class);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case CustomMessageService.MSG_MESSAGECHANGED:
			onMessageChanged((String) msg.obj);
			break;
		}
	}

	@Override
	protected void doRegistration() {
		getService().registerClient(getMessenger());
	}

	@Override
	protected void onInitialized() {}

	protected void onMessageChanged(String newmsg) {}

	public final void setCustomMessage(String msg) {
		getService().send(CustomMessageService.MSG_SETMESSAGE, 0, 0, msg);
	}
}
