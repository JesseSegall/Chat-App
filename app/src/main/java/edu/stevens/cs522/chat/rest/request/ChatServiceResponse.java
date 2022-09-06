package edu.stevens.cs522.chat.rest.request;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import edu.stevens.cs522.base.EnumUtils;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public abstract class ChatServiceResponse implements Parcelable {

    private final static String TAG = ChatServiceResponse.class.getCanonicalName();

    public enum ResponseType {
        ERROR,
        DUMMY,
        REGISTER,
        POSTMESSAGE,
        SYNCHRONIZE
    }

    public final static String RESPONSE_MESSAGE_HEADER = "X-Response-Message";

	/*
	 * These fields are obtained from the response metadata (response headers and status line).
	 * The fields in the subclass responses are obtained from the JSON body of the response entity.
	 */

    // Human-readable response message (optional)
    public String responseMessage = "";

    // HTTP status code.
    public int httpResponseCode = 0;

    // HTTP status line message.
    public String httpResponseMessage = "";

    public abstract boolean isValid();

    public ChatServiceResponse(Response<?> response) throws IOException {

        String message = response.headers().get(RESPONSE_MESSAGE_HEADER);
        if (message != null) {
            responseMessage = message;
        }

        httpResponseCode = response.code();

        httpResponseMessage = response.message();

    }

    public ChatServiceResponse(String responseMessage, int httpResponseCode, String httpResponseMessage) {
        this.responseMessage = responseMessage;
        this.httpResponseCode = httpResponseCode;
        this.httpResponseMessage = httpResponseMessage;
    }

    public ChatServiceResponse(Parcel in) {
        if (in.readByte() == 1) {
            responseMessage = in.readString();
        }
        httpResponseCode = in.readInt();
        if (in.readByte() == 1) {
            httpResponseMessage = in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if (responseMessage != null) {
            out.writeByte((byte) 1);
            out.writeString(responseMessage);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(httpResponseCode);
        if (httpResponseMessage != null) {
            out.writeByte((byte) 1);
            out.writeString(httpResponseMessage);
        } else {
            out.writeByte((byte) 0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public static ChatServiceResponse createResponse(Parcel in) {
        ResponseType requestType = EnumUtils.readEnum(ResponseType.class, in);
        switch (requestType) {
            case ERROR:
                return new ErrorResponse(in);
            case DUMMY:
                return new DummyResponse(in);
            case REGISTER:
                return new RegisterResponse(in);
            case POSTMESSAGE:
                return new PostMessageResponse(in);
            case SYNCHRONIZE:
                return new SynchronizeResponse(in);
            default:
                break;
        }
        throw new IllegalArgumentException("Unknown request type: "+requestType.name());
    }

    public static final Parcelable.Creator<ChatServiceResponse> CREATOR = new Parcelable.Creator<ChatServiceResponse>() {
        public ChatServiceResponse createFromParcel(Parcel in) {
            return createResponse(in);
        }

        public ChatServiceResponse[] newArray(int size) {
            return new ChatServiceResponse[size];
        }
    };

}
