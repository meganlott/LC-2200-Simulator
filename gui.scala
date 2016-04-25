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
import scalafx.scene.input.MouseEvent
import scalafx.scene.text._
import scalafx.geometry.{VPos, Pos}
import scalafx.scene.Node
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Polygon
import javafx.scene.control.Tooltip
import javafx.scene.image.{Image, ImageView}
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, TableView, TableColumn, ScrollPane, Menu, MenuItem, MenuBar, Label, TextField, ComboBox}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.{StringProperty}
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

abstract class Component {

  val name: String

  var activators: ArrayBuffer[Activator] = new ArrayBuffer[Activator]()

  var inputs: Int = 1

  protected var inWires: Array[Wire] = new Array[Wire](1)
  var outWire: Wire = null;

  def readInputData(): Array[Short] = {
    var data = new Array[Short](inputs)
    for (i <- 0 until inputs) {
      data(i) = inWires(i).value
    }
    return data;
  }

  def setOutputData(data: Short) {
    outWire.setValue(data)
  }

  def setNumberOfInputs(num: Int) {
    inputs = num;
    inWires = new Array[Wire](num)
  }

  def getInputLocation(input: Int = 0): (Double, Double)
  def getOutputLocation(): (Double, Double)

  def inputToBus(input: Int = 0) {
    val loc = getInputLocation(input)
    inWires(input) = new RealWire(loc._1, DataPath.busTop, loc._1, loc._2)
    DataPath.bus.add(inWires(input))
  }

  def outputToBus() {
    val loc = getOutputLocation()
    DataPath.bus.add(new RealWire(loc._1, loc._2, loc._1, DataPath.busBottom))
    outWire = DataPath.bus
  }
  
  def inputToComponent(c: Component, input: Int = 0) {
    val loc1 = c.getOutputLocation()
    val loc2 = getInputLocation(input)
    val newWire = new RealWire(loc1._1, loc1._2, loc1._1, loc2._2)
    inWires(input) = newWire
    c.outWire = newWire
  }

  def createActivator(n: String, dir: Int)
  
  def addActivator(a: Activator) {
    activators += a
    DataPath.addActivator(a)
  }

  def scale(s: Double)
  //def getSceneComponent(): Node;
}

class Activator(val xx: Double, val yy: Double, val n: String, val s: Component, val flip: Int = 0) {
  val name = n;
  val source = s;
  var offset = 12 * flip;
  val shape = Line(xx + offset, yy, xx, yy)
  if (flip != 0) {
    shape.stroke = Black
    shape.strokeWidth = 1
    DataPath.pane.children += shape
  }
  
  var text = new Text {
    x = xx
    y = yy + 3
    text = name
    style = "-fx-font-size: 8pt"
    fill = Black
  }
  
  moveText(1.0)
  DataPath.pane.children += text

  def activate(): Array[Short] = {
    shape.stroke = Red
    text.fill = Red
    return source.readInputData()
  }
  def deactivate() {
    shape.stroke = Black
    text.fill = Black
  }

  def moveText(sc: Double) {
    var offset = 12 * flip * sc;
    if (flip != 0) {
      offset = flip * 16;
      if (flip > 0) {
        offset += 3
      } else if (flip < 0) {
        offset += 5
      }
    }
    val j: Double = text.getLayoutBounds().getWidth()
    if (flip < 0) {
      text.x = (xx + offset) * sc - j
    } else {
      text.x = xx*sc + offset*sc
    }

  }
  def scale(sc: Double) {
    shape.startX = xx*sc + 12 * flip * sc;
    shape.endX = xx*sc
    moveText(sc)
  }
}

object DataPath {

  val busTop = 20;
  val busBottom = 320;

  var bg: Rectangle = null

  var pane: Pane = null;

  var components: Map[String, Component] = Map()
  var activators: Map[String, Activator] = Map()

  val bus: WireSet = new WireSet()

  def activate(s: String, f: Array[Short]=>Short) {
    activators(s).s.setOutputData(f(activators(s).activate()))
  }
  def deactivate(s: String) = activators(s).deactivate()
  def deactivateAll() {
    for((key,value) <- activators)
      value.deactivate()
  }

  def addActivator(a: Activator) {
    activators += (a.name -> a)
  }

  def addComponent(c: Component) {
    components += (c.name -> c)
  }
}

class RectComp(val xx: Double, val yy: Double, val w: Double, val h: Double, val n: String) extends Component{
  var x = xx;
  var y = yy;
  val shape = Rectangle(xx, yy, w, h)
  val name = n;
  shape.fill = White
  shape.stroke = Black
  shape.strokeWidth = 2

  var actDir = 0

