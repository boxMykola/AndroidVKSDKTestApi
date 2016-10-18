package com.vk.vktestapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.io.IOException;

public class ApiCallActivity extends ActionBarActivity {

    private VKRequest myRequest;

    private static final String FRAGMENT_TAG = "response_view";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_call);

		if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), FRAGMENT_TAG)
					.commit();
			processRequestIfRequired();
		}
	}

    private PlaceholderFragment getFragment() {
        return (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
    private void processRequestIfRequired() {
        VKRequest request = null;

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("request")) {
            long requestId = getIntent().getExtras().getLong("request");
            request = VKRequest.getRegisteredRequest(requestId);
            if (request != null)
                request.unregisterObject();
        }

        if (request == null) return;
        myRequest = request;
        request.executeWithListener(mRequestListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("response", getFragment().textView.getText());
        if (myRequest != null) {
            outState.putLong("request", myRequest.registerObject());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CharSequence response = savedInstanceState.getCharSequence("response");
        if (response != null) {
            getFragment().textView.setText(response);
        }

        long requestId = savedInstanceState.getLong("request");
        myRequest = VKRequest.getRegisteredRequest(requestId);
        if (myRequest != null) {
            myRequest.unregisterObject();
            myRequest.setRequestListener(mRequestListener);
        }
    }
	protected void setResponseText(String text) {
		PlaceholderFragment fragment = getFragment();
		if (fragment != null && fragment.textView != null) {
			fragment.textView.setText(text);
		}
	}


	VKRequestListener mRequestListener = new VKRequestListener() {
		@Override
		public void onComplete(VKResponse response) {
//			for(int i = 0; i<((VKList<VKApiUser>)response.parsedModel).size(); i++) {
//				VKApiUser vkApiUser = ((VKList<VKApiUser>) response.parsedModel).get(i);
//				setResponseText(""+vkApiUser.first_name+"\n"
//				+vkApiUser.last_name+"\n");
			final StringBuilder sb = new StringBuilder();
			for(int i = 0; i<((VKList<VKApiPost>)response.parsedModel).size(); i++) {
				VKApiPost vkApiPost = ((VKList<VKApiPost>) response.parsedModel).get(i);
				VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID,vkApiPost.from_id,VKApiConst.FIELDS,
						"first_name,last_name,sex,city,country"));
				request.secure = false;
				request.useSystemLanguage = true;

				request.executeWithListener(new VKRequest.VKRequestListener() {
												@Override
												public void onComplete(VKResponse response){
													super.onComplete(response);
													for(int i = 0; i<((VKList<VKApiUser>)response.parsedModel).size(); i++) {
														VKApiUser vkApiUser = ((VKList<VKApiUser>) response.parsedModel).get(i);

														sb.append("====================================\n");
														sb.append(vkApiUser.first_name);
														sb.append("\n");
														sb.append(vkApiUser.);
														System.out.println(vkApiUser.last_name);
													}
												}
											});

				sb.append(vkApiPost.text);
				sb.append("\n");
				sb.append("\n");


			}
			setResponseText(sb.toString());
			//setResponseText(response.json.toString());
		}

		@Override
		public void onError(VKError error) {
			setResponseText(error.toString());
		}

		@Override
		public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
		                       long bytesTotal) {
			// you can show progress of the request if you want
		}

		@Override
		public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
			getFragment().textView.append(
					String.format("Attempt %d/%d failed\n", attemptNumber, totalAttempts));
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myRequest.cancel();
		Log.d(VKSdk.SDK_TAG, "On destroy");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		public TextView textView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_api_call, container, false);
			textView = (TextView) v.findViewById(R.id.response);
			return v;
		}
	}
}