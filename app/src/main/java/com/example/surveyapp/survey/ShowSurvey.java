package com.example.surveyapp.survey;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.surveyapp.R;
import com.example.surveyapp.adapters.CheckBoxRecyViewAdapter;
import com.example.surveyapp.adapters.NpsGridViewAdapter;
import com.example.surveyapp.adapters.NpsOptionsAdapter;
import com.example.surveyapp.adapters.RadioBtnAdapter;
import com.example.surveyapp.listeners.CheckBoxClickListener;
import com.example.surveyapp.listeners.NpsOptionClickListener;
import com.example.surveyapp.listeners.RadioClickListener;
import com.example.surveyapp.listeners.VolleyResponseListener;
import com.example.surveyapp.models.QuestionResponse;
import com.example.surveyapp.network.VolleyService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShowSurvey implements VolleyResponseListener {
    private View layoutView;
    private RelativeLayout layout;
    private RadioBtnAdapter adapter;
    private NpsOptionsAdapter npsOptionsAdapter;
    private NpsGridViewAdapter npsGridViewAdapter;
    private Dialog dialog;
    private CheckBoxRecyViewAdapter checkBoxRecyViewAdapter;
//    private static ShowSurvey instance;
    private MaterialButton  nextQuestion;
    private  AppCompatImageView closeDialogBtn;

    private int currentIndx=0;
    private  Context context;
    private  JSONArray surveyQuestions;
    private  JSONObject surveyUi;
    private JSONObject surveyInfo;
    VolleyService volleyService;
     String uuid;
    QuestionResponse currentQuestionResponse;
    public ShowSurvey(Context ctx, String url){
        this.context=ctx;
        Log.d("survey", "showing on :"+((Activity)context).getTitle());
        currentQuestionResponse=new QuestionResponse();
        this.uuid=UUID.randomUUID().toString();
        volleyService=new VolleyService();
        volleyService.callVolley(ctx,url ,this::onResponseReceived );

    }
    private void trackWithAlium() {
        volleyService.loadRequestWithVolley(context, getLoadReqURL());
        Log.d("POST",getLoadReqURL() );
    }
    private Map<String, String> getParams(){
        Map<String, String> params = new HashMap<>();
        params.put("srvid", "1201");
        params.put("srvtpid", "6");
        params.put("srvLng", "1");
        params.put("srvldid", uuid+"ppup1710923706208srv");
        params.put("srvpt", "dashboard");
        params.put("ua", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        params.put("vstid", uuid);
        params.put("ran", "1710923706208");
        params.put("orgId", "1038");
        params.put("custSystemId", "NA");
        params.put("custId", "1066");
        params.put("custEmail", "NA");
        params.put("custMobile", "NA");
        params.put("Content-Type", "application/x-www-form-urlencoded");
        return params;
    }
    private String getLoadReqURL(){
        try {
            String BASE_URL="https://tracker.alium.co.in/tracker?";
            String surveyId="srvid="+surveyInfo.getString("surveyId")+"&";
            String srvpid="srvtpid=6&";
            String srvLng="srvLng=1&";
            String vstid="vstid="+uuid+"&"  ;
            String srvldid="srvldid="+uuid+"ppup"+ new Date().getTime()+"srv"+"&";
            String srvpt="srvpt="+((Activity)context).getTitle().toString()+"&";
            String ua= "ua=Mozilla/5.0%20(Windows%20NT%2010.0%3B%20Win64%3B%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/122.0.0.0%20Safari/537.36&";
            String ran="ran="+new Date().getTime()+"&";
            String orgId="orgId="+surveyInfo.getString("orgId")+"&";
            String cutomerData= "custSystemId=NA&custId="+surveyInfo.getString("customerId")+"&custEmail=NA&custMobile=NA";
            String post_string=BASE_URL+surveyId+srvpid+srvLng+srvldid+srvpt+ua+vstid+ran+orgId+
                    cutomerData;
          return post_string;
        } catch (JSONException e) {

            throw new RuntimeException(e);
        }
    }
    public void show(){
        dialog=new Dialog(context);
        Activity activity=(Activity)context;
        Log.d("showSurvey", activity.getTitle().toString());
        nextQuestion=layoutView.findViewById(R.id.btn_next);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  currentIndx++;
                  handleNextQuestion();
                  nextQuestion.setEnabled(false);

            }
        });
        closeDialogBtn=layoutView.findViewById(R.id.close_dialog_btn);
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitSurvey();
            }
        });
        if(surveyQuestions.length()>0 && currentIndx==0) showCurrentQuestion();
        dialog.setContentView(this.layoutView);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        trackWithAlium();
    }

    @Override
    public void onResponseReceived(JSONObject json) {
        Log.d("response from", json.toString());
        String checkURL=((Activity)context).getTitle().toString();
        surveyResponse(json, checkURL);

    }

    private void surveyResponse(JSONObject response, String checkURL) {
        Log.d("Target2", checkURL);
        Iterator<String> keys = response.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject jsonObject = response.getJSONObject(key);
                JSONObject ppupsrvObject = jsonObject.getJSONObject("ppupsrv");
                String urlValue = ppupsrvObject.getString("url");
                Log.d("Target2", "Key: " + key + ", URL: " + urlValue);

                if (checkURL.equals(urlValue)){
                    Log.e("True","True");
                    Uri uri=Uri.parse(urlValue);
                    Log.d("path", uri.getPath());
//        surveyResponse(json, checkURL);
                    List<String> segments= uri.getPathSegments();
                    for(int i=0; i<segments.size(); i++){
                        if(segments.get(i).equalsIgnoreCase(((Activity)context).getTitle().toString())){
                            Log.d("path-segment",segments.get(i));
                            loadSurvey(key);
                        }
                    }

                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadSurvey(String key) {
        String surURL="https://assets.alium.co.in/cmmn/cqsjn/cqsjn_1038_" +key + ".json";
        VolleyResponseListener volleyResponseListener2=new VolleyResponseListener() {
            @Override
            public void onResponseReceived(JSONObject json) {
                Log.d("survey loaded", json.toString());
                try {
                    surveyQuestions=json.getJSONArray("surveyQuestions");
                    if(json.has("surveyUI")){
                        surveyUi=json.getJSONObject("surveyUI");
                    }
                    if(json.has("surveyInfo")){
                        surveyInfo=json.getJSONObject("surveyInfo");
                    }
                    layoutView= LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
//                    Drawable dialogBg=context.getDrawable(R.drawable.dialog_bg);
//
//                    layoutView.setBackgroundResource(R.drawable.dialog_bg);

                    layout= layoutView.findViewById(R.id.dialog_layout_content);
                    GradientDrawable gradientDrawable=(GradientDrawable)  layoutView
                            .findViewById(R.id.dialog_layout).getBackground();

                    gradientDrawable.setCornerRadius((int)(15* Resources.getSystem().getDisplayMetrics().density));
                    gradientDrawable.setColor(Color.WHITE);
//                    if(surveyUi!=null)layoutView.setBackgroundColor(Color.argb(255,
//                            Integer.parseInt(surveyUi.getString("rBackground")),
//                            Integer.parseInt(surveyUi.getString("gBackground")),
//                            Integer.parseInt(surveyUi.getString("bBackground"))));
                    if(surveyUi!=null)gradientDrawable.setColor(Color.argb(255,
                            Integer.parseInt(surveyUi.getString("rBackground")),
                            Integer.parseInt(surveyUi.getString("gBackground")),
                            Integer.parseInt(surveyUi.getString("bBackground"))));
                    if(surveyUi!=null) gradientDrawable.setStroke((int)(2* Resources.getSystem()
                                    .getDisplayMetrics().density),
                            Color.argb(255,
                                    Integer.parseInt(surveyUi.getString("rBorder")),
                                    Integer.parseInt(surveyUi.getString("gBorder")),
                                    Integer.parseInt(surveyUi.getString("bBorder"))));
                    show();
//                    trackWithAlium();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        volleyService.callVolley(context, surURL,volleyResponseListener2 );

    }
    private void handleNextQuestion(){
        String url=getLoadReqURL()+"&"+"qusid="+currentQuestionResponse.getQuestionId()+"&"+
                "qusrs="+currentQuestionResponse.getQuestionResponse()+"&"+
                "restp="+currentQuestionResponse.getResponseType();
//        Map params=getParams();
//        params.put("qusid",currentQuestionResponse.getQuestionId());
//        params.put("qusrs",currentQuestionResponse.getQuestionResponse());
//        params.put("restp",currentQuestionResponse.getResponseType());
        volleyService.loadRequestWithVolley(context,url );
        if(surveyQuestions.length()>0 && currentIndx< surveyQuestions.length()){

            this.layout.removeAllViews();
            showCurrentQuestion();
        }else if(currentIndx==surveyQuestions.length()){
            //show thank you msg
            Toast.makeText(context, "Your response has been submitted!!", Toast.LENGTH_LONG).show();
            submitSurvey();
        }
    }
    private void submitSurvey(){
        dialog.dismiss();
    }


    private void showCurrentQuestion(){
        try {
            currentQuestionResponse.setQuestionId(surveyQuestions.getJSONObject(currentIndx)
                    .getInt("id"));
            currentQuestionResponse.setResponseType(surveyQuestions
                    .getJSONObject(currentIndx).getString("responseType"));

                if(surveyQuestions.getJSONObject(currentIndx).getString("responseType").equals("1")){

                    View longtextQues= LayoutInflater.from(context).inflate(R.layout.long_text_ques,
                            null);
                    AppCompatTextView quest=longtextQues.findViewById(R.id.question);
                    quest.setText(surveyQuestions.getJSONObject(currentIndx).getString("question"));
                    TextInputLayout textInputLayout=longtextQues.findViewById(R.id.text_input_layout);
                    TextInputEditText input=longtextQues.findViewById(R.id.text_input_edit_text);
                    input.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            currentQuestionResponse.setQuestionResponse(input.getText().toString().trim()
                                    .replace(" ", "%20"));
                            if(currentQuestionResponse.getQuestionResponse().length()>0){
                                nextQuestion.setEnabled(true);
                            }else{
                                nextQuestion.setEnabled(false);
                            }
                            Log.d("input", currentQuestionResponse.getQuestionResponse());
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    if(surveyUi!=null)quest.setTextColor(Color.argb(255,
                            Integer.parseInt(surveyUi.getString("rText")),
                            Integer.parseInt(surveyUi.getString("gText")),
                            Integer.parseInt(surveyUi.getString("bText"))));
                    this.layout.addView(longtextQues);
                }

            else if(surveyQuestions.getJSONObject(currentIndx).getString("responseType")
                        .equals("2")){

                JSONArray responseOptJSON=surveyQuestions.getJSONObject(currentIndx)
                        .getJSONArray("responseOptions");
                List<String> responseOptions=new ArrayList<>();
                for (int i=0; i<responseOptJSON.length(); i++){
                    responseOptions.add(responseOptJSON.getString(i));
                }
                View radioQues= LayoutInflater.from(context).inflate(R.layout.radio_ques, null);
                AppCompatTextView quest=radioQues.findViewById(R.id.question);
                quest.setText(surveyQuestions.getJSONObject(currentIndx).getString("question"));
                    if(surveyUi!=null)quest.setTextColor(Color.argb(255,Integer
                                    .parseInt(surveyUi.getString("rText")),
                            Integer.parseInt(surveyUi.getString("gText")),
                            Integer.parseInt(surveyUi.getString("bText"))));

                    RecyclerView radioBtnRecyView=radioQues.findViewById(R.id.radio_btn_rec_view);
                radioBtnRecyView.setLayoutManager(new LinearLayoutManager(context));

                RadioClickListener radioClickListener=new RadioClickListener() {
                    @Override
                    public void onClick(int position) {
                        radioBtnRecyView.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.updateCheckedItem(position);
                                if(currentQuestionResponse.getQuestionResponse().length()>0){
                                    nextQuestion.setEnabled(true);
                                }else{
                                    nextQuestion.setEnabled(false);
                                }
                            }
                        });
                    }
                };
                this.adapter=new RadioBtnAdapter(responseOptions,radioClickListener, currentQuestionResponse );
                radioBtnRecyView.setAdapter(adapter);

                this.layout.addView(radioQues);
            }
            else
                if(surveyQuestions.getJSONObject(currentIndx).getString("responseType").equals("3")){
                    JSONArray responseOptJSON=surveyQuestions.getJSONObject(currentIndx).getJSONArray("responseOptions");
                    List<String> responseOptions=new ArrayList<>();
                    for (int i=0; i<responseOptJSON.length(); i++){
                        responseOptions.add(responseOptJSON.getString(i));
                    }
                    View checkBoxQues= LayoutInflater.from(context).inflate(R.layout.checkbox_type_ques, null);
                    AppCompatTextView quest=checkBoxQues.findViewById(R.id.question);
                    quest.setText(surveyQuestions.getJSONObject(currentIndx).getString("question"));
                    if(surveyUi!=null)quest.setTextColor(Color.argb(255,Integer.parseInt(surveyUi.getString("rText")),
                            Integer.parseInt(surveyUi.getString("gText")), Integer.parseInt(surveyUi.getString("bText"))));

                    RecyclerView recyclerView=checkBoxQues.findViewById(R.id.checkbox_recy_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    CheckBoxClickListener checkBoxClickListener=new CheckBoxClickListener() {
                        @Override
                        public void onClick(int position, boolean selected) {
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    checkBoxRecyViewAdapter.updateCheckedItem(position, selected);
                                    if(currentQuestionResponse.getQuestionResponse().length()>0){
                                        nextQuestion.setEnabled(true);
                                    }else{
                                        nextQuestion.setEnabled(false);
                                    }
                                }
                            });
                        }
                    };
                    checkBoxRecyViewAdapter=new CheckBoxRecyViewAdapter(responseOptions, checkBoxClickListener, currentQuestionResponse);
                    recyclerView.setAdapter(checkBoxRecyViewAdapter);
                    this.layout.addView(checkBoxQues);
                }else  if(surveyQuestions.getJSONObject(currentIndx).getString("responseType").equals("4")){
                    View npsQues= LayoutInflater.from(context).inflate(R.layout.nps_ques, null);
                    AppCompatTextView quest=npsQues.findViewById(R.id.question);
                    quest.setText(surveyQuestions.getJSONObject(currentIndx).getString("question"));
                    if(surveyUi!=null)quest.setTextColor(Color.argb(255,Integer.parseInt(surveyUi.getString("rText")),
                            Integer.parseInt(surveyUi.getString("gText")), Integer.parseInt(surveyUi.getString("bText"))));

                    GridView npsRecView=npsQues.findViewById(R.id.nps_recy_view);
                    NpsOptionClickListener listener=new NpsOptionClickListener() {
                        @Override
                        public void onClick(int position) {
                            npsRecView.post(new Runnable() {
                                @Override
                                public void run() {
                                    npsGridViewAdapter.updatedSelectedOption(position);
                                    if( currentQuestionResponse.getQuestionResponse().length()>0){
                                        nextQuestion.setEnabled(true);
                                        Log.d("int", ""+Integer.parseInt(currentQuestionResponse.getQuestionResponse()));

                                    }else{
                                        nextQuestion.setEnabled(false);
                                    }
//                                    npsOptionsAdapter.updatedSelectedOption(position);
                                }
                            });
                        }
                    };
                    npsOptionsAdapter=new NpsOptionsAdapter(listener);
                    npsGridViewAdapter=new NpsGridViewAdapter(context, listener, currentQuestionResponse);
                    npsRecView.setAdapter( npsGridViewAdapter);
                    this.layout.addView(npsQues);

                }
            Log.d("surveyQuestion", "id: "+currentQuestionResponse.getQuestionId()
                    +" type: "+currentQuestionResponse.getResponseType());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


//    public static void createSurvey(Context ctx, JSONObject json){
//        if(instance==null){
//            instance=new ShowSurvey();
//        }
//        try {
//            surveyQuestions=json.getJSONArray("surveyQuestions");
//            surveyUi=json.getJSONObject("surveyUI");
//            context=ctx;
//
//            instance.layoutView= LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
//            instance.layout= instance.layoutView.findViewById(R.id.dialog_layout_content);
//            instance.layoutView.setBackgroundColor(Color.argb(255,Integer.parseInt(surveyUi.getString("rBackground")),
//                    Integer.parseInt(surveyUi.getString("gBackground")), Integer.parseInt(surveyUi.getString("bBackground"))));
//            instance.show( );
//
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//    }


    private void updateSurveyUi(){

    }

}
