package com.ubtechinc.developer;

public class DeveloperAngle {
   public DeveloperAngle() {
   }

   public void checkData(int[] data) {
      if (data != null && data.length == 20) {
         for(int i = 0; i < data.length; ++i) {
            if (data[i] != 250) {
               if (i != 0 && i != 3) {
                  if (i == 1) {
                     if (data[i] < 50) {
                        data[i] = 50;
                     } else if (data[i] > 210) {
                        data[i] = 210;
                     }
                  } else if (i != 2 && i != 5) {
                     if (i == 4) {
                        if (data[i] < 30) {
                           data[i] = 30;
                        } else if (data[i] > 190) {
                           data[i] = 190;
                        }
                     } else if (i == 6) {
                        if (data[i] < 100) {
                           data[i] = 100;
                        } else if (data[i] > 200) {
                           data[i] = 200;
                        }
                     } else if (i != 7 && i != 12) {
                        if (i == 8) {
                           if (data[i] < 35) {
                              data[i] = 35;
                           } else if (data[i] > 230) {
                              data[i] = 230;
                           }
                        } else if (i == 9) {
                           if (data[i] < 35) {
                              data[i] = 35;
                           } else if (data[i] > 215) {
                              data[i] = 215;
                           }
                        } else if (i == 10) {
                           if (data[i] < 10) {
                              data[i] = 10;
                           } else if (data[i] > 190) {
                              data[i] = 190;
                           }
                        } else if (i == 11) {
                           if (data[i] < 40) {
                              data[i] = 40;
                           } else if (data[i] > 140) {
                              data[i] = 140;
                           }
                        } else if (i == 13) {
                           if (data[i] < 10) {
                              data[i] = 10;
                           } else if (data[i] > 205) {
                              data[i] = 205;
                           }
                        } else if (i == 14) {
                           if (data[i] < 25) {
                              data[i] = 25;
                           } else if (data[i] > 205) {
                              data[i] = 205;
                           }
                        } else if (i == 15) {
                           if (data[i] < 50) {
                              data[i] = 50;
                           } else if (data[i] > 140) {
                              data[i] = 140;
                           }
                        } else if (i != 16 && i != 17) {
                           if (i == 18) {
                              if (data[i] < 75) {
                                 data[i] = 75;
                              } else if (data[i] > 165) {
                                 data[i] = 165;
                              }
                           } else if (i == 19 && data[i] >= 105 && data[i] > 155) {
                              data[i] = 155;
                           }
                        } else if (data[i] < 95) {
                           data[i] = 95;
                        } else if (data[i] > 125) {
                           data[i] = 125;
                        }
                     } else if (data[i] < 20) {
                        data[i] = 20;
                     } else if (data[i] > 220) {
                        data[i] = 220;
                     }
                  } else if (data[i] < 55) {
                     data[i] = 55;
                  } else if (data[i] > 185) {
                     data[i] = 185;
                  }
               } else if (data[i] < 5) {
                  data[i] = 5;
               } else if (data[i] > 235) {
                  data[i] = 235;
               }
            }
         }
      }

   }

   public int checkAngle(byte id, int angle) {
      if (angle == 250) {
         return angle;
      } else {
         if (id != 0 && id != 3) {
            if (id == 1) {
               if (angle < 50) {
                  angle = 50;
               } else if (angle > 210) {
                  angle = 210;
               }
            } else if (id != 2 && id != 5) {
               if (id == 4) {
                  if (angle < 30) {
                     angle = 30;
                  } else if (angle > 190) {
                     angle = 190;
                  }
               } else if (id == 6) {
                  if (angle < 100) {
                     angle = 100;
                  } else if (angle > 200) {
                     angle = 200;
                  }
               } else if (id != 7 && id != 12) {
                  if (id == 8) {
                     if (angle < 35) {
                        angle = 35;
                     } else if (angle > 230) {
                        angle = 230;
                     }
                  } else if (id == 9) {
                     if (angle < 35) {
                        angle = 35;
                     } else if (angle > 215) {
                        angle = 215;
                     }
                  } else if (id == 10) {
                     if (angle < 10) {
                        angle = 10;
                     } else if (angle > 190) {
                        angle = 190;
                     }
                  } else if (id == 11) {
                     if (angle < 40) {
                        angle = 40;
                     } else if (angle > 140) {
                        angle = 140;
                     }
                  } else if (id == 13) {
                     if (angle < 10) {
                        angle = 10;
                     } else if (angle > 205) {
                        angle = 205;
                     }
                  } else if (id == 14) {
                     if (angle < 25) {
                        angle = 25;
                     } else if (angle > 205) {
                        angle = 205;
                     }
                  } else if (id == 15) {
                     if (angle < 50) {
                        angle = 50;
                     } else if (angle > 140) {
                        angle = 140;
                     }
                  } else if (id != 16 && id != 17) {
                     if (id == 18) {
                        if (angle < 75) {
                           angle = 75;
                        } else if (angle > 165) {
                           angle = 165;
                        }
                     } else if (id == 19 && angle >= 105 && angle > 155) {
                        angle = 155;
                     }
                  } else if (angle < 95) {
                     angle = 95;
                  } else if (angle > 125) {
                     angle = 125;
                  }
               } else if (angle < 20) {
                  angle = 20;
               } else if (angle > 220) {
                  angle = 220;
               }
            } else if (angle < 55) {
               angle = 55;
            } else if (angle > 185) {
               angle = 185;
            }
         } else if (angle < 5) {
            angle = 5;
         } else if (angle > 235) {
            angle = 235;
         }

         return angle;
      }
   }
}
