package com.diamondman.android.example.isis.Services;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;

import com.diamondman.android.isis.IsisService;

public class FeedbackService extends IsisService {
	public static final int					MSG_SET_VIBRATING		= 1;
	public static final int					MSG_TOGGLE_VIBRATING	= 2;
	public static final int					MSG_VIBRATING_BEGIN		= 3;
	public static final int					MSG_VIBRATING_END		= 4;
	public static final int					MSG_SET_VIBEPATTERN		= 5;
	public static final int					MSG_VIBEPATTERN_CHANGED	= 6;
	private volatile boolean				vibrating				= false;
	private final List<Messenger>			mClients				= new ArrayList<Messenger>();
	private static final List<VibePattern>	vibePatterns			= new ArrayList<VibePattern>();
	private Integer							selectedVibePattern		= 0;
	Vibrator								vb;
	private static VibePattern[]			defaultPatterns			= new VibePattern[] { new VibePattern("Alternating Pulse", new long[] { 1000, 1000 }),
			new VibePattern("Fast Pulse", new long[] { 200, 200 }), new VibePattern("Super Fast Pulse", new long[] { 50, 50 }) };

	static {
		for (VibePattern pattern : defaultPatterns)
			vibePatterns.add(pattern);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SET_VIBRATING:
			if (msg.arg1 == 1 && !vibrating) {
				startVibration();
			} else if (msg.arg1 == 0 && vibrating) {
				endVibration();
			}
			break;
		case MSG_TOGGLE_VIBRATING:
			if (!vibrating) {
				startVibration();
			} else {
				endVibration();
			}
			break;
		case MSG_SET_VIBEPATTERN:
			synchronized (selectedVibePattern) {
				selectedVibePattern = msg.arg1;
				notifyClients(MSG_VIBEPATTERN_CHANGED, selectedVibePattern, 0, null);
			}
			break;
		}
	}

	private void startVibration() {
		vibrating = true;
		vb.vibrate(vibePatterns.get(selectedVibePattern).pattern, 0);
		notifyClients(MSG_VIBRATING_BEGIN, 0, 0, null);
	}

	private void endVibration() {
		vibrating = false;
		vb.cancel();
		notifyClients(MSG_VIBRATING_END, 0, 0, null);
	}

	public boolean isVibrating() {
		return vibrating;
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
					client.send(Message.obtain(null, (vibrating) ? MSG_VIBRATING_BEGIN : MSG_VIBRATING_END, 0, 0, null));
					client.send(Message.obtain(null, MSG_VIBEPATTERN_CHANGED, selectedVibePattern, 0, null));
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

	public static class VibePattern {
		public String	name;
		public long[]	pattern;

		public VibePattern(String name, long[] pattern) {
			this.name = name;
			this.pattern = pattern;
		}
	}

	public int getVibePatternCount() {
		synchronized (vibePatterns) {
			return vibePatterns.size();
		}
	}

	public String getVibeName(int index) {
		synchronized (vibePatterns) {
			return vibePatterns.get(index).name;
		}
	}

	public long[] getVibePattern(int index) {
		synchronized (vibePatterns) {
			return vibePatterns.get(index).pattern;
		}
	}

	public Integer getSelectedVibePattern() {
		synchronized (selectedVibePattern) {
			return selectedVibePattern;
		}
	}
}
