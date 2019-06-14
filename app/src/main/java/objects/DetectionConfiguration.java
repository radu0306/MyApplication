package objects;

import android.os.Parcel;
import android.os.Parcelable;

public class DetectionConfiguration implements Parcelable {

    private String shapeToDetect;
    private String shapeLabel;
    private boolean useFlash = false;
    private boolean displayInfo = false;
    private String colorToDetect;

    public DetectionConfiguration(String shapeToDetect, String shapeLabel, boolean useFlash, boolean displayInfo, String colorToDetect) {
        this.shapeToDetect = shapeToDetect;
        this.shapeLabel = shapeLabel;
        this.useFlash = useFlash;
        this.displayInfo = displayInfo;
        this.colorToDetect = colorToDetect;
    }

    public DetectionConfiguration(){

    }

    protected DetectionConfiguration(Parcel in) {
        shapeToDetect = in.readString();
        shapeLabel = in.readString();
        useFlash = in.readByte() != 0;
        displayInfo = in.readByte() != 0;
        colorToDetect = in.readString();
    }

    public static final Creator<DetectionConfiguration> CREATOR = new Creator<DetectionConfiguration>() {
        @Override
        public DetectionConfiguration createFromParcel(Parcel in) {
            return new DetectionConfiguration(in);
        }

        @Override
        public DetectionConfiguration[] newArray(int size) {
            return new DetectionConfiguration[size];
        }
    };

    public String getShapeToDetect() {
        return shapeToDetect;
    }

    public void setShapeToDetect(String shapeToDetect) {
        this.shapeToDetect = shapeToDetect;
    }

    public String getShapeLabel() {
        return shapeLabel;
    }

    public void setShapeLabel(String shapeLabel) {
        this.shapeLabel = shapeLabel;
    }

    public boolean isUseFlash() {
        return useFlash;
    }

    public void setUseFlash(boolean useFlash) {
        this.useFlash = useFlash;
    }

    public boolean isDisplayInfo() {
        return displayInfo;
    }

    public void setDisplayInfo(boolean displayInfo) {
        this.displayInfo = displayInfo;
    }

    public String getColorToDetect() { return colorToDetect; }

    public void setColorToDetect(String colorToDetect) { this.colorToDetect = colorToDetect; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shapeToDetect);
        dest.writeString(shapeLabel);
        dest.writeByte((byte) (useFlash ? 1 : 0));
        dest.writeByte((byte) (displayInfo ? 1 : 0));
        dest.writeString(colorToDetect);
    }
}
