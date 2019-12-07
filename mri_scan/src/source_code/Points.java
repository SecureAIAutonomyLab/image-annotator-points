package source_code;

public class Points {
    private double x;
    private double y;
    private double zoomLevel;

    public Points(double x, double y, double zoomLevel){
        super();
        this.x = x;
        this.y = y;
        this.zoomLevel = zoomLevel;
    }

    public double getX(){
        return x;
    }
    public void setX(double x){
        this.x = x;
    }

    public double getY(){
        return y;
    }

    public void setY(double y){
        this.y = y;
    }

    public double getZoomLevel(){
        return zoomLevel;
    }

    public void setZoomLevel(double zoomLevel){
        this.zoomLevel = zoomLevel;
    }


    @Override
    public String toString(){
        return "Parameters [x="+x+", y="+y+"Zoom Level: "+zoomLevel+"]";
    }
}
