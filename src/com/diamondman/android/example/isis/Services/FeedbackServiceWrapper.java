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
import android.content.Context;
import android.os.Message;

import com.diamondman.android.isis.BindFailedException;
import com.diamondman.android.isis.IsisServiceWrapper;

public class FeedbackServiceWrapper extends IsisServiceWrapper<FeedbackService> {
	int	id	= 0;

	protected FeedbackServiceWrapper(Context context, int id) throws BindFailedException {
		super(context, FeedbackService.class);
		this.id = id;
	}

	protected FeedbackServiceWrapper(Context context) throws BindFailedException {
		super(context, FeedbackService.class);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case FeedbackService.MSG_VIBRATING_BEGIN:
			onVibrationStart();
			break;
		case FeedbackService.MSG_VIBRATING_END:
			onVibrationEnd();
			break;
		case FeedbackService.MSG_VIBEPATTERN_CHANGED:
			currentVibePatternChanged(msg.arg1);
			break;
		}
	}

	protected void currentVibePatternChanged(int arg1) {}

	@Override
	protected void doRegistration() {
		getService().registerClient(getMessenger());
	}

	@Override
	protected void onInitialized() {

	}

	public void VibrationOn() {
		send(FeedbackService.MSG_SET_VIBRATING, 1, 0, null);
	}

	public void VibrationOff() {
		send(FeedbackService.MSG_SET_VIBRATING, 0, 0, null);
	}

	public void VibrationToggle() {
		send(FeedbackService.MSG_TOGGLE_VIBRATING);
	}

	public boolean isVibrating() {
		return getService().isVibrating();
	}

	protected void onVibrationStart() {}

	protected void onVibrationEnd() {}

	@Override
	public final void release() {
		getService().unregisterClient(getMessenger());
		super.release();
	}

	public int getVibePatternCount() {
		return getService().getVibePatternCount();
	}

	public String getVibeName(int index) {
		return getService().getVibeName(index);
	}

	public long[] getVibePattern(int index) {
		return getService().getVibePattern(index);
	}

	public void setCurrentVibePattern(int index) {
		send(FeedbackService.MSG_SET_VIBEPATTERN, index, 0, null);
	}

	public int getCurrentVibePattern() {
		return getService().getSelectedVibePattern();
	}
}
