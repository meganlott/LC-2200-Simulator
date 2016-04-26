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

/** 
 * A superclass for all components of the datapath
 * For example, ALU and PC
 */
abstract class Component {

  // The name of the component
  val name: String

  // A list of activators
  // These are things like drivers
  var activators: ArrayBuffer[Activator] = new ArrayBuffer[Activator]()

  // Number of inputs into the component
  var inputs: Int = 1

  // A list of all of the wires leading into the component
  protected var inWires: Array[Wire] = new Array[Wire](1)

  // The wire going out of the component
  var outWire: Wire = null;

  /**
   * Returns an array of the values on the input wires
   * leading into the component
   *
   * @return an Array of shorts
   */
  def readInputData(): Array[Short] = {
    var data = new Array[Short](inputs)
    for (i <- 0 until inputs) {
      data(i) = inWires(i).value
    }
    return data;
  }

  /**
   * Sets the value on the output wire
   *
   * @param data the data to push to the output wire
   */
  def setOutputData(data: Short) {
    if (outWire != null)
      outWire.setValue(data)
  }

  /**
   * Sets the number of input wires
   *
   * @param num the number of input wires
   */
  def setNumberOfInputs(num: Int) {
    inputs = num;
    inWires = new Array[Wire](num)
  }

  /**
   * Gets the proper location for an input wire
   * Must be overriden by subclass
   *
   * @param input which input
   * @return a tuple of location
   */
  def getInputLocation(input: Int = 0): (Double, Double)

  /**
   * Gets the proper location for the output wire
   * Must by overriden by subclass
   *
   * @return a tuple of location
   */
  def getOutputLocation(): (Double, Double)

  /**
   * Connects the input of the component to the BUS
   *
   * @param input which input to connect
   * @param top whether to connect to the top or bottom BUS line
   */
  def inputToBus(input: Int = 0, top: Boolean = true) {
    val loc = getInputLocation(input)
    inWires(input) = new RealWire(loc._1, if (top) DataPath.busTop else DataPath.busBottom, loc._1, loc._2)
    DataPath.bus.add(inWires(input))
  }

  /**
   * Connects the output of the component to the BUS
   */
  def outputToBus() {
    val loc = getOutputLocation()
    DataPath.bus.add(new RealWire(loc._1, loc._2, loc._1, DataPath.busBottom))
    outWire = DataPath.bus
  }
  
  /**
   * Connected the input of the component to the output of another component
   *
   * @param c the component to connect to
   * @param input which input to connect to the component
   */
  def inputToComponent(c: Component, input: Int = 0) {
    val loc1 = c.getOutputLocation()
    val loc2 = getInputLocation(input)
    val newWire = new RealWire(loc1._1, loc1._2, loc1._1, loc2._2)
    inWires(input) = newWire
    c.outWire = newWire
  }

  /**
   * Creates a new activator
   * Must be overriden by the subclass
   *
   * @param n the name of the activator
   * @param dir which direction to push the activator, -1 for left, 1 for right, or 0 for on the component
   */
  def createActivator(n: String, dir: Int)
  
  /**
   * Adds a created activator to the component as well as the DataPath
   *
   * @param a the activator to add
   */
  def addActivator(a: Activator) {
    activators += a
    DataPath.addActivator(a)
  }

  /**
   * Scale the component when the screen scales
   * Must be overriden by the subclass
   *
   * @param sx scaling factor in x direction
   * @param sy scaling factor in y direction
   */
  def scale(sx: Double, sy: Double)
}

/**
 * An activator for a component
 *
 * @param xx x location
 * @param yy y location
 * @param n name
 * @param s source component
 * @param flip direction to flip the component
 */
class Activator(val xx: Double, val yy: Double, val n: String, val s: Component, val flip: Int = 0) {
  val name = n;
  val source = s;
  var offset = 12 * flip;
  // The line we draw from the component to ourself
  val shape = Line(xx + offset, yy, xx, yy)
  // Only add the line if we are actually outside the component
  if (flip != 0) {
    shape.stroke = Black
    shape.strokeWidth = 1
    DataPath.pane.children += shape
  }
  
  // Our label
  var text = new Text {
    x = xx
    y = yy + 3
    text = name
    style = "-fx-font-size: 8pt"
    fill = Black
  }
  
