package com.ubtechinc.alpha2ctrlapp.network.action;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.ubtechinc.alpha2ctrlapp.network.JsonUtils;
import com.ubtechinc.alpha2ctrlapp.network.common.HttpPost;
import com.ubtechinc.alpha2ctrlapp.network.model.request.UploadFileRequest;
import com.ubtechinc.alpha2ctrlapp.network.model.response.ActionNameResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.AuthenticationResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.CheckRegiResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.DevelopSerialNumberRsp;
import com.ubtechinc.alpha2ctrlapp.network.model.response.FindMasterModel;
import com.ubtechinc.alpha2ctrlapp.network.model.response.FindMasterRespon;
import com.ubtechinc.alpha2ctrlapp.network.model.response.GetCipherResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.RegisterResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.UploadControllerInfoResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.UploadPhotoResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.ValidateFriendshipResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.VerificationResponse;
import org.apache.http.HttpException;

public class UserAction extends BaseAction {
   private Context mContext;
   private Handler mHanler;
   private ClientAuthorizeListener listener;
   private String filePath = null;

   public UserAction(Context context, ClientAuthorizeListener listener) {
      super(context);
      this.mContext = context;
      this.listener = listener;
   }

   public Object doInBackground(int requestCode) throws HttpException {
      switch(requestCode) {
      case 10001:
         return HttpPost.getJsonByPost("", this.getParamerObj(), true);
      case 20001:
         return HttpPost.getJsonByPost("system/verifycode", this.getParamerObj(), false);
      case 20002:
         return HttpPost.getJsonByPost("user/register", this.getParamerObj(), false);
      case 20003:
         return HttpPost.getJsonByPost("user/robotfind", this.getParamerObj(), false);
      case 20004:
         return HttpPost.getJsonByPost("system/getValue", this.getParamerObj(), false);
      case 20005:
         return HttpPost.getJsonByPost("relation/updateControl", this.getParamerObj(), false);
      case 20006:
         return HttpPost.getJsonByPost("relation/find", this.getParamerObj(), false);
      case 20007:
         UploadFileRequest obj = (UploadFileRequest)this.getParamerObj();
         return HttpPost.uploadFileByPost("system/uploadPhoto", this.getParamerObj(), false, 1, this.filePath, obj.getSerialNumber());
      case 20008:
         return HttpPost.getJsonByPost("equipment/developSerial", this.getParamerObj(), false);
      case 20009:
         return HttpPost.getJsonByPost("relation/find", this.getParamerObj(), false);
      case 20010:
         return HttpPost.getJsonByPost("action/detailById", this.getParamerObj(), false);
      case 20030:
         return HttpPost.getJsonByPost("version/check", this.getParamerObj(), false);
      case 20031:
         return HttpPost.getJsonByPost("version/verify", this.getParamerObj(), false);
      case 30001:
         return HttpPost.getJsonByPost("joke/find", this.getParamerObj(), false);
      default:
         return null;
      }
   }

