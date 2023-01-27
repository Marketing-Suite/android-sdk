package experian.mobilesdk.model;

import com.google.gson.annotations.SerializedName;

public class DeviceToken {
  @SerializedName("DeviceToken")
  final String deviceToken;

  public DeviceToken(String deviceToken) {
    this.deviceToken = deviceToken;
  }
}
