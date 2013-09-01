package org.thomasheller.android.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;

public class Notes extends Activity implements TextWatcher {

	private static final String PREFERENCES_FILE = "notes_storage";
	private static final String PREFERENCES_KEY = "user_notes";

	private EditText editText;
	private SharedPreferences storage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_notes);
		editText = (EditText) findViewById(R.id.editText);
		editText.addTextChangedListener(this);
		storage = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
		restore();
		handleIntent(getIntent());
	}
	
	private void restore() {
		String notes = storage.getString(PREFERENCES_KEY, "");
		editText.setText(notes);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
			String text = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (editText.length() > 0) {
				editText.append("\n");
			}
			editText.append(text);
			editText.setSelection(editText.getText().length());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_notes, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				share();
				return true;
			case R.id.menu_delete:
				delete();
				return true;
			case R.id.menu_bullet:
				bullet();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void share() {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, editText.getText());
		String title = getResources().getString(R.string.share_intent_title);
		Intent.createChooser(intent, title);
		startActivity(intent);
	}

	private void delete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.delete_title);
		builder.setMessage(R.string.delete_message);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editText.setText("");
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.create().show();
	}

	private void bullet() {
		String text = editText.getText().toString();
		String insert = getResources().getString(R.string.bullet_template);
		int cursorPosition = editText.getSelectionEnd();
		if (cursorPosition == 0) {
			String newText = insert + "\n" + text;
			editText.setText(newText);
			editText.setSelection(insert.length());
		} else {
			int nextNewline = text.indexOf('\n', cursorPosition);
			if (nextNewline == -1) {
				nextNewline = text.length();
			}
			String textBefore = text.substring(0, nextNewline);
			String textAfter = text.substring(nextNewline);
			String newText = textBefore + "\n" + insert + textAfter;
			editText.setText(newText);
			editText.setSelection(nextNewline + insert.length() + 1);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		String text = editText.getText().toString();
		SharedPreferences.Editor editor = storage.edit();
		editor.putString(PREFERENCES_KEY, text);
		editor.commit();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
