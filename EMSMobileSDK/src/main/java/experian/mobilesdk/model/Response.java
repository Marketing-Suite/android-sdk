package experian.mobilesdk.model;

import com.google.gson.annotations.SerializedName;

public class Response {
  @SerializedName("Message")
  String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
