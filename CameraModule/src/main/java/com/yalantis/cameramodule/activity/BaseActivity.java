/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Zillow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.yalantis.cameramodule.activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


/**包含ActionBar处理的base类*/
public abstract class BaseActivity extends AppCompatActivity {

    protected Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                return BaseActivity.this.handleMessage(msg);
            }
        });
    }

    protected void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    protected boolean handleMessage(Message msg) {
        return false;
    }

    protected void showActionBar() {
        if (isSupportActionBar()) {
            getActionBarCustom().show();
        }
    }

    protected void hideActionBar() {
        if (isSupportActionBar()) {
            getActionBarCustom().hide();
        }
    }

    public void showBack() {
        if(isSupportActionBar()){
            getActionBarCustom().setDisplayShowHomeEnabled(false);
            getActionBarCustom().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void hideBack() {
        if(isSupportActionBar()){
            getActionBarCustom().setDisplayShowHomeEnabled(true);
            getActionBarCustom().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    protected boolean isSupportActionBar(){
        ActionBar actionBar = null;
        try {
            actionBar =  getActionBarCustom();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actionBar!=null;
    }
    protected ActionBar getActionBarCustom(){
        return getSupportActionBar();
    }


}