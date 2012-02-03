package com.diamondman.android.example.isis.Services;

import android.content.Context;
import android.os.Message;

import com.diamondman.android.isis.IsisServiceWrapper;

public abstract class CentralServiceWrapper extends IsisServiceWrapper<CentralService> {

	public CentralServiceWrapper(Context context) {
		super(context, CentralService.class);
	}

	@Override
	protected final void handleMessage(Message msg) {
		switch (msg.what) {
		case CentralService.MSG_QUITAPPLICATION:
			applicationTerminationRequestReceived();
			break;
		}
	}

	@Override
	protected final void doRegistration() {
		getService().registerClient(getMessenger());
	}

	@Override
	public final void release() {
		getService().unregisterClient(getMessenger());
		super.release();
	}

	@Override
	protected void onInitialized() {}
	
	protected abstract void applicationTerminationRequestReceived();
	
	public final void requestApplicationTermination(){
		send(CentralService.MSG_QUITAPPLICATION);
	}

}