  // Move the text to the proper location
  moveText(1.0, 1.0)
  // Add ourselves to the pane
  DataPath.pane.children += text

  /**
   * Activates the activator
   * Returns the inputData from our source
   *
   * @return an Array of the values from our source's input wires
   */
  def activate(): Array[Short] = {
    shape.stroke = Red
    text.fill = Red
    return source.readInputData()
  }

  /**
   * Stop looking activated
   * This is for the simulator
   */
  def deactivate() {
    shape.stroke = Black
    text.fill = Black
  }

  /**
   * Move the text to the proper location
   *
   * @param sc the x scaling factor
   * @param sc2 the y scaling factor
   */
  def moveText(sc: Double, sc2: Double) {
    var offset = 12 * flip * sc;
    if (flip != 0) {
      offset = flip * 16;
      if (flip > 0) {
        offset += 3
      } else if (flip < 0) {
        offset += 1
      }
    }
    val j: Double = text.getLayoutBounds().getWidth()
    if (flip < 0) {
      text.x = (xx + offset) * sc - j
    } else {
      text.x = xx*sc + offset*sc
    }
    text.y = (yy + 3) * sc2
  }

  /**
   * Scale when the screen scales
   *
   * @param sc the x scaling factor
   * @param sc2 the y scaling factor
   */
  def scale(sc: Double, sc2: Double) {
    shape.startX = xx*sc + 12 * flip * sc;
    shape.endX = xx*sc
    shape.startY = yy * sc2
    shape.endY = yy * sc2
    moveText(sc, sc2)
  }
}

/**
 * The DataPath object
 * Holds the BUS and a list of components and their activators
 */
object DataPath {

  // Bus Y location
  val busTop = 20;
  val busBottom = 320;

  // Background rectangle
  var bg: Rectangle = null

  // Source pane
  var pane: Pane = null;

  // List of components and activators
  var components: Map[String, Component] = Map()
  var activators: Map[String, Activator] = Map()

  // The BUS. It is a WireSet as it is many wires.
  val bus: WireSet = new WireSet(450,350)

  /**
   * Activate an activator
   *
   * @param s name of the activator
   * @param f a function that takes in the input wire values and returns the output value
   */
  def activate(s: String, f: Array[Short]=>Short) {
    activators(s).s.setOutputData(f(activators(s).activate()))
  }

  /**
   * Deactivate an activator
   *
   * @param s name of the activator
   */
  def deactivate(s: String) = activators(s).deactivate()

  /**
   * Deactivates all activators
   */
  def deactivateAll() {
    for((key,value) <- activators)
      value.deactivate()
  }

  /**
   * Adds a new activator
   *
   * @param a activator to add
   */
  def addActivator(a: Activator) {
    activators += (a.name -> a)
  }

  /**
   * Adds a new component
   *
   * @param c component to add
   */
  def addComponent(c: Component) {
    components += (c.name -> c)
  }
}

/**
 * A Rectangular component (as opposed to a triangular component)
 * Extends Component
 *
 * @param xx x position
 * @param yy y position
 * @param w width
 * @param h height
 * @param n name
 */
class RectComp(val xx: Double, val yy: Double, val w: Double, val h: Double, val n: String) extends Component{
  // Location
  var x = xx;
  var y = yy;

  // The rectangular shape itself
  val shape = Rectangle(xx, yy, w, h)
  val name = n;
  shape.fill = White
  shape.stroke = Black
  shape.strokeWidth = 2

  // Whether or not we have to shift our label to make room for an activator
  var shiftText = false;

  // Our label
  var text = new Text {
    x = 0
    y = yy + 20
    text = name
    font = Font.font(null, FontWeight.Thin, 12)
    style = "-fx-font-size: 12pt"
    fill = Black
  }
  // Set location
  text.x = xx + w/2 - text.getLayoutBounds().getWidth()/2 - 3

  // Add everything to the datapath & pane
  DataPath.pane.children += shape
  DataPath.pane.children += text
  DataPath.addComponent(this)

  /**
   * Get the proper location of an input for a wire
   *
   * @param input which input
   * @return a tuple of location
   */
  def getInputLocation(input: Int = 0): (Double, Double) = {
    var step = w / (inputs + 1)
    return (x + step * (input+1), y)
  }

