package com.diamondman.android.example.isis.Services;

//Copyright (c) 2012, Jessy Diamond Exum
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//
//Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the 
//documentation and/or other materials provided with the distribution.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
//THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
//CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
//PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
