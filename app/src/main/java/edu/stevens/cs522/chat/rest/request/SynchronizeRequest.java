package edu.stevens.cs522.chat.rest.request;

import android.os.Parcel;

import java.io.IOException;

import edu.stevens.cs522.base.EnumUtils;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public class SynchronizeRequest extends ChatServiceRequest {

    // Added by request processor
    public long lastSequenceNumber;

    public SynchronizeRequest() {
        super();
    }

    @Override
    public ChatServiceResponse getResponse(Response<?> response) throws IOException{
        if (response.isSuccessful()) {
            return new SynchronizeResponse(response);
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
        EnumUtils.writeEnum(dest, RequestType.SYNCHRONIZE);
        super.writeToParcel(dest, flags);
        dest.writeLong(lastSequenceNumber);
    }

    public SynchronizeRequest(Parcel in) {
        super(in);
        lastSequenceNumber = in.readLong();
    }

    public static Creator<SynchronizeRequest> CREATOR = new Creator<SynchronizeRequest>() {
        @Override
        public SynchronizeRequest createFromParcel(Parcel in) {
            EnumUtils.readEnum(RequestType.class, in);
            return new SynchronizeRequest(in);
        }

        @Override
        public SynchronizeRequest[] newArray(int size) {
            return new SynchronizeRequest[size];
        }
    };

}
