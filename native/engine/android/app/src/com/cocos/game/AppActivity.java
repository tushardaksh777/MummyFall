/****************************************************************************
Copyright (c) 2015-2016 Chukong Technologies Inc.
Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.cocos.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cocos.lib.JsbBridge;
import com.cocos.service.SDKWrapper;
import com.cocos.lib.CocosActivity;
import com.gametathya.mummyJump.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.gametathya.gamestreet.Base;

import java.util.Arrays;
import java.util.Base64;

public class AppActivity extends CocosActivity {
    private RewardedAd mRewardedAd;
    private InterstitialAd mInterstitialAd;
    private static Base base = Base.getInstance();
    private static AppActivity app=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);
        app = this;
        loadRewardedAds();
        loadInterstitialAd();

        //sendToNative => onScript CallBack
        JsbBridge.setCallback(new JsbBridge.ICallback() {
            @Override
            public void onScript(String score, String url) {
                base.addUserSession(app);
                // Check usr
                // Open Ad
                Log.d("JSB","Callback :"+score+" Url"+url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        base.submitHighScore(app,Integer.parseInt(score));
                        showRewardedAds();
                    }
                });
                //JsbBridge.sendToScript("HighScore");
            }
        });
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("56061b2b-188d-4fe2-823f-841df729df69")).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this,new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }});

    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.shared().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.shared().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.shared().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.shared().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.shared().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.shared().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.shared().onStop();
    }

    @Override
    public void onBackPressed() {
        SDKWrapper.shared().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.shared().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.shared().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.shared().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.shared().onStart();
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        SDKWrapper.shared().onLowMemory();
        super.onLowMemory();
    }
    private void showadCallback(){
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            String TAG="Ad";
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.");
                mRewardedAd = null;
                loadRewardedAds();

                JsbBridge.sendToScript("HighScore");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.");
                mRewardedAd = null;
                loadRewardedAds();
                showInterstitialAd();
                //JsbBridge.sendToScript("HighScore");
            }

        });
    }
    private void showRewardedAds(){
        if (mRewardedAd != null) {
            Activity activityContext = this;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {

                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d("Ad", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                }
            });
        } else {
            showInterstitialAd();
            Log.d("Ad", "The rewarded ad wasn't ready yet.");
            loadRewardedAds();
        }
    }
    private void loadRewardedAds(){
        AdRequest adRequest = new AdRequest.Builder().build();
//test -	ca-app-pub-3940256099942544/5224354917
        //offical - ca-app-pub-2758519241237532/7046838971
        RewardedAd.load(this, "ca-app-pub-2758519241237532/7046838971",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("Ad", loadAdError.toString());
                        Log.d("Ad", loadAdError.getMessage());
                        Log.d("Ad", loadAdError.getResponseInfo().toString());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                       Log.d("Ad", "Ad was loaded.");
                    }
                });
    }
    private void loadInterstitialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

//        InterstitialAd.load(this, "ca-app-pub-2758519241237532~3193292243", adRequest,
//                new InterstitialAdLoadCallback() {
//
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        // The mInterstitialAd reference will be null until
//                        // an ad is loaded.
//                        mInterstitialAd = interstitialAd;
//                        Log.i("InterstitialAdAd", "onAdLoaded");
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error
//                        Log.d("InterstitialAdAd", loadAdError.toString());
//                        mInterstitialAd = null;
//                        loadInterstitialAd();
//                        //JsbBridge.sendToScript("HighScore");
//                    }
//                });
    }
    private void showInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("InterstitialAdAd", "The interstitial ad wasn't ready yet.");
            JsbBridge.sendToScript("HighScore");
            loadRewardedAds();
            loadInterstitialAd();
        }
    }

    private void callbackInterstitialAd(){
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d("InterstitialAdAd", "Ad dismissed fullscreen content.");
                mInterstitialAd = null;
                JsbBridge.sendToScript("HighScore");

            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e("InterstitialAdAd", "Ad failed to show fullscreen content.");
                mInterstitialAd = null;
                JsbBridge.sendToScript("HighScore");
            }

        });
    }
}
