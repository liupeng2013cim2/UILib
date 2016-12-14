package com.andy.ui;

import com.andy.ui.refreshlayout.RefreshLayout;
import com.andy.ui.refreshlayout.RefreshLayout.LoadListener;
import com.andy.ui.refreshlayout.RefreshLayout.RefreshListener;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {

	MainActivity instance;
	RefreshLayout refreshLayout;

	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this;
		refreshLayout = (RefreshLayout) this.findViewById(R.id.refresh_layout);

		String[] mItems = getResources().getStringArray(R.array.spinnername);
		// 建立Adapter并且绑定数据源
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(instance,
				android.R.layout.simple_spinner_item, mItems);
		refreshLayout.setAdapter(adapter);

		refreshLayout.setRefreshListener(new RefreshListener() {

			@Override
			public void onRefresh() {
				Toast.makeText(instance, "refreshing data...", 1000).show();
				handler.postDelayed(new Runnable() {
					public void run() {
						onComplete();
					}
				}, 2000);
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				refreshLayout.setRefreshState(false);
			}

		});

		refreshLayout.setLoadListener(new LoadListener() {

			@Override
			public void onLoad() {
				// TODO Auto-generated method stub
				Toast.makeText(instance, "loading data...", 1000).show();
				handler.postDelayed(new Runnable() {
					public void run() {
						onComplete();
					}
				}, 2000);
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub

				refreshLayout.setLoadState(false);
			}

		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
