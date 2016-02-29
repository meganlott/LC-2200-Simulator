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
import scalafx.scene.Node
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Polygon
import javafx.scene.control.Tooltip
import javafx.scene.image.{Image, ImageView}
import scalafx.scene.control.{Button, TableView, TableColumn, ScrollPane, Menu, MenuItem, MenuBar, Label, TextField}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.{StringProperty}

abstract class Component {
  /*
  protected var x: Double
  protected var y: Double
  */
  //def getSceneComponent(): Node;
}

object CComponent {
  var pane: Pane = null;
}

class RectComp(val xx: Double, val yy: Double, val w: Double, val h: Double, val n: String) extends Component{
  var x = xx;
  var y = yy;
  val shape = Rectangle(xx, yy, w, h)
  shape.fill = White
  shape.stroke = Black
  shape.strokeWidth = 2

  var text = new Text {
    x = xx + 10
    y = yy + 20
    text = n
    style = "-fx-font-size: 12pt"
    fill = Black
  }

  CComponent.pane.children += shape
  CComponent.pane.children += text

  text.text = "HELLO";

  /*
  def getSceneComponent(): Node = {
    return shape
  }
  */
}

class Wire(val sx: Double, val sy: Double, val ex: Double, val ey: Double) {

  val shape = Line(sx, sy, ex, ey);
  shape.stroke = Black
  shape.strokeWidth = 3

  CComponent.pane.children += shape
}

class TBuffer(val xx: Double, val yy: Double, val n: String) extends Polygon {
}

case class RegInfo(name:String, mem:String)
case class MemInfo(addr:String, mem:String)

object InputManager {
//Top panel input
  //Forward button
  val stepForward = new Button("Step Forward")
    stepForward.setTooltip(
    new Tooltip("Simulates one clock cycle.")
    );
    stepForward.layoutX = 150
    stepForward.layoutY = 60
    stepForward.setMinWidth(120)
  //Backwards button
  val stepBackward = new Button("Step Backward")
    stepBackward.setTooltip(
    new Tooltip("Moves back one clock cycle.")
    );
    stepBackward.layoutX = 20
    stepBackward.layoutY = 60
    stepBackward.setMinWidth(120)
  // Instruction drop down menu
  val instructionSelection = new MenuBar
  val instructionMenu = new Menu("Choose Instruction")
  //These need to be dynamically created from the file system
  val addItem = new MenuItem("Add")

  //Set up selection menu
  instructionMenu.items = List(addItem)
  instructionSelection.menus = List(instructionMenu)
  instructionSelection.layoutX = 305
  instructionSelection.layoutY = 60
  
  // first register input
  val rxtextbox = new TextField
    rxtextbox.promptText = "RX"
    rxtextbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    rxtextbox.maxWidth = 50
    rxtextbox.layoutX = 460
    rxtextbox.layoutY = 60

  // second register input
  val rytextbox = new TextField
    rytextbox.promptText = "RY"
    rytextbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    rytextbox.maxWidth = 50
    rytextbox.layoutX = 520
    rytextbox.layoutY = 60

 // third register input
  val rztextbox = new TextField
    rztextbox.promptText = "RZ"
    rztextbox.setTooltip(
    new Tooltip("Enter a register or numerical value up to (range?)")
    );
    rztextbox.maxWidth = 50
    rztextbox.layoutX = 580
    rztextbox.layoutY = 60

  // execute instruction button
  val execute = new Button("Execute")
    execute.setTooltip(
    new Tooltip("Simulates the execution of an entire instruction.")
    );
    execute.layoutX = 650
    execute.layoutY = 60
    execute.setMinWidth(120)
  val topPaneInputs = Array(stepForward, stepBackward, instructionMenu, rxtextbox, rytextbox, rxtextbox, execute)
}