  var text = new Text {
    x = 0
    y = yy + 20
    text = name
    style = "-fx-font-size: 12pt"
    fill = Black
  }
  text.x = xx + w/2 - text.getLayoutBounds().getWidth()/2 - 3

  DataPath.pane.children += shape
  DataPath.pane.children += text
  DataPath.addComponent(this)

  def getInputLocation(input: Int = 0): (Double, Double) = {
    var step = w / (inputs + 1)
    return (x + step * (input+1), y)
  }

  def getOutputLocation(): (Double, Double) = {
    return (x + w / 2, y + h)
  }

  def scale(s: Double) {
    text.x = (xx + w/2 - 3) * s - text.getLayoutBounds().getWidth()/2
    shape.x = xx * s;
    shape.width = s * w;


    for (wire <- inWires) {
      if (wire != null)
        wire.scale(s)
    }
    if (outWire != null)
      outWire.scale(s)

    for (a <- activators) { a.scale(s) }

  }

  def createActivator(n: String, dir: Int) {
    var sx = x
    actDir = dir
    if (dir > 0) {
      sx = x + w
    } else if (dir == 0) {
      sx = getInputLocation(activators.size)._1 - 24
      text.y = y + 40
    }
    var a = new Activator(sx, y+15, n, this, dir)
    addActivator(a)
  }
}

class TriComp(val xx: Double, val yy: Double, val w: Double, val h: Double, val n: String) extends Component{
  var x = xx;
  var y = yy;
  var shape = Polygon(xx-w/2,yy,xx+w/2,yy,xx,yy+h);
  val name = n;
  shape.fill = White
  shape.stroke = Black
  shape.strokeWidth = 2
  DataPath.pane.children += shape
  DataPath.addComponent(this)

  def getInputLocation(input: Int = 0): (Double, Double) = {
    return (x, y)
  }

  def getOutputLocation(): (Double, Double) = {
    return (x, y+h)
  }

  def createActivator(n: String, dir: Int) {
    var sx = x - w/2;
    if (dir > 0) {
      sx = x + w/2;
    }
    var a = new Activator(sx, y+15, n, this, dir)
    addActivator(a)
  }

  def scale(s: Double) {
    val temp = Polygon(xx*s-s*w/2,yy,xx*s+s*w/2,yy,xx*s,yy+h);
    temp.fill = White
    temp.stroke = Black
    temp.strokeWidth = 2
    DataPath.pane.children -= shape
    shape = temp
    DataPath.pane.children += shape

    for (wire <- inWires) {
      if (wire != null)
        wire.scale(s)
    }
    if (outWire != null)
      outWire.scale(s)
    for (a <- activators) { a.scale(s) }
  }
}

abstract class Wire() {

  var value: Short = 0;

  def setValue(value: Short)
  def getValue(): Short = {
    return value
  }
  def scale(s: Double)
}
class RealWire(val sx: Double, val sy: Double, val ex: Double, val ey: Double) extends Wire {

  val shape = Line(sx, sy, ex, ey);
  shape.stroke = Black
  shape.strokeWidth = 3

  DataPath.pane.children += shape

  var text = new Text {
    x = (sx+ex)/2 + 6
    y = (sy+ey)/2 + 12
    text = InputManager.formatInt(value)
    style = "-fx-font-size: 8pt"
    fill <== when (shape.hover) choose Red otherwise TRANSPARENT
  }
  DataPath.pane.children += text

  def scale(s: Double) {
    shape.startX = sx * s;
    shape.endX = ex * s;
    text.x = ((sx+ex)/2 + 6) * s
  }

  def setValue(value: Short) = {
    this.value = value;
    text.text = InputManager.formatInt(value)
  }
}

class WireSet() extends Wire{
  val wires = new ArrayBuffer[Wire]();

  def setValue(v: Short) = {
    for (wire <- wires) {
      wire.setValue(v);
    }
  }

  def scale (s: Double)  = {
    for (wire <- wires) {
      wire.scale(s);
    }
  }

  def add(wire: Wire): Wire = {
    wire.setValue(value)
    wires.append(wire)
    return wire
  }
}

class TBuffer(val xx: Double, val yy: Double, val n: String) extends Polygon {
}

