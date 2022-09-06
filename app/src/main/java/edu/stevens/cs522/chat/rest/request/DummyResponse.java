package edu.stevens.cs522.chat.rest.request;

import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.base.EnumUtils;

/**
 * Created by dduggan.
 */

public class DummyResponse extends ChatServiceResponse implements Parcelable {

    public boolean isValid() {
        return true;
    }

    public DummyResponse() {
        super("", 200, "OK");
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        EnumUtils.writeEnum(out, ResponseType.DUMMY);
        super.writeToParcel(out, flags);
    }

    public DummyResponse(Parcel in) {
        super(in);
    }

    public static final Creator<DummyResponse> CREATOR = new Creator<DummyResponse>() {
        public DummyResponse createFromParcel(Parcel in) {
            EnumUtils.readEnum(ResponseType.class, in);
            return new DummyResponse(in);
        }

        public DummyResponse[] newArray(int size) {
            return new DummyResponse[size];
        }
    };

}