object LC2200Simulator extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title.value = "LC-2200 Simulator"
    width = 1024
    height = 768
    scene = new Scene(new javafx.scene.Scene(root))
  }
  lazy val root = new BorderPane{
    center = simulatorPane
    top = topPane
    topPane.setMinHeight(100)
    left = leftPane
    leftPane.setMinWidth(200)
  }
  lazy val simulatorPane: Pane = new Pane {
    //val graphic = new Image("file:lc2200datapath.png")
    //val imgview = new ImageView(graphic)
    //children += imgview
    
    val r = new Rectangle {
      x = 0
      y = 0
      width = 720
      height = 520
      stroke = Black
      fill = White
    }

    CComponent.pane = this;
    
    children += r

    new Wire(20,20,720,20);
    new Wire(20,20,20,320);
    new Wire(20,320,720,320);

    new Wire(80,20,80,320);
    new RectComp(60, 50, 40, 30, "PC");
    var t = new Text {
      x = 70
      y = 70
      text = "PC"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    var poly = Polygon(65,270,95,270,80,300);
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;

    new Wire(140,20,140,120);
    new Wire(190,20,190,120);
    new Wire(165, 210, 165, 320)
    new RectComp(120, 50, 40, 30, "A");
    new RectComp(170, 50, 40, 30, "B");
    t = new Text {
      x = 135
      y = 70
      text = "A         B"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    poly = Polygon(120,120, 160,120, 165,130, 170,120, 210,120, 190, 210, 140, 210);
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;
    t = new Text {
      x = 150
      y = 160
      text = "ALU"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    poly = Polygon(150,270,180,270,165,300)
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;

    new Wire(280,20,280,320);
    new RectComp(240, 120, 80, 100, "registers");
    t = new Text {
      x = 247
      y = 150
      text = "registers\n16 x\n32 bits"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    poly = Polygon(265,270,295,270,280,300);
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;

    new Wire(380,20,380,140);
    new Wire(420,20,420,140);
    new RectComp(360, 50, 40, 30, "MAR");
    t = new Text {
      x = 363
      y = 70
      text = "MAR"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    new Wire(400,160,400,320);
    new RectComp(360, 120, 80, 100, "memory");
    t = new Text {
      x = 367
      y = 150
      text = "memory\n2^32 x\n32 bits"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    poly = Polygon(385,270,415,270,400,300);
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;

    new Wire(490,20,490,120);
    new RectComp(470, 50, 40, 30, "IR");
    t = new Text {
      x = 482
      y = 70
      text = "IR"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    new Wire(120,320,120,460);
    new RectComp(100, 360, 40, 30, "=0?");
    t = new Text {
      x = 105
      y = 380
      text = "=0?"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t

    new Wire(510,180,510,320);
    new RectComp(480, 200, 60, 40, "sign");
    t = new Text {
      x = 487
      y = 215
      text = "sign\nextend"
      fill = Black
    }
    children += t
      
    new RectComp(100, 410, 40, 30, "Z");
    t = new Text {
      x = 115
      y = 430
      text = "Z"
      style = "-fx-font-size: 12pt"
      fill = Black
    }
    children += t
    
    poly = Polygon(495,270,525,270,510,300);
    poly.fill = White
    poly.stroke = Black
    poly.strokeWidth = 2
    children += poly;

  }

  lazy val topPane: Pane = new Pane {
    val title = new Label("LC-2200 Simulator")
    title.layoutX = 20
    title.layoutY = 0
    title.style = "-fx-font-size: 36pt"
    children += title

    // I would like something like this but I can't get it to work
    /*for ( i <- 0 to InputManager.topPaneInputs.length) {
      children += InputManager.topPaneInputs(i)
    }*/

    children += InputManager.stepForward
    children += InputManager.stepBackward
    children += InputManager.instructionSelection
    children += InputManager.rxtextbox
    children += InputManager.rytextbox
    children += InputManager.rztextbox
    children += InputManager.execute
  }

  lazy val leftPane: Pane = new Pane {

    val regTableLabel = new Label("Register View")
    regTableLabel.layoutX = 20
    regTableLabel.layoutY = 30
    children += regTableLabel

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
    scrollpane1.layoutY = 50
    children += scrollpane1
    
    val memTableLabel = new Label("Memory View")
    memTableLabel.layoutX = 20
    memTableLabel.layoutY = 260
    children += memTableLabel
    
    val scrollpane2 : ScrollPane = new ScrollPane 
    val memData = new ObservableBuffer[MemInfo]()
    memData.addAll(MemInfo("0x0000","0x0000"),
        MemInfo("0x0001","0x0001"),
        MemInfo("0x0002","0x0002"),
        MemInfo("0x0003","0x0003"),
        MemInfo("0x0004","0x0004"),
        MemInfo("0x0005","0x0005"),
        MemInfo("0x0006","0x0006"),
        MemInfo("0x0007","0x0007")
        )

    val memTable = new TableView(memData)

    val col3 = new TableColumn[MemInfo, String]("Address")
    col3.cellValueFactory = cdf => StringProperty(cdf.value.addr)
    val col4 = new TableColumn[MemInfo, String]("Value")
    col4.cellValueFactory = cdf => StringProperty(cdf.value.mem)

    memTable.columns ++= List(col3, col4)
    scrollpane2.content = memTable
    scrollpane2.maxHeight = 200
    scrollpane2.maxWidth = 160
    scrollpane2.layoutX = 20
    scrollpane2.layoutY = 280
    children += scrollpane2
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

