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
import javafx.scene.image.{Image, ImageView}

class Component {
  
}

object HelloStageDemo extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "Hello Stage"
    width = 640
    height = 480
    scene = new Scene(new javafx.scene.Scene(root))
  }
  lazy val root = new BorderPane{
    center = simulatorPane
  }
  lazy val simulatorPane: Pane = new Pane {
    val graphic = new Image("file:lc2200datapath.png")
    val imgview = new ImageView(graphic)
    children += imgview
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
