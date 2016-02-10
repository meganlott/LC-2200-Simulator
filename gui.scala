import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.stage.Stage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.effect._
import scalafx.scene.layout._
import scalafx.scene.shape._
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text._
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Polygon
import javafx.scene.image.{Image, ImageView}
import scalafx.scene.control.{Button, TableView, TableColumn, ScrollPane}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.{StringProperty}

class SComponent(val xx: Double, val yy: Double) extends Rectangle{
  x = xx;
  y = yy;
  width = 400
  height = 400
  fill = Yellow
  stroke = Black
  strokeWidth = 2
}

class Wire(val sx: Double, val sy: Double, val ex: Double, val ey: Double) extends Line {
  startX = sx
  startY = sy
  endX = ex
  endY = ey
  stroke = Black
  strokeWidth = 3
}

case class RegInfo(name:String, mem:String)

object HelloStageDemo extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "LC-2200 Simulator"
    width = 1024
    height = 768
    scene = new Scene(new javafx.scene.Scene(root))
  }
  lazy val root = new BorderPane{
    center = simulatorPane
    top = topPane
    left = leftPane
  }
  lazy val simulatorPane: Pane = new Pane {
    //val graphic = new Image("file:lc2200datapath.png")
    //val imgview = new ImageView(graphic)
    //children += imgview
    
    children += new SComponent(40, 50);

    //polygons are just impossible, it turns out. Rip polygons
    val polygon = Polygon(10.0,20.0,10.0,200.0,100.0,20.0)
    //val polygon = new Polygon {
        ////points = (100.0, 10.0, 200.0, 200.0, 300.0, 300.0)
        //fill = Cyan
        //stroke = Black
        //strokeWidth = 2
    //}
    //this prints something, but I can't manipulate the value at all
    //println(polygon.points)
   // polygon.points = Array[Double](100.0,100.0, 200.0,200.0,300.0,300.0)
    children += polygon
  }

  lazy val topPane: Pane = new Pane {
    //super.setMinHeight(200)
    val stepForward = new Button("Step Forward")
    stepForward.layoutX = 20
    stepForward.layoutY = 50
    //stepForward.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
    children += stepForward
  }

  lazy val leftPane: Pane = new Pane {
    val stepForward = new Button("Step Forward")
    stepForward.layoutX = 20
    stepForward.layoutY = 50
    //stepForward.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
    children += stepForward

    val scrollpane1 : ScrollPane = new ScrollPane 
    val regData = new ObservableBuffer[RegInfo]()
    regData.addAll(RegInfo("R0","0x0000"),
        RegInfo("R1","0x0001"),
        RegInfo("R2","0x0002"),
        RegInfo("R3","0x0003"),
        RegInfo("R4","0x0004"),
        RegInfo("R5","0x0005"),
        RegInfo("R6","0x0006"),
        RegInfo("R7","0x0007")
        )

    val regTable = new TableView(regData)

    val col1 = new TableColumn[RegInfo, String]("Register")
    col1.cellValueFactory = cdf => StringProperty(cdf.value.name)
    val col2 = new TableColumn[RegInfo, String]("Value")
    col2.cellValueFactory = cdf => StringProperty(cdf.value.mem)

    regTable.columns ++= List(col1, col2)
    scrollpane1.content = regTable
    scrollpane1.maxHeight = 200
    scrollpane1.maxWidth = 160
    scrollpane1.layoutX = 20
    scrollpane1.layoutY = 80
    children += scrollpane1
  }

  def newComponent(xx: Double, yy: Double) = {
    new Rectangle {
      x = xx;
      y = yy;
      width = 20
      height = 20
      stroke = Black
    }
  }

  def addButtons() = {
    val stepForward = new Button("Step Forward")
    stepForward.layoutX = 20
    stepForward.layoutY = 50
  }
  stage.getIcons().add(new Image("file:CPU.png"))
}

/*
object HelloStageDemo extends JFXApp {
    title.value = "Hello Stage"
    width = 640
    height = 480
    scene = new Scene {
      fill = Color.Blue
      content = new HBox {
        children = Seq(
          new Text {
            text = "LC-2200"
            style = "-fx-font-size: 48pt"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Cyan, DodgerBlue)
            )
            effect = new DropShadow {
              color = DodgerBlue
              radius = 25
              spread = 0.25
            }
          },
          new Rectangle {
            x = 25
            y = 40
            width <== when (hover) choose 200 otherwise 100
            height = 100
            fill <== when (hover) choose Color.Green otherwise Color.Red
          }
        )
      }
    }

  }
}
*/



//import scala.swing._

//class UI extends MainFrame {
  //title = "GUI Program #1";
  //preferredSize = new Dimension(640, 480);
  //contents = new Label("Here is the contents!");
  //contents = new FlowPanel { 
    //contents += new Button("huhu") { 
      //minimumSize = preferredSize
      //maximumSize = preferredSize
      //preferredSize = preferredSize
    //}
  //}
//}

//object GuiProgramOne {
  //def main(arg: Array[String]) {
    //val ui = new UI
    //ui.visible = true
    //println("End of main function.")
  //}
//}
