package com.iap.test1.iap1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

public class MainActivity extends AppCompatActivity {

    private Button buyButton;
    private Button refreshButton;
    private static final String TAG = "*********";
    IabHelper mHelper;
    static final String ITEM_SKU = "android.test.purchased";
    static final int IAP_REQ_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buyButton = (Button)findViewById(R.id.buyButton);
        refreshButton = (Button)findViewById(R.id.refreshButton);
        refreshButton.setEnabled(false);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkvaU4SKfglKKZyaMLMB6kchq2D232yuFyAGc4mQp10xAuBHmkb3mEbjGaiKlF8Lukbox30uG+aThurMBptEOGV5VG0Q2WCLd1unXDubzSsKPBcfx90rEpSnAXUiPeqAXVFyz3GQJcKFXNSFpo+9WRcG9OA/MfLUjGBJoqc0oT8vJdmH9tTlJjI5MtswX4uH0y+chMHi/Z5jsLg2jG2sQG8rmAJ4VyR7hFXcaOEaqkXKOtzFzwKRiU6oqJDDD0S4VMvek/9GEZQI6euwbhcpxuoI1uXiFSLGnoGu3YxiM524qPh0mSFqhZjgnn6Y1n2R/GK9pvi6T7DbCDEJMsopQOwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
           public void onIabSetupFinished(IabResult result){
               if (!result.isSuccess()) {
                   Log.i(TAG, "In-app Billing setup failed: " + result);
               } else {
                   Log.i(TAG, "In-app Billing is set up OK");
               }
           }
        });
    }

    public void refreshClicked (View view){
        refreshButton.setEnabled(false);
        buyButton.setEnabled(true);
    }

    public void buyClick(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, IAP_REQ_CODE, mPurchaseFinishedListener, "USD 0.99 for 60 Crystal ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(TAG, "onActivityResult : outer");
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            Log.i(TAG, "onActivityResult : inner");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase){
            if (result.isFailure()) {
                // Handle error
                Log.i(TAG, "onIabPurchaseFinished : result.isFailure");
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                Log.i(TAG, "onIabPurchaseFinished : purchase.getSku().equals(ITEM_SKU)");
                consumeItem();
                buyButton.setEnabled(false);
            }
        }
    };

    public void consumeItem() {
        Log.i(TAG, "consumeItem");
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
                Log.i(TAG, "onQueryInventoryFinished : result.isFailure");
            } else {
                Log.i(TAG, "onQueryInventoryFinished : success");
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                Log.i(TAG, "onConsumeFinished : result.isSuccess");
                refreshButton.setEnabled(true);
            } else {
                // handle error
                Log.i(TAG, "onConsumeFinished : failure");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }
}