   public void onSuccess(int requestCode, Object result) {
      if (result == null) {
         this.listener.onResult(-1, "-1");
         Log.i("zdy", "没有响应消息");
      } else {
         switch(requestCode) {
         case 10001:
            AuthenticationResponse res = (AuthenticationResponse)JsonUtils.getInstance().jsonToBean(result.toString(), AuthenticationResponse.class);
            if (res == null) {
               this.listener.onResult(0, "size = 0");
               return;
            }

            if (res.isStatus()) {
               if ("8888".equals(res.getInfo())) {
                  this.listener.onResult(2, "vip");
               } else {
                  this.listener.onResult(1, "success");
               }
            } else {
               this.listener.onResult(0, "fail");
            }
            break;
         case 20001:
            VerificationResponse vRes = (VerificationResponse)JsonUtils.getInstance().jsonToBean(result.toString(), VerificationResponse.class);
            if (vRes != null) {
               if (vRes.getModels() == null && vRes.getModels().equals("")) {
                  this.listener.onResult(0, "size = 0");
               } else {
                  this.listener.onResult(1, vRes.getModels());
               }
            } else {
               this.listener.onResult(0, "size = 0");
            }
            break;
         case 20002:
            RegisterResponse registResponse = (RegisterResponse)JsonUtils.getInstance().jsonToBean(result.toString(), RegisterResponse.class);
            if (registResponse.getInfo().length() >= 4 && isNumeric(registResponse.getInfo().substring(0, 4))) {
               int code = Integer.valueOf(registResponse.getInfo().substring(0, 4));
               if (Integer.valueOf(registResponse.getInfo().substring(0, 4)) == 0) {
                  this.listener.onResult(1, registResponse.getModels()[0].getUserId() + "");
               } else {
                  this.listener.onResult(0, "fail");
               }
            } else {
               this.listener.onResult(0, "fail");
            }
            break;
         case 20003:
            CheckRegiResponse CheckRegiResponse = (CheckRegiResponse)JsonUtils.getInstance().jsonToBean(result.toString(), CheckRegiResponse.class);
            if (CheckRegiResponse != null) {
               if ("0000".equals(CheckRegiResponse.getInfo())) {
                  this.listener.onResult(0, "not register");
               } else {
                  String serverIP = CheckRegiResponse.getModels();
                  if (serverIP != null && !"".equals(serverIP)) {
                     this.listener.onResult(1, CheckRegiResponse.getInfo() + "##" + serverIP);
                  } else {
                     this.listener.onResult(1, CheckRegiResponse.getInfo());
                  }
               }
            } else {
               this.listener.onResult(0, "check_register_null");
            }
            break;
         case 20004:
            GetCipherResponse respon = (GetCipherResponse)JsonUtils.getInstance().jsonToBean(result.toString(), GetCipherResponse.class);
            if (respon != null) {
               if ("".equals(respon.getModels())) {
                  this.listener.onResult(0, "get cipher error");
               } else {
                  this.listener.onResult(1, respon.getModels());
               }
            } else {
               this.listener.onResult(0, "get cipher null");
            }
            break;
         case 20005:
            UploadControllerInfoResponse uciRespn = (UploadControllerInfoResponse)JsonUtils.getInstance().jsonToBean(result.toString(), UploadControllerInfoResponse.class);
            if (uciRespn != null) {
               if ("0000".equals(uciRespn.getInfo())) {
                  this.listener.onResult(1, "ok");
               } else {
                  this.listener.onResult(0, uciRespn.getInfo());
               }
            } else {
               this.listener.onResult(0, "upload response null");
            }
            break;
         case 20006:
            ValidateFriendshipResponse vfResponse = (ValidateFriendshipResponse)JsonUtils.getInstance().jsonToBean(result.toString(), ValidateFriendshipResponse.class);
            if (vfResponse != null) {
               if (vfResponse.getModels() != null && vfResponse.getModels().length > 0) {
                  this.listener.onResult(1, "is friend");
               } else {
                  this.listener.onResult(0, "not friend");
               }
            } else {
               this.listener.onResult(0, "friend list null");
            }
            break;
         case 20007:
            UploadPhotoResponse upResponse = (UploadPhotoResponse)JsonUtils.getInstance().jsonToBean(result.toString(), UploadPhotoResponse.class);
            if (upResponse != null) {
               if ("0000".equals(upResponse.getInfo())) {
                  this.listener.onResult(1, upResponse.getModels());
               } else {
                  this.listener.onResult(0, "fail");
               }
            } else {
               this.listener.onResult(0, "fail");
            }
            break;
         case 20008:
            DevelopSerialNumberRsp rsp = (DevelopSerialNumberRsp)JsonUtils.getInstance().jsonToBean(result.toString(), DevelopSerialNumberRsp.class);
            if (rsp != null) {
               if ("0000".equals(rsp.getInfo())) {
                  this.listener.onResult(1, rsp.getModels());
               } else {
                  this.listener.onResult(0, "fail");
               }
            } else {
               this.listener.onResult(0, "fail");
            }
            break;
         case 20009:
            FindMasterRespon findMasterRsp = (FindMasterRespon)JsonUtils.getInstance().jsonToBean(result.toString(), FindMasterRespon.class);
            if (findMasterRsp != null) {
               if ("0000".equals(findMasterRsp.getInfo()) && findMasterRsp.getModels().length > 0) {
                  FindMasterModel[] var20 = findMasterRsp.getModels();
                  int var14 = var20.length;

                  for(int var15 = 0; var15 < var14; ++var15) {
                     FindMasterModel model = var20[var15];
                     if (model.getUpUserId() == 0) {
                        String info = null;
                        if (model.getUserPhone() != null && !model.getUserName().equals("")) {
                           info = model.getUserPhone();
                        } else if (model.getUserPhone() != null && !model.getUserName().equals("")) {
                        }

                        this.listener.onResult(1, model.getUserName());
                        return;
                     }
                  }

                  this.listener.onResult(0, "no master");
               } else {
                  this.listener.onResult(0, "server error");
               }
            } else {
               this.listener.onResult(0, "network fail");
            }
            break;
         case 20010:
            ActionNameResponse actionNameResponse = (ActionNameResponse)JsonUtils.getInstance().jsonToBean(result.toString(), ActionNameResponse.class);
            if (actionNameResponse != null && actionNameResponse.isStatus()) {
               this.listener.onResult(1, result.toString());
            } else {
               this.listener.onResult(0, (String)null);
            }
            break;
         case 20030:
            this.listener.onResult(0, result.toString());
            break;
         case 20031:
            this.listener.onResult(0, result.toString());
            break;
         case 30001:
            this.listener.onResult(0, result.toString());
         }
      }

   }

   public void onFailure(int requestCode, int state, Object result) {
      super.onFailure(requestCode, state, result);
      this.listener.onResult(state, "onFailure");
   }

   public void setFilePath(String filePath) {
      this.filePath = filePath;
   }

   public boolean isSuccess(int code) {
      return code == 0;
   }

   public static boolean isNumeric(String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