  /**
   * Get the proper location of the output for a wire
   *
   * @return a tuple of location
   */
  def getOutputLocation(): (Double, Double) = {
    return (x + w / 2, y + h)
  }

  /**
   * Scale the component as the screen scales
   *
   * @param s the x scaling factor
   * @param s2 the y scaling factor
   */
  def scale(s: Double, s2: Double) {
    text.x = (xx + w/2) * s - text.getLayoutBounds().getWidth()/2
    if (!shiftText) {
      text.y = (y + 20) * s2
    } else {
      text.y = (y + 40) * s2
    }
    shape.x = xx * s
    shape.y = yy * s2
    shape.width = s * w
    shape.height = s2 * h


    // Scale wires and activators

    for (wire <- inWires) {
      if (wire != null)
        wire.scale(s, s2)
    }
    if (outWire != null)
      outWire.scale(s, s2)

    for (a <- activators) { a.scale(s, s2) }
  }

  /** 
   *  Creates a new Activator
   *
   *  @param n the name
   *  @param dir the direction
   */
  def createActivator(n: String, dir: Int) {
    var sx = x
    if (dir > 0) {
      sx = x + w
    } else if (dir == 0) {
      shiftText = true
      sx = getInputLocation(activators.size)._1 - 24
      text.y = y + 40
    }
    var a = new Activator(sx, y+15, n, this, dir)
    addActivator(a)
  }
}

/**
 * A Triangular component (as opposed to a rectangular component)
 * Extends Component
 *
 * @param xx x position
 * @param yy y position
 * @param w width
 * @param h height
 * @param n name
 */
class TriComp(val xx: Double, val yy: Double, val w: Double, val h: Double, val n: String) extends Component{
  // Location
  var x = xx;
  var y = yy;
  // Our triangular shape
  var shape = Polygon(xx-w/2,yy,xx+w/2,yy,xx,yy+h);
  shape.fill = White
  shape.stroke = Black
  shape.strokeWidth = 2
  // Add things to the datapath and pane
  DataPath.pane.children += shape
  val name = n;
  DataPath.addComponent(this)

  /**
   * Gets the proper location for an input wire
   *
   * @param input which input
   * @return a tuple of location
   */
  def getInputLocation(input: Int = 0): (Double, Double) = {
    return (x, y)
  }

  /**
   * Gets the proper location for the output wire
   *
   * @return a tuple of location
   */
  def getOutputLocation(): (Double, Double) = {
    return (x, y+h)
  }

  /** 
   *  Creates a new Activator
   *
   *  @param n the name for the activator
   *  @param dir the direction
   */
  def createActivator(n: String, dir: Int) {
    var sx = x - w/2;
    if (dir > 0) {
      sx = x + w/2;
    }
    var a = new Activator(sx, y+15, n, this, dir)
    addActivator(a)
  }

  /** 
   *  Scales when the screen scales
   *
   *  @param s the x scaling factor
   *  @param s2 the y scaling factor
   */
  def scale(s: Double, s2: Double) {
    val temp = Polygon(xx*s-s*w/2,yy*s2,xx*s+s*w/2,yy*s2,xx*s,yy*s2+h*s2);
    temp.fill = White
    temp.stroke = Black
    temp.strokeWidth = 2
    DataPath.pane.children -= shape
    shape = temp
    DataPath.pane.children += shape

    for (wire <- inWires) {
      if (wire != null)
        wire.scale(s, s2)
    }
    if (outWire != null)
      outWire.scale(s, s2)
    for (a <- activators) { a.scale(s, s2) }
  }
}

/** A superclass for all wires, meaning WireSet and RealWire
 */
abstract class Wire() {
  // The value on the wire
  var value: Short = 0;

  /** 
   *  Set the value of the wire
   *
   *  @param value the value to set
   */
  def setValue(value: Short)
  
  /** 
   *  Returns the value on the wire
   *
   *  @return the value
   */
  def getValue(): Short = {
    return value
  }

  /**
   *  Scale the wire to match the screen scaling
   *
   *  @param sx the x scaling factor
   *  @param sy the y scaling factor
   */
  def scale(sx: Double, sy: Double)
}

/** 
 *  A singular wire
 *  Extends Wire
 *
 *  @param sx starting x for the line
 *  @param sy start y
 *  @param ex end x
 *  @param ey end t
 */
