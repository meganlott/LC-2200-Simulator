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
import scalafx.scene.control.Button

class Component {
  
}

object HelloStageDemo extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "LC-2200 Simulator"
    width = 1000
    height = 850
    scene = new Scene(new javafx.scene.Scene(root))
  }
  lazy val root = new BorderPane{
    center = simulatorPane
    top = topPane
  }
  lazy val simulatorPane: Pane = new Pane {
    val graphic = new Image("file:lc2200datapath.png")
    val imgview = new ImageView(graphic)
    children += imgview
    val rec = new Rectangle {
        x = 0
        y = 0
        width = 400
        height = 400
        fill = Yellow
        stroke = Black
        strokeWidth = 2
    }

    //you can update these values after instantiation easily for annimation 
    val line = new Line {
        stroke = Black
        strokeWidth = 3
        startX = 0
        startY = 0
        endX = 250
        endY = 250
    }
    //polygons are just impossible, it turns out. Rip polygons
    val polygon = new Polygon {
        //points = (100.0, 10.0, 200.0, 200.0, 300.0, 300.0)
        fill = Cyan
        stroke = Black
        strokeWidth = 2
    }
    //this prints something, but I can't manipulate the value at all
    println(polygon.points)
   // polygon.points = Array[Double](100.0,100.0, 200.0,200.0,300.0,300.0)
    children += rec
    children += line
  }

  lazy val topPane: Pane = new Pane {
    val stepForward = new Button("Step Forward")
    stepForward.layoutX = 20
    stepForward.layoutY = 50
    //stepForward.setStyle("-fx-font: 22 arial; -fx-base: #b6e7c9;");
    children += stepForward
  }

  def drawDataPath(xx: Integer, yy: Integer) = {
    new Rectangle {
      x = 2
      y = 3
      width = 400
      height = 200
      fill = Cyan
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
