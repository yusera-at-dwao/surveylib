package com.example.surveylib.models;

import java.util.List;


public class Question {
    int id;
    String question;
    String responseType;
    List<String> responseOptions;
    List<String> conditionMapping;
}
