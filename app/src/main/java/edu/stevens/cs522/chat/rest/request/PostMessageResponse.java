package edu.stevens.cs522.chat.rest.request;

import android.net.Uri;
import android.os.Parcel;

import java.io.IOException;

import edu.stevens.cs522.base.EnumUtils;
import retrofit2.Response;

/**
 * Created by dduggan.
 */

public class PostMessageResponse extends ChatServiceResponse {

    protected final static String LOCATION = "Location";

    // assigned by server
    protected long messageId;

    public PostMessageResponse(Response<?> response) throws IOException {
        super(response);

        // TODO set messageId from HTTP response header
        String loc = response.headers().get(LOCATION);
        if (loc != null) {
            Uri uri = Uri.parse(loc);
            messageId = Long.parseLong((uri.getLastPathSegment()));
        }

    }

    public long getMessageId() {
        return messageId;
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EnumUtils.writeEnum(dest, ResponseType.POSTMESSAGE);
        super.writeToParcel(dest, flags);
        dest.writeLong(messageId);
    }

    public PostMessageResponse(Parcel in) {
        super(in);
        messageId = in.readLong();
    }

    public static Creator<PostMessageResponse> CREATOR = new Creator<PostMessageResponse>() {
        @Override
        public PostMessageResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new PostMessageResponse(in);
        }

        @Override
        public PostMessageResponse[] newArray(int size) {
            return new PostMessageResponse[size];
        }
    };
}
