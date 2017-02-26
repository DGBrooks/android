package com.twofours.surespot.gifs;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twofours.surespot.R;
import com.twofours.surespot.SurespotConfiguration;
import com.twofours.surespot.SurespotLog;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.network.MainThreadCallbackWrapper;
import com.twofours.surespot.network.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class GifSearchView extends RelativeLayout {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GifSearchAdapter mGifsAdapter;
    private IAsyncCallback<String> mCallback;
    private ProgressBar mProgressBar;


    public GifSearchView(Context context) {
        super(context);
    }

    public GifSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GifSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        mRecyclerView = (RecyclerView) findViewById(R.id.rvGifs);
        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mProgressBar = (ProgressBar) findViewById(R.id.gif_progress_bar);

        RecyclerView keywordView = (RecyclerView) findViewById(R.id.rvGifKeywords);
        keywordView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        ArrayList<String> keywords = new ArrayList<>();
        keywords.add("HIGH FIVE");
        keywords.add("CLAPPING");
        keywords.add("THUMBS UP");
        keywords.add("NO");
        keywords.add("YES");
        keywords.add("SHRUG");
        keywords.add("MIC DROP");
        keywords.add("SORRY");
        keywords.add("CHEERS");
        keywords.add("THANK YOU");
        keywords.add("WINK");
        keywords.add("ANGRY");
        keywords.add("NERVOUS");
        keywords.add("DUH");
        keywords.add("OOPS");
        keywords.add("HUNGRY");
        keywords.add("HUGS");
        keywords.add("WOW");
        keywords.add("BORED");
        keywords.add("GOODNIGHT");
        keywords.add("AWKWARD");
        keywords.add("AWW");
        keywords.add("PLEASE");
        keywords.add("YIKES");
        keywords.add("OMG");
        keywords.add("BYE");
        keywords.add("WAITING");
        keywords.add("EYEROLL");
        keywords.add("IDK");
        keywords.add("KITTIES");
        keywords.add("PUPPIES");
        keywords.add("LOSER");
        keywords.add("COLD");
        keywords.add("PARTY");
        keywords.add("AGREE");
        keywords.add("DANCE");
        keywords.add("EXCUSE ME");
        keywords.add("WHAT");
        keywords.add("STOP");
        keywords.add("SLEEPY");
        keywords.add("CREEP");
        keywords.add("JK");
        keywords.add("SCARED");
        keywords.add("CHILL OUT");
        keywords.add("MISS YOU");
        keywords.add("DONE");

        keywordView.setAdapter(new GifKeywordAdapter(this.getContext(), keywords, new IAsyncCallback<String>() {
            @Override
            public void handleResponse(String result) {
                searchGifs(result);
            }
        }));

        EditText etGifSearch = (EditText) findViewById(R.id.etGifSearch);
        etGifSearch.setFilters(new InputFilter[]{new InputFilter.LengthFilter(SurespotConfiguration.MAX_SEARCH_LENGTH)});
        etGifSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchGifs(v.getText().toString());
                }

                return handled;
            }
        });
    }

    private void searchGifs(String terms) {
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        if (mGifsAdapter != null) {
            mGifsAdapter.clearGifs();
        }

        NetworkManager.getNetworkController(GifSearchView.this.getContext()).searchGiphy(terms, new MainThreadCallbackWrapper(new MainThreadCallbackWrapper.MainThreadCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onResponse(Call call, Response response, String responseString) throws IOException {
                if (mGifsAdapter == null) {
                    mGifsAdapter = new GifSearchAdapter(GifSearchView.this.getContext(), getGifUrls(responseString), mCallback);
                    mRecyclerView.setAdapter(mGifsAdapter);
                    mGifsAdapter.notifyDataSetChanged();
                }
                else {
                    mGifsAdapter.setGifs(getGifUrls(responseString));
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

            }
        }));
    }

    public void setCallback(IAsyncCallback<String> callback) {
        mCallback = callback;


    }

    private List<GifDetails> getGifUrls(String result) {
        ArrayList<GifDetails> gifURLs = new ArrayList<>();
        try {

            JSONObject json = new JSONObject(result);
            JSONArray data = json.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {
                JSONObject orig = data.getJSONObject(i).getJSONObject("images").getJSONObject("fixed_height");
                String url = orig.getString("url");
                if (url.toLowerCase().startsWith("https")) {
                    int height = orig.getInt("height");
                    int width = orig.getInt("width");

                    gifURLs.add(new GifDetails(url, width, height));
                }
            }
        }
        catch (JSONException e) {
            SurespotLog.e(TAG, e, "getGifUrls JSON error");
        }
        return gifURLs;
    }
}