case class RegInfo(name:String, mem:String)
case class MemInfo(addr:String, mem:String)


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
      width = 720 //startWidth
      height = 520 //startHeight
      stroke = Black
      fill = White
    }
    DataPath.bg = r;
    DataPath.pane = this;
    
    children += r

    DataPath.bus.add(new RealWire(20,20,720,20))
    DataPath.bus.add(new RealWire(20,20,20,320))
    DataPath.bus.add(new RealWire(20,320,720,320))

    var xBasis = 90

    val pc = new RectComp(xBasis, 50, 40, 30, "PC");
    pc.inputToBus(0)
    pc.createActivator("LdPC", -1)
    val pcDrive = new TriComp(xBasis+20,270,30,30,"PCDrive");
    pcDrive.inputToComponent(pc)
    pcDrive.outputToBus()
    pcDrive.createActivator("DrPC", -1)

    xBasis += 110

    val aBox = new RectComp(xBasis, 50, 40, 30, "A");
    aBox.inputToBus()
    aBox.createActivator("LdA", -1)
    val bBox = new RectComp(xBasis+50, 50, 40, 30, "B");
    bBox.inputToBus()
    bBox.createActivator("LdB", 1)

    val alu = new RectComp(xBasis, 120, 90, 90, "ALU")
    alu.setNumberOfInputs(2)
    alu.inputToComponent(aBox, 0)
    alu.inputToComponent(bBox, 1)
    alu.createActivator("ALUFunc", -1)
    val aluDrive = new TriComp(xBasis+45, 270, 30, 30, "ALUDrive");
    aluDrive.inputToComponent(alu)
    aluDrive.outputToBus()
    aluDrive.createActivator("DrALU", -1)

    xBasis += 170
    
    val regs = new RectComp(xBasis, 120, 80, 100, "registers");
    regs.inputToBus()
    regs.createActivator("Din", 0)
    regs.createActivator("WrREG", -1)
    val regDrive = new TriComp(xBasis+40, 270, 30, 30, "REGDrive");
    regDrive.inputToComponent(regs)
    regDrive.outputToBus()
    regDrive.createActivator("DrREG", -1)

    xBasis += 160

    val mar = new RectComp(xBasis, 50, 40, 30, "MAR");
    mar.inputToBus()
    mar.createActivator("LdMAR", -1)
    val mem = new RectComp(xBasis, 120, 80, 100, "memory\n2^32 x\n32 bits");
    mem.setNumberOfInputs(2)
    mem.inputToBus(1)
    mem.inputToComponent(mar, 0)
    mem.createActivator("Addr", 0)
    mem.createActivator("Din", 0)
    mem.createActivator("WrMEM", -1)
    val memDrive = new TriComp(xBasis+40, 270, 30, 30, "MEMDrive")
    memDrive.inputToComponent(mem)
    memDrive.outputToBus()
    memDrive.createActivator("DrMEM", -1)

    xBasis += 120

    val ir = new RectComp(xBasis, 50, 40, 30, "IR");
    ir.inputToBus()
    ir.createActivator("LdIR", -1)

    val signEx = new RectComp(xBasis + 5, 200, 60, 50, "sign\nextend");
    //signEx.outputToBus()
    val signDrive = new TriComp(xBasis+40, 270, 30, 30, "DrIFF")
    signDrive.inputToComponent(signEx)
    signDrive.outputToBus()
    signDrive.createActivator("DrOFF", -1)
    
    val eqZero = new RectComp(100, 360, 40, 30, "=0?");
    //eqZero.inputToBus()
      
    val Z = new RectComp(100, 410, 40, 30, "Z");
    Z.inputToComponent(eqZero)

  }
  //r.stroke = Red;
  val startWidth = 1024
  val startHeight = 768
  var currWidth: Double = startWidth;
  var currHeight: Double = startHeight;
  val bgW = 720;
  val bgH = 520;

  stage.widthProperty.addListener{ (o: javafx.beans.value.ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    currWidth = newVal.doubleValue //newVal.toString().toDouble;
    var scale = currWidth / startWidth;
    if (scale < 1) { scale = 1 } 
    for ((_, comp) <- DataPath.components) { comp.scale(scale) }
    DataPath.bg.width = bgW*scale
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
    children += InputManager.reset
    children += InputManager.instructionSelection
    children += InputManager.rdtextbox
    children += InputManager.sr1textbox
    children += InputManager.sr2textbox
    children += InputManager.execute
    children += InputManager.stepCounter
  }

  lazy val leftPane: Pane = new Pane {
    val regTableLabel = new Label("Register View")
      regTableLabel.layoutX = 20
      regTableLabel.layoutY = 30
      children += regTableLabel
    val memTableLabel = new Label("Memory View")
      memTableLabel.layoutX = 20
      memTableLabel.layoutY = 350
      children += memTableLabel

    //Adding left pane inputs
    children += InputManager.scrollpane1
    children += InputManager.regAddrBox
    children += InputManager.regValBox
    children += InputManager.setReg
    children += InputManager.scrollpane2
    children += InputManager.memAddrBox
    children += InputManager.memValBox
    children += InputManager.setMem

    
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
