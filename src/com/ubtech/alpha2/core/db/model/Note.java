package com.ubtech.alpha2.core.db.model;

import java.util.Date;

public class Note {
   private Long id;
   private String text;
   private String comment;
   private Date date;

   public Note() {
   }

   public Note(Long id) {
      this.id = id;
   }

   public Note(Long id, String text, String comment, Date date) {
      this.id = id;
      this.text = text;
      this.comment = comment;
      this.date = date;
   }

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public Date getDate() {
      return this.date;
   }

   public void setDate(Date date) {
      this.date = date;
   }
}
