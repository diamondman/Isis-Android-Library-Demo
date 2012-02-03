package com.diamondman.android.example.isis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.diamondman.android.example.isis.Services.CentralService;
import com.diamondman.android.example.isis.Services.CentralServiceWrapper;
import com.diamondman.android.example.isis.Services.CustomMessageServiceWrapper;
import com.diamondman.android.example.isis.Services.FeedbackServiceWrapper;
import com.diamondman.android.isis.IsisActivity;

public class IsisDemoActivity extends IsisActivity {
	public CentralServiceWrapper		central;
	public FeedbackServiceWrapper		feedback;
	public CustomMessageServiceWrapper	message;
	private boolean						justUpdatedTextBox	= false;
	private boolean						justSetNewMessage	= false;
	private int							color;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		color = getIntent().getIntExtra("ColorID", 0);
		View container = findViewById(R.id.mainContainer);
		switch (color) {
		case 0:
			container.setBackgroundColor(Color.RED);
			break;
		case 1:
			container.setBackgroundColor(Color.GREEN);
			break;
		case 2:
			container.setBackgroundColor(Color.BLUE);
			break;
		}

		setMenu(R.menu.demomenu);
		startService(new Intent(this, CentralService.class));
		central = new CentralServiceWrapper(this) {
			protected void applicationTerminationRequestReceived() {
				finish();
			}
		};

		feedback = new FeedbackServiceWrapper(this, color) {
			boolean	justSetSelectedIndex	= true;
			boolean	justUpdatedVibePattern	= false;

			@Override
			protected void onVibrationStart() {
				runOnUiThread(new Runnable() {
					public void run() {
						TextView tv = (TextView) findViewById(R.id.vibration_status);
						tv.setBackgroundColor(Color.GREEN);
						tv.setTextColor(Color.RED);
						tv.setText(R.string.vib_on);
						findViewById(R.id.btn_vibon).setEnabled(false);
						findViewById(R.id.btn_viboff).setEnabled(true);
					}
				});
			}

			@Override
			protected void onVibrationEnd() {
				runOnUiThread(new Runnable() {
					public void run() {
						TextView tv = (TextView) findViewById(R.id.vibration_status);
						tv.setBackgroundColor(Color.RED);
						tv.setTextColor(Color.GREEN);
						tv.setText(R.string.vib_off);
						findViewById(R.id.btn_vibon).setEnabled(true);
						findViewById(R.id.btn_viboff).setEnabled(false);
					}
				});
			}

			protected void currentVibePatternChanged(final int patternindex) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (!justUpdatedVibePattern) {
							Log.d("BUZZSHIT", "UPDATE UI from "+color+" to value "+patternindex);
							Spinner vibmodes = ((Spinner) findViewById(R.id.vibratepattern));
							vibmodes.setSelection(patternindex);
							justSetSelectedIndex = true;
						}
						justUpdatedVibePattern = false;
					}
				});
			}

			@Override
			protected void onInitialized() {
				Spinner vibmodes = ((Spinner) findViewById(R.id.vibratepattern));
				vibmodes.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
						if (!justSetSelectedIndex) {
							Log.d("BUZZSHIT", "DATA SENT from "+color+" to value "+position);
							feedback.setCurrentVibePattern(position);
							justUpdatedVibePattern = true;
						}
						justSetSelectedIndex = false;
					}

					public void onNothingSelected(AdapterView<?> arg0) {}
				});
				vibmodes.setAdapter(new FeedbackServiceWrapperAdapter());
			}
		};

		message = new CustomMessageServiceWrapper(this) {
			@Override
			protected void onMessageChanged(final String newmsg) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (!justSetNewMessage) {
							justUpdatedTextBox = true;
							((EditText) findViewById(R.id.messagebox)).setText(newmsg);
						}
						justSetNewMessage = false;
					}
				});
			}

			@Override
			protected void onInitialized() {
				runOnUiThread(new Runnable() {
					public void run() {
						((EditText) findViewById(R.id.messagebox)).addTextChangedListener(new TextWatcher() {
							public void onTextChanged(CharSequence s, int start, int before, int count) {}

							public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

							public void afterTextChanged(Editable s) {
								if (!justUpdatedTextBox) {
									message.setCustomMessage(s.toString());
									justSetNewMessage = true;
								}
								justUpdatedTextBox = false;
							}
						});
					}
				});
			}
		};

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.quit_menu_item) {
			central.requestApplicationTermination();
			return true;
		} else if (item.getItemId() == R.id.create_new_activity) {
			Intent startNewActivityIntent = new Intent(this, this.getClass());
			int newColor = (color + 1) > 2 ? 0 : color + 1;
			startNewActivityIntent.putExtra("ColorID", newColor);
			startActivity(startNewActivityIntent);
			return true;
		}
		return false;
	}

	public void vib_on(View v) {
		feedback.VibrationOn();
	}

	public void vib_toggle(View v) {
		feedback.VibrationToggle();
	}

	public void vib_off(View v) {
		feedback.VibrationOff();
	}

	class FeedbackServiceWrapperAdapter extends BaseAdapter {

		public int getCount() {
			return feedback.getVibePatternCount();
		}

		public Object getItem(int position) {
			return feedback.getVibePattern(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View item = null;
			if (convertView == null) {
				LayoutInflater li = getLayoutInflater();
				item = li.inflate(R.layout.dropdown_item, null);
			} else
				item = convertView;

			((TextView) item.findViewById(R.id.value)).setText(feedback.getVibeName(position));
			return item;
		}

	}
}