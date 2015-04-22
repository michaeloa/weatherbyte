package com.micabyte.weatherbyte;

import android.os.Parcel;
import android.os.Parcelable;

public class WxInfo implements Parcelable
{
    private static final byte VERSION = 1;
    public int icon;
    private String locationId;
    public String location;
    public double temp;
    public long date;

    public WxInfo()
    {
        super();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeByte(VERSION);
        parcel.writeString(locationId);
        parcel.writeString(location);
        parcel.writeDouble(temp);
        parcel.writeInt(icon);
    }

    public static final Parcelable.Creator<WxInfo> CREATOR = new Parcelable.Creator<WxInfo>()
    {
        public WxInfo createFromParcel(Parcel parcel)
        {
            if(parcel.readByte()!=VERSION)
                return null;
            WxInfo info = new WxInfo();
            info.locationId = parcel.readString();
            info.location = parcel.readString();
            info.temp = parcel.readDouble();
            info.icon = parcel.readInt();
            return info;
        }

        public WxInfo[] newArray(int count)
        {
            return new WxInfo[count];
        }
    };

}