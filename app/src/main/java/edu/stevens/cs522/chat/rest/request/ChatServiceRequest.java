package edu.stevens.cs522.chat.rest.request;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.base.DateUtils;
import edu.stevens.cs522.base.EnumUtils;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public abstract class ChatServiceRequest implements Parcelable {

    private final static String TAG = ChatServiceRequest.class.getCanonicalName();

    public enum RequestType {
        REGISTER("Register"),
        POST_MESSAGE("Post Message"),
        SYNCHRONIZE("Synchronize");
        private final String value;
        RequestType(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public enum StatusType {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        FAILED("Failed"),
        COMPLETED("Completed");
        private final String value;
        StatusType(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    // Status of a request
    public StatusType status;

    // Installation id
    public UUID appID;

    // App version
    public long version;

    // Time stamp
    public Date timestamp;

    // Device coordinates
    public double longitude;

    public double latitude;

    // Output in case of errors
    public String responseMessage;


    protected ChatServiceRequest() {
        this.status = StatusType.PENDING;
    }

    protected ChatServiceRequest(Parcel in) {
        // Assume tag has already been read, this will be called by subclass constructor
        status = EnumUtils.readEnum(StatusType.class, in);
        if (in.readByte() != 0) {
            appID = UUID.fromString(in.readString());
        }
        version = in.readLong();
        if (in.readByte() != 0) {
            timestamp = DateUtils.readDate(in);
        }
        longitude = in.readDouble();
        latitude = in.readDouble();
        if (in.readByte() != 0) {
            responseMessage = in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Subclasses write tag, then call this, then write out their own fields
        EnumUtils.writeEnum(out, status);
        if (appID != null) {
            out.writeByte((byte)1);
            out.writeString(appID.toString());
        } else {
            out.writeByte((byte)0);
        }
        out.writeLong(version);
        if (timestamp != null) {
            out.writeByte((byte)1);
            DateUtils.writeDate(out, timestamp);
        } else {
            out.writeByte((byte)0);
        }
        out.writeDouble(longitude);
        out.writeDouble(latitude);
        if (responseMessage != null) {
            out.writeByte((byte) 1);
            out.writeString(responseMessage);
        } else {
            out.writeByte((byte) 0);
        }
    }

    /*
     * HTTP request headers (set in RequestProcessor.perform())
     */
    public static String APP_ID_HEADER = "X-App-Id";

    public static String TIMESTAMP_HEADER = "X-Timestamp";

    public static String LONGITUDE_HEADER = "X-Longitude";

    public static String LATITUDE_HEADER = "X-Latitude";

    // App-specific HTTP request headers.
    public final Map<String,String> getRequestHeaders() {
        Map<String,String> headers = new HashMap<>();
        headers.put(APP_ID_HEADER, appID.toString());
        headers.put(TIMESTAMP_HEADER, Long.toString(timestamp.getTime()));
        headers.put(LONGITUDE_HEADER, Double.toString(longitude));
        headers.put(LATITUDE_HEADER, Double.toString(latitude));
        return headers;
    }

    public String toString() { return this.getClass().getName(); }

    public abstract ChatServiceResponse getResponse(Response<?> response) throws IOException;

    protected ErrorResponse getErrorResponse(Response<?> response) {
        int rc = response.code();
        ErrorResponse.Status status;
        if (rc >= 400 && rc < 500) {
            status = ErrorResponse.Status.APPLICATION_ERROR;
        } else if (rc >= 500) {
            status = ErrorResponse.Status.SERVER_ERROR;
        } else {
            status = ErrorResponse.Status.SYSTEM_ERROR;
        }
        return new ErrorResponse(rc, status, response.message());
    }

    public abstract ChatServiceResponse process(RequestProcessor processor);

    public int describeContents() {
        return 0;
    }

    public static ChatServiceRequest createRequest(Parcel in) {
        RequestType requestType = EnumUtils.readEnum(RequestType.class, in);
        switch (requestType) {
            case REGISTER:
                return new RegisterRequest(in);
            case POST_MESSAGE:
                return new PostMessageRequest(in);
            case SYNCHRONIZE:
                return new SynchronizeRequest(in);
            default:
                break;
        }
        throw new IllegalArgumentException("Unknown request type: "+requestType.name());
    }

    public static final Parcelable.Creator<ChatServiceRequest> CREATOR = new Parcelable.Creator<ChatServiceRequest>() {

        public ChatServiceRequest createFromParcel(Parcel in) {
            return createRequest(in);
        }

        public ChatServiceRequest[] newArray(int size) {
            return new ChatServiceRequest[size];
        }

    };

}