package edu.stevens.cs522.chat.rest.request;

import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import java.io.IOException;

import edu.stevens.cs522.base.EnumUtils;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public class RegisterRequest extends ChatServiceRequest {

    private static final String TAG = ChatServiceRequest.class.getCanonicalName();

    public Uri chatServer;

    public String chatname;

    public RegisterRequest(Uri chatServer, String chatname) {
        super();
        this.chatServer = chatServer;
        this.chatname = chatname;
    }

    @Override
    public ChatServiceResponse getResponse(Response<?> response) throws IOException{
        if (response.isSuccessful()) {
            return new RegisterResponse(response);
        } else {
            return getErrorResponse(response);
        }
    }

    @Override
    public ChatServiceResponse process(RequestProcessor processor) {
        return processor.perform(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, RequestType.REGISTER);
        super.writeToParcel(dest, flags);
        dest.writeString(chatServer.toString());
        dest.writeString(chatname);
    }

    public RegisterRequest(Parcel in) {
        super(in);
        this.chatServer = Uri.parse(in.readString());
        this.chatname = in.readString();
    }

    public static Creator<RegisterRequest> CREATOR = new Creator<RegisterRequest>() {
        @Override
        public RegisterRequest createFromParcel(Parcel in) {
            EnumUtils.readEnum(RequestType.class, in);
            return new RegisterRequest(in);
        }

        @Override
        public RegisterRequest[] newArray(int size) {
            return new RegisterRequest[size];
        }
    };

}
