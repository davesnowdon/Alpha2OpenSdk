package com.ubtechinc.alpha2ctrlapp.network.model;

public class JokeModel {
   private String jokeTitle;
   private String jokeType;
   private String jokeContext;
   private String jokeLanguage;

   public JokeModel() {
   }

   public String getJokeTitle() {
      return this.jokeTitle;
   }

   public void setJokeTitle(String jokeTitle) {
      this.jokeTitle = jokeTitle;
   }

   public String getJokeType() {
      return this.jokeType;
   }

   public void setJokeType(String jokeType) {
      this.jokeType = jokeType;
   }

   public String getJokeContext() {
      return this.jokeContext;
   }

   public void setJokeContext(String jokeContext) {
      this.jokeContext = jokeContext;
   }

   public String getJokeLanguage() {
      return this.jokeLanguage;
   }

   public void setJokeLanguage(String jokeLanguage) {
      this.jokeLanguage = jokeLanguage;
   }
}
