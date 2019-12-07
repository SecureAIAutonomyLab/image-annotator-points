package source_code;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Secure AI And Autonomy Laboratory
 * Main.java by Thinh Vo.
 * Application: MRI Scan Image.
 * Purpose:
 * *** The main goal is to use this application to mark circle dots on certain areas of the MRI scan image.
 * *** The circle dots include x and y coordinates C(12,43). They will correspond to the pixel scales of the original image with different image dimensions during zooming in or zooming out.
 *     The application need to get close to 100% accuracy data.
 * *** JavaFX is chosen for this application because it has various resources to support GUI and OpenCV.
 */
public class Main extends Application {
    /**
     * Declare Variables
     */
    static double initx=-1;
    static double inity=-1;
    static double originalImageWidth=0;
    static double originalImageHeight=0;
    static double globalZoomValue=0.0;
    static int height;
    static int width;
    public static String path;
    static Scene initialScene,View;
    static double offSetX,offSetY, zoomlvl;
    float zoom =0;
    static int onpresss = 0;
    static int aon = 0;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "x,y,Zoom Level";
    static ArrayList<Points> parameters = new ArrayList<Points>();
    static Label normalizeXText = new Label();
    static List<Label> coordinateListLabel;
    static String fileName = "Your Chosen Location to Save Image";
    static String filePath = "Your Chosen Image Icon";

    /**
     * Setting up GUI windows, button and other icons to upload and interact with images.
     * @param: Stage variable from library.
     * @throws FileNotFoundException
     */
    @Override
    public void start(Stage s) throws FileNotFoundException {
        s.setResizable(false);
        s.setTitle("MRI Scan Image");
        Image iconImage = new Image(new FileInputStream(filePath));
        s.getIcons().add(iconImage);
        GridPane grid = new GridPane();
        grid.setHgap(20);grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        Label hint = new Label("Select Your Image");
        TextField URL = new TextField();
        URL.setEditable(false);
        URL.setPrefWidth(350);

        Button browse = new Button("Browse");
        FileChooser fc = new FileChooser();
        ExtensionFilter png = new ExtensionFilter("png", "*.png");
        ExtensionFilter jpg = new ExtensionFilter("jpg", "*.jpg");
        ExtensionFilter tiff = new ExtensionFilter("TIFF", "*.tif", "*.tiff");
        fc.getExtensionFilters().addAll(png,jpg,tiff);
            browse.setOnAction(e->{
            URL.setText(fc.showOpenDialog(s).getAbsolutePath());
        });

        Button open = new Button("Open");
        open.setOnAction(e->{
            path = URL.getText();
            initView();
            s.setScene(View);
        });
        grid.add(hint, 0, 0);
        grid.add(URL, 1, 0);
        grid.add(browse, 2, 0);
        grid.add(open, 2, 1);
        initialScene = new Scene(grid,600,100);
        s.setScene(initialScene);
        s.show();
    }

