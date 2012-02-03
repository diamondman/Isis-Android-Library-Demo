package com.diamondman.android.example.isis.Services;

import java.util.ArrayList;
import java.util.List;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.diamondman.android.isis.IsisService;

public class CustomMessageService extends IsisService {
	public static final int			MSG_MESSAGECHANGED	= 1;
	public static final int			MSG_SETMESSAGE= 2;
	private final List<Messenger>	mClients			= new ArrayList<Messenger>();
	String							customMessage		= "Enter a message to be saved in the service! DERP A LERPA DOO!";

	@Override
	protected void handleMessage(Message msg) {
		switch(msg.what){
		case MSG_SETMESSAGE:
			//no need to synchronize a resource modified by a message queue.
			customMessage=(String)msg.obj;
			notifyClients(MSG_MESSAGECHANGED,0,0,customMessage);
			break;
		}
	}

	private void notifyClients(int what, int arg1, int arg2, Object obj) {
		synchronized (mClients) {
			for (Messenger m : mClients) {
				try {
					m.send(Message.obtain(null, what, arg1, arg2, obj));
				} catch (RemoteException e) {}
			}
		}
	}

	public boolean registerClient(Messenger client) {
		synchronized (mClients) {
			if (client == null) return false;
			if (!mClients.contains(client)) {
				mClients.add(client);
				try {
					client.send(Message.obtain(null, MSG_MESSAGECHANGED, 0, 0, customMessage));
				} catch (RemoteException e) {}
			}
			return true;
		}
	}

	public void unregisterClient(Messenger client) {
		synchronized (mClients) {
			mClients.remove(client);
		}
	}

}
