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
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, TableView, TableColumn, ScrollPane, Menu, MenuItem, MenuBar, Label, TextField}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.{StringProperty}
import scala.collection.mutable.ArrayBuffer

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
  //def getSceneComponent(): Node;
}

class Activator(val xx: Double, val yy: Double, val n: String, val s: Component, val flip: Int = 0) {
  val name = n;
  val source = s;
  var offset = 12;
  val shape = Line(xx + offset, yy, xx, yy)
  if (flip != 0) {
    offset = flip * 16;
    shape.stroke = Black
    shape.strokeWidth = 1
    DataPath.pane.children += shape
    if (flip < 0) {
      offset += flip * 42
    } else {
      offset += 3
    }
  }
  
  var text = new Text {
    x = xx + offset
    y = yy + 3
    text = name
    style = "-fx-font-size: 8pt"
    fill = Black
  }
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
}

object DataPath {

  val busTop = 20;
  val busBottom = 320;

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

  var text = new Text {
    x = xx + 5
    y = yy + 20
    text = name
    style = "-fx-font-size: 12pt"
    fill = Black
  }

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

  def createActivator(n: String, dir: Int) {
    var sx = x
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
  val shape = Polygon(xx-w/2,yy,xx+w/2,yy,xx,yy+h);
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
}

abstract class Wire() {

  var value: Short = 0;

  def setValue(value: Short)
  def getValue(): Short = {
    return value
  }
}
class RealWire(val sx: Double, val sy: Double, val ex: Double, val ey: Double) extends Wire {

  val shape = Line(sx, sy, ex, ey);
  shape.stroke = Black
  shape.strokeWidth = 3

  DataPath.pane.children += shape

  var text = new Text {
    x = (sx+ex)/2
    y = (sy+ey)/2
    text = InputManager.formatInt(value)
    style = "-fx-font-size: 8pt"
    fill = Black
  }
  DataPath.pane.children += text


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
    stepForward.onAction = (e:ActionEvent) => {
      stepForwardPressed()
    }

  //Backwards button
  val stepBackward = new Button("Step Backward")
    stepBackward.setTooltip(
    new Tooltip("Moves back one clock cycle.")
    );
    stepBackward.layoutX = 20
    stepBackward.layoutY = 60
    stepBackward.setMinWidth(120)
    stepBackward.onAction = (e:ActionEvent) => {
      stepBackwardPressed()
    }

  // Instruction drop down menu
  val instructionSelection = new MenuBar
  val instructionMenu = new Menu("Choose Instruction")
  var instructionMenuItemList = List[MenuItem]()

  //Dynamically update names based on the file system
  val loader = new JsonLoader()
  val instructions = loader.getInstructions() //get all instructions available
  for(instr <- instructions) {
    ///create menu item
    val newItem = new MenuItem(instr.name)
    //add to list
    instructionMenuItemList = instructionMenuItemList ::: List(newItem)
  }

  //TODO Make these locations relative to the size of the screen
  //Set up selection menu
  instructionMenu.items = instructionMenuItemList
  instructionSelection.menus = List(instructionMenu)
  instructionSelection.layoutX = 300
  instructionSelection.layoutY = 58
  
  // first register input
  val rxtextbox = new TextField
    rxtextbox.promptText = "RX"
    rxtextbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    rxtextbox.maxWidth = 50
    rxtextbox.layoutX = 470
    rxtextbox.layoutY = 60

  // second register input
  val rytextbox = new TextField
    rytextbox.promptText = "RY"
    rytextbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    rytextbox.maxWidth = 50
    rytextbox.layoutX = 530
    rytextbox.layoutY = 60

 // third register input
  val rztextbox = new TextField
    rztextbox.promptText = "RZ"
    rztextbox.setTooltip(
    new Tooltip("Enter a register or numerical value up to (range?)")
    );
    rztextbox.maxWidth = 50
    rztextbox.layoutX = 590
    rztextbox.layoutY = 60

  // execute instruction button
  val execute = new Button("Execute")
    execute.setTooltip(
    new Tooltip("Simulates the execution of an entire instruction.")
    );
    execute.layoutX = 655
    execute.layoutY = 60
    execute.setMinWidth(120)
    execute.onAction = (e:ActionEvent) => {
      run()
    }
  val topPaneInputs = Array(stepForward, stepBackward, instructionMenu, rxtextbox, rytextbox, rxtextbox, execute)

//Left panel inputs
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

  val leftPaneInputs = Array(scrollpane1, scrollpane2)

  //Functions

  //Takes in an integer value and formats it for the UI
  def formatInt( i: Int) : String = {
      val hexstring = Integer.toHexString(i)
      var formatedString = "0x"
      while (formatedString.length() < (6 - hexstring.length())) {
        formatedString += "0"
     }
      return formatedString + hexstring
    }

//Takes in an integer location and an integer value then updates the memory location in
  //datapathstate and the UI
  def updateMem(location: Int, value: Int) {

    memData(location) = MemInfo(formatInt(location), formatInt(value))
  } 

  //Retreives the value currently on the screen at memory location
  def getMemVal(location: Int) : Int = {
    var value = memData(location).mem
    return convertHexString(value)
  }

  //Takes in an integer location and an integer value then updates register location in
  //datapathstate and the UI
  def updateReg(location: Int, value: Int) {
    regData(location) = RegInfo("R" + location.toString(), formatInt(value))
  }

  //Retreives the value currently on the screen at the register location
  def getRegVal(location: Int) : Int  =  {
    var value = regData(location).mem
    return convertHexString(value)
  }

  //Parse UI string value
  def convertHexString(str: String) : Int = {
    val l = str.length()
    var sub = str.substring(2, str.length() )
    return Integer.parseInt(sub, 16)
  }

  //Tells the simulation manager to run an entire instruction
  def run() {
    SimulationManager.runInstruction(0)
  }

  //Tells simulation manager to complete one step of an instruction
  def stepForwardPressed() {
    SimulationManager.stepInstruction(0)
  }

  //Tells simulation manager to go back to the previous instruction step
  def stepBackwardPressed() {
    //TODO
    println("step backward pressed")
  }

  //Retreives the name of the instruction currently selected in the instruction menu selection
  def getSelectedInstruction() {
    //TODO
  }
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

    xBasis += 150
    
    val regs = new RectComp(xBasis, 120, 80, 100, "registers");
    regs.inputToBus()
    regs.createActivator("Din", 0)
    regs.createActivator("WrREG", -1)
    val regDrive = new TriComp(xBasis+40, 270, 30, 30, "REGDrive");
    regDrive.inputToComponent(regs)
    regDrive.outputToBus()
    regDrive.createActivator("DrREG", -1)

    xBasis += 150

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
    
    val eqZero = new RectComp(100, 360, 40, 30, "=0?");
    //eqZero.inputToBus()
      
    val Z = new RectComp(100, 410, 40, 30, "Z");
    Z.inputToComponent(eqZero)
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
    val memTableLabel = new Label("Memory View")
      memTableLabel.layoutX = 20
      memTableLabel.layoutY = 260
      children += memTableLabel

    //Adding left pane inputs
    children += InputManager.scrollpane1
    children += InputManager.scrollpane2

    
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