    /**
     * Setting up second window (including size, input file)
     */
    public static void initView(){
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        Pane pane = new Pane(),papa = new Pane();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setMinWidth(600);
        scrollPane.setMinHeight(600);
        scrollPane.setMaxWidth(600);
        scrollPane.setMaxHeight(600);
        scrollPane.setContent(pane);
        Label title = new Label(path.substring(path.lastIndexOf("\\")+1));
        Image image = null;
        try {
            image = new Image(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ImageView imageView = new ImageView(image);
        imageView.setLayoutX(0);
        imageView.setLayoutY(0);
        /**
         * ratio: Aspect Ratio: Resize something (like an image), but wish to preserve the way it looks.
         **/
        double ratio = originalImageWidth/originalImageHeight;
        double ratio_value_changed = 0;
        height = (int) image.getHeight();
        width = (int) image.getWidth();
        originalImageWidth = (int) image.getWidth();
        originalImageHeight = (int) image.getHeight();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(originalImageWidth);
        imageView.setFitHeight(originalImageHeight);
        HBox zoom = new HBox(10);
        zoom.setAlignment(Pos.CENTER);

        Slider zoomLvl = new Slider();
        zoomLvl.setMin(1);
        zoomLvl.setMax(10);
        zoomLvl.setMaxWidth(200);
        zoomLvl.setMinWidth(200);
        Label hint = new Label("Zoom Level");
        Label value = new Label("1.0");
        Slider Hscroll = new Slider();
        zoomLvl.valueProperty().addListener(e->{
            zoomlvl = zoomLvl.getValue();//exact #
            globalZoomValue = zoomlvl;
            double newValue = (double)((int)(zoomlvl*1000))/1000;
            value.setText(newValue+"");
        });
        zoom.getChildren().addAll(hint,zoomLvl,value);
        pane.setOnMouseDragged(e->{onpresss = 1;});
        BorderPane imageViewBorderPane = new BorderPane();
        offSetX = width/2;
        offSetY = height/2;
        pane.getChildren().add(imageView);
        imageView.fitWidthProperty().bind(pane.widthProperty());
        imageView.fitHeightProperty().bind(pane.heightProperty());
        imageViewBorderPane.setCenter(scrollPane);
        ArrayList<Circle> circles = new ArrayList<Circle>();// contain circle
        //Point of circle. Point is ratio - width and height
        List<Point2D> points = new ArrayList<Point2D>(),origin = new ArrayList<Point2D>();// contain list of ratio
        zoomLvl.valueProperty().addListener(e->{
            zoomlvl = zoomLvl.getValue();
            pane.setMinHeight(zoomlvl*originalImageHeight);
            pane.setMaxHeight(zoomlvl*originalImageHeight);
            pane.setMinWidth(originalImageWidth*zoomlvl);
            pane.setMaxWidth(originalImageWidth*zoomlvl);
        });

        pane.setOnMousePressed(e->{
            inity = e.getY();
            initx = e.getX();
        });
        pane.heightProperty().addListener((ob,o,n)->{
            for(int i =0;i<circles.size();i++){
                circles.get(i).setLayoutY(
                        points.get(i).getY()*(double)n);
            }
        });
        pane.widthProperty().addListener((ob,o,n)->{
            for(int i =0;i<circles.size();i++){
                circles.get(i).setLayoutX(
                        points.get(i).getX()*(double)n);
            }
        });
        ScrollPane spane = new ScrollPane();
        spane.setMinWidth(150);
        spane.setMaxWidth(150);
        VBox vbox = new VBox();
        vbox.setMinWidth(120);
        vbox.setMaxWidth(120);
        vbox.setSpacing(5);
        spane.setContent(vbox);
        imageViewBorderPane.setLeft(spane);

        pane.setOnMouseMoved(cursor->{
            if(globalZoomValue == 0.0){
                globalZoomValue = 1.0;
            }
            double normalizeX = (double)((int)(cursor.getX()/globalZoomValue*1000))/1000;
            double normalizeY = (double)((int)(cursor.getY()/globalZoomValue*1000))/1000;
            double formalizeGlobalZoomValue = (double)((int)(globalZoomValue*1000))/1000;
            String normalizeXString = String.valueOf(normalizeX);
            normalizeXText.setText(normalizeXString);
        });

        Button active = new Button("Activate");
        active.setOnMouseClicked(e->{
            aon = aon == 0?1:0;
        });
        HBox hbox = new HBox();
        hbox.getChildren().add(active);
        imageViewBorderPane.setTop(hbox);
        hbox.setSpacing(10);

        Button save = new Button("Save");
        save.setOnMouseClicked(ee->{
            writePointsToCsvFile(points,fileName);
        });
        hbox.getChildren().add(save);
        pane.setOnMouseClicked(e->{
            if(circles.size() > 20)
                return;
            if (onpresss == 1 ){
                onpresss = 0;
                return;

            }
            if(aon ==0)
                return;
            initx = e.getX();
            inity = e.getY();
            imageViewBorderPane.setCursor(Cursor.DEFAULT);

            if(globalZoomValue == 0.0){
                globalZoomValue = 1.000;
            }
            if(e.getButton() == MouseButton.PRIMARY){
                Circle circle = new Circle(3);
                circle.setFill(Color.BLUE);
                circle.setLayoutX(initx);
                circle.setLayoutY(inity);
                String result = "35 45";
                Point2D p = new Point2D(initx/pane.getWidth(),inity/pane.getHeight());
                double normalizeX = (double)((int)(initx/globalZoomValue*1000))/1000;
                double normalizeY = (double)((int)(inity/globalZoomValue*1000))/1000;
                double formalizeGlobalZoomValue = (double)((int)(globalZoomValue*1000))/1000;
                circle.setOnMouseClicked(ee->{
                    if(ee.getButton().equals(MouseButton.SECONDARY)){
                        int index = circles.indexOf(circle);
                        circles.remove(index);
                        points.remove(index);

                        pane.getChildren().remove(circle);
                    }
                });
                vbox.getChildren().add(new Label("Zoom Level: "+formalizeGlobalZoomValue+"\n"+normalizeX+":"+normalizeY));
                ScrollPane sp = new ScrollPane();
                sp.setPannable(true);
                sp.setPrefSize(10,100);
                sp.setContent(vbox);
                Points parameter = new Points(normalizeX,normalizeY,formalizeGlobalZoomValue);
                parameters.add(parameter);
                circles.add(circle);
                origin.add(new Point2D(initx,inity));
                points.add(p);
                pane.getChildren().addAll(circle);
            }
            FileWriter fileWriter = null;
        });
        pane.setOnMousePressed(e->{
            inity = e.getY();
            initx = e.getX();
        });
        root.getChildren().addAll(title,imageViewBorderPane,zoom);
        View = new Scene(root,originalImageWidth+300,originalImageHeight+350);
    }

    /**
     * Main method to launch the program.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Write point coordinates to CSV file.
     * @param pointsList: list of recorded points.
     * @param fileName: name of file to be saved.
     */
    public static void writePointsToCsvFile(List<Point2D> pointsList, String fileName) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.flush();
            //Write the CSV file header
            fileWriter.append(FILE_HEADER.toString());
            //Add a new line separator after the header
            fileWriter.append(NEW_LINE_SEPARATOR);

            //Write a new student object list to the CSV file
            for(Point2D point : pointsList){
                fileWriter.append(String.valueOf(point.getX()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(point.getY()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
}