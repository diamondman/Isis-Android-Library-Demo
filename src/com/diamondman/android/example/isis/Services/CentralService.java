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

/**
 * This is the usual solution I do for multiple services. This service only
 * handles loading the other services and distributing termination signals to
 * them. This Service is started by StartService() which makes it stay in memory
 * for all but the most extreme circumstances (Android terminates the
 * Application from low ram). This Service then becomes a client to the other
 * services thus loading them all into memory before others need the services,
 * assuming you start the central service immediately (which you should). After
 * this is done, all services the CentralService is bound to stay in memory for
 * as long as CentralService is in memory, which is basically forever. To make
 * an escape point, I like to make this service have a termination message that
 * notifies all clients of the termination, then terminates itself. This works
 * well because the clients can close their service wrappers with time to get
 * some small operation through the wrapper, then the CentralService disconnects
 * from the services which makes them have no clients registered to them
 * resulting in their termination, and everything is clean and happy and ready
 * to start up again as soon as a client starts CentralService.
 * 
 * @author Jessy Diamond Exum
 * 
 */
public class CentralService extends IsisService {
	public final static int				MSG_QUITAPPLICATION	= 1;
	private final List<Messenger>		mClients			= new ArrayList<Messenger>();
	private FeedbackServiceWrapper		feedbacksrv;
	private CustomMessageServiceWrapper	msgsrv;

	@Override
	public void onCreate() {
		super.onCreate();
		feedbacksrv = new FeedbackServiceWrapper(this) {};
		msgsrv = new CustomMessageServiceWrapper(this) {};
	}

	@Override
	protected final void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_QUITAPPLICATION:
			notifyClientsOfTermSignalAndQuit();
			break;
		}
	}

	private void notifyClientsOfTermSignalAndQuit() {
		synchronized (mClients) {
			for (Messenger m : mClients) {
				try {
					m.send(Message.obtain(null, MSG_QUITAPPLICATION, 0, 0, null));
				} catch (RemoteException e) {}
			}
		}
		stopSelf();
	}

	public boolean registerClient(Messenger client) {
		synchronized (mClients) {
			if (client == null) return false;
			if (!mClients.contains(client)) mClients.add(client);
			return true;
		}
	}

	public void unregisterClient(Messenger client) {
		synchronized (mClients) {
			mClients.remove(client);
		}
	}

	/**
	 * When you create a Service that consumes other Services with
	 * ServiceWrappers, you need to manually call destroy on these services
	 * since the reflective automatic dispose methods don't exist for Services
	 * (yet?).
	 */
	@Override
	public void onDestroy() {
		feedbacksrv.release();
		msgsrv.release();
		super.onDestroy();
	}
}
