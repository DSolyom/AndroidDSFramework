/*
	Copyright 2015 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package ds.framework.v4.app.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ds.framework.v4.app.DSFragment;
import ds.framework.v4.common.Debug;
import ds.framework.v4.template.Template;

/**
 * Created by DS on 4/19/2015.
 */
public class DialogYesNoFragment extends DSFragment {

    private CharSequence mTitle;
    private CharSequence mQuestion;
    private CharSequence mPositiveText;
    private Runnable mPositiveOnClick;
    private CharSequence mNegativeText;
    private Runnable mNegativeOnClick;

    public DialogYesNoFragment() {
        super(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_FRAME & STYLE_NO_TITLE, ds.framework.v4.R.style.Theme_Transparent);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // hack for fullscreen
        Dialog d = getDialog();
        if (d!=null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }

        if (mNegativeOnClick == null || mPositiveOnClick == null) {
            Debug.logI("DialogYesNoFragment", "No click runnable for either negative or positive button - dismissing!");
            dismissAllowingStateLoss();
        }
    }

    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        final View rootView = inflater.inflate(ds.framework.v4.R.layout.x_dialog_yes_no, container, false);
        return rootView;
    }

    /**
     * set dialog's title
     * 
     * @param title
     * @return
     */
    public DialogYesNoFragment setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    /**
     * set dialog's question
     * 
     * @param question
     * @return
     */
    public DialogYesNoFragment setQuestion(CharSequence question) {
        mQuestion = question;
        return this;
    }

    /**
     * set dialog's positive button text and behavior
     *
     * @param positiveText
     * @param onClick
     * @return
     */
    public DialogYesNoFragment setPositiveButton(CharSequence positiveText, Runnable onClick) {
        mPositiveText = positiveText;
        mPositiveOnClick = onClick;
        
        return this;
    }

    /**
     * set dialog's negative button text and behavior
     *
     * @param negativeText
     * @param onClick
     * @return
     */
    public DialogYesNoFragment setNegativeButton(CharSequence negativeText, Runnable onClick) {
        mNegativeText = negativeText;
        mNegativeOnClick = onClick;

        return this;
    }

    @Override
    public void display() {
        super.display();

        mTemplate.fill(ds.framework.v4.R.id.tv_dialog_title, mTitle);
        mTemplate.fill(ds.framework.v4.R.id.tv_dialog_title, mTitle, Template.VISIBLE);
        mTemplate.fill(ds.framework.v4.R.id.tv_dialog_question, mQuestion);
        mTemplate.fill(ds.framework.v4.R.id.tv_dialog_question, mQuestion, Template.VISIBLE);
        mTemplate.fill(ds.framework.v4.R.id.btn_positive, mPositiveText);
        mTemplate.fill(ds.framework.v4.R.id.btn_negative, mNegativeText);
        mTemplate.fill(ds.framework.v4.R.id.btn_positive, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPositiveOnClick != null) {
                    mPositiveOnClick.run();
                }
            }
        });
        mTemplate.fill(ds.framework.v4.R.id.btn_negative, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mNegativeOnClick != null) {
                    mNegativeOnClick.run();
                }
            }
        });
    }
}