class RealWire(val sx: Double, val sy: Double, val ex: Double, val ey: Double) extends Wire {
  // The lines
  val shape = Line(sx, sy, ex, ey);
  shape.stroke = Black
  shape.strokeWidth = 3
  // A bounding box for hovering
  val bounding = Rectangle(sx-6, sy-6, ex-sx+12, ey-sy+12);
  bounding.stroke = TRANSPARENT
  bounding.fill = TRANSPARENT

  DataPath.pane.children += shape
  DataPath.pane.children += bounding
  // Value label
  var text = new Text {
    x = (sx+ex)/2 + 6
    y = (sy+ey)/2 + 12
    text = InputManager.formatInt(value)
    font = Font.font(null, FontWeight.Bold, 18)
    style = "-fx-font-size: 8pt"
    fill <== when (bounding.hover) choose Red otherwise TRANSPARENT
  }
  DataPath.pane.children += text

  /**
   * Scale as the screen scales
   *
   * @param s the x scaling factor
   * @param y the y scaling factor
   */
  def scale(s: Double, s2: Double) {
    shape.startX = sx * s
    shape.startY = sy * s2
    shape.endX = ex * s
    shape.endY = ey * s2
    bounding.x = (sx-6)*s
    bounding.y = (sy-6)*s2
    bounding.width = (ex-sx+12)*s
    bounding.height = (ey-sy+12)*s2
    text.x = ((sx+ex)/2 + 6) * s
    text.y = ((sy+ey)/2 + 12) * s2
  }

  /** 
   *  Set the value of the wire
   *  @param value the value to put on the wire
   */
  def setValue(value: Short) = {
    this.value = value;
    text.text = InputManager.formatInt(value)
  }
}

/**
 * A set of wires
 * Exists to allow one to treat this as a singular wire
 * Currently only used for the BUS
 * Extends Wire
 *
 * @param xin x location of bus value display
 * @param yin y location of bus value display
 */
class WireSet(val xin: Double, val yin: Double) extends Wire{
  // Our wires
  val wires = new ArrayBuffer[Wire]();
  // The giant label for the WireSet
  var text = new Text {
    x = xin
    y = yin
    text = "Value on bus: " + InputManager.formatInt(value)
    font = Font.font(null, FontWeight.Bold, 18)
    style = "-fx-font-size: 16pt"
  }
  /**
   * The WireSet is created before a Pane is set,
   * so this must be called after creation
   */
  def finishSetup() {
    DataPath.pane.children += text
  }

  /**
   * Set the value on the wire(s)
   *
   * @param v the value to set
   */
  def setValue(v: Short) = {
    for (wire <- wires) {
      wire.setValue(v);
    }
    text.text = "Value on bus: " + InputManager.formatInt(v)
  }

  /**
   * Scale as the screen scales
   *
   * @param s x scaling factor
   * @param s2 y scaling factor
   */
  def scale (s: Double, s2: Double)  = {
    for (wire <- wires) {
      wire.scale(s, s2);
    }
    text.x = xin*s
    text.y = yin*s2
  }

  /**
   * Add a wire to the wireset
   *
   * @param wire the wire to add
   * @return returns this wire
   */
  def add(wire: Wire): Wire = {
    wire.setValue(value)
    wires.append(wire)
    return wire
  }
}

/**
 * Placeholder class needed for register table view
 *
 * @param name register number
 * @param mem the value
 */
case class RegInfo(name:String, mem:String)

/**
 * Placeholder class needed for memory table view
 *
 * @param name memory address
 * @param mem the value
 */
case class MemInfo(addr:String, mem:String)

/**
 * The big kahuna
 */
object LC2200Simulator extends JFXApp {
  /**
   * The window of the application
   */
  stage = new JFXApp.PrimaryStage {
    title.value = "LC-2200 Simulator"
    width = 1024
    height = 740
    scene = new Scene(new javafx.scene.Scene(root))
  }
  stage.minWidth = stage.width.get;
  stage.minHeight = stage.height.get;

  // Root of the application
  lazy val root = new BorderPane{
    center = simulatorPane
    top = topPane
    topPane.setMinHeight(100)
    left = leftPane
    leftPane.setMinWidth(250)
  }

