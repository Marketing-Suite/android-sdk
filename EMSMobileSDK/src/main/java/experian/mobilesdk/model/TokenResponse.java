package experian.mobilesdk.model;

import com.google.gson.annotations.SerializedName;

public class TokenResponse extends Response {
  @SerializedName("Push_Registration_Id")
  private String pushRegistrationId;

  @SerializedName("Cust_Id")
  private String custId;

  @SerializedName("Application_Id")
  private String applicationId;

  @SerializedName("Device_Token")
  private String deviceToken;

  @SerializedName("Status_Id")
  private String statusId;

  public String getPushRegistrationId() {
    return pushRegistrationId;
  }

  public void setPushRegistrationId(String pushRegistrationId) {
    this.pushRegistrationId = pushRegistrationId;
  }

  public String getCustId() {
    return custId;
  }

  public void setCustId(String custId) {
    this.custId = custId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getDeviceToken() {
    return deviceToken;
  }

  public void setDeviceToken(String deviceToken) {
    this.deviceToken = deviceToken;
  }

  public String getStatusId() {
    return statusId;
  }

  public void setStatusId(String statusId) {
    this.statusId = statusId;
  }
}
