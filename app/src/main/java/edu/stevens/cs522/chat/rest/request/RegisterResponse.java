package edu.stevens.cs522.chat.rest.request;

import android.net.Uri;
import android.os.Parcel;

import java.io.IOException;

import edu.stevens.cs522.base.EnumUtils;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public class RegisterResponse extends ChatServiceResponse {

    private final static String LOCATION = "Location";

    public RegisterResponse(Response<?> response) throws IOException {
        super(response);
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, ResponseType.REGISTER);
        super.writeToParcel(dest, flags);
    }

    public RegisterResponse(Parcel in) {
        super(in);
    }

    public static Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
        @Override
        public RegisterResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new RegisterResponse(in);
        }

        @Override
        public RegisterResponse[] newArray(int size) {
            return new RegisterResponse[size];
        }
    };
}