  // The pane for the datapath itself
  lazy val simulatorPane: Pane = new Pane {
    
    // The background rectangle
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
    // Create the bus
    DataPath.bus.finishSetup()
    DataPath.bus.add(new RealWire(20,20,720,20))
    DataPath.bus.add(new RealWire(20,20,20,320))
    DataPath.bus.add(new RealWire(20,320,720,320))

    // x location to create components
    var xBasis = 90

    // Create the PC, add its activators and driver
    val pc = new RectComp(xBasis, 50, 40, 30, "PC");
    pc.inputToBus(0)
    pc.createActivator("LdPC", -1)
    val pcDrive = new TriComp(xBasis+20,270,30,30,"PCDrive");
    pcDrive.inputToComponent(pc)
    pcDrive.outputToBus()
    pcDrive.createActivator("DrPC", -1)

    xBasis += 110

    // Create the ALU, A, B, the activators and driver
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
    
    // Create the registers box, activators, driver
    val regs = new RectComp(xBasis, 120, 80, 100, "registers");
    regs.inputToBus()
    regs.createActivator("Din", 0)
    regs.createActivator("WrREG", -1)
    val regDrive = new TriComp(xBasis+40, 270, 30, 30, "REGDrive");
    regDrive.inputToComponent(regs)
    regDrive.outputToBus()
    regDrive.createActivator("DrREG", -1)

    xBasis += 160

    // Create the MAR and Memory and driver and activators
    val mar = new RectComp(xBasis, 50, 40, 30, "MAR");
    mar.inputToBus()
    mar.createActivator("LdMAR", -1)
    val mem = new RectComp(xBasis, 120, 80, 100, "memory\n2^16 x\n16 bits");
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

    // Create the IR
    val ir = new RectComp(xBasis, 50, 40, 30, "IR");
    ir.inputToBus()
    ir.createActivator("LdIR", -1)

    // Create SignEx and driver
    val signEx = new RectComp(xBasis + 5, 200, 60, 50, "sign\nextend");
    val signDrive = new TriComp(xBasis + 35, 270, 30, 30, "DrOFF")
    signDrive.inputToComponent(signEx)
    signDrive.outputToBus()
    signDrive.createActivator("DrOFF", -1)
    
    // Z
    val Z = new RectComp(100, 410, 40, 30, "Z");
    Z.inputToBus(0,false)
    Z.createActivator("LdZ", -1)

    // Create EqZero
    val eqZero = new RectComp(100, 360, 40, 30, "=0?");
  }

  // Values to track scaling of window
  val startWidth = 1024
  val startHeight = 740
  var currWidth: Double = startWidth;
  var currHeight: Double = startHeight;
  val bgW = 720;
  val bgH = 520;

  // Called when the width of the window changes
  stage.widthProperty.addListener{ (o: javafx.beans.value.ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    currWidth = newVal.doubleValue //newVal.toString().toDouble;
    scaleEverything()
  }

  // Called when the height of the window changes
  stage.heightProperty.addListener{ (o: javafx.beans.value.ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
    currHeight = newVal.doubleValue //newVal.toString().toDouble;
    scaleEverything()
  }

  /**
   * Rescale the datapath with screen size
   */
  def scaleEverything() {
    var scalex = currWidth / startWidth;
    var scaley = currHeight / startHeight;
    if (scalex < 1) { scalex = 1 } 
    if (scaley < 1) { scaley = 1 } 
    for ((_, comp) <- DataPath.components) { comp.scale(scalex, scaley) }
    DataPath.bg.width = bgW*scalex
    DataPath.bg.height = bgH*scaley
  }

  // The top pane, grabs most of its things from InputManager
  lazy val topPane: Pane = new Pane {
    val title = new Label("LC-2200 Simulator")
    title.layoutX = 20
    title.layoutY = 0
    title.style = "-fx-font-size: 36pt"
    children += title

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

  // The left pane, grabs most of its things from InputManager
  lazy val leftPane: Pane = new Pane {
    val regTableLabel = new Label("Register View")
      regTableLabel.layoutX = 20
      regTableLabel.layoutY = 10
      children += regTableLabel
    val memTableLabel = new Label("Memory View")
      memTableLabel.layoutX = 20
      memTableLabel.layoutY = 300
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

  // Set the icon of the application
  stage.getIcons().add(new Image("file:CPU.png"))
}
