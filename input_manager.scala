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
import javafx.beans.value.{ChangeListener, ObservableValue}

object InputManager {
//Top panel input
  //Forward button
  val stepForward = new Button("Step Forward")
    stepForward.setTooltip(
    new Tooltip("Simulates one clock cycle.")
    );
    stepForward.setDisable(true)
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
    stepBackward.setDisable(true)
    stepBackward.setMinWidth(120)
    stepBackward.onAction = (e:ActionEvent) => {
      stepBackwardPressed()
    }

  //reset button
  val reset = new Button("Reset")
    reset.setTooltip(new Tooltip("Reset Simulation"))
    reset.layoutX = 20
    reset.layoutY = 90
    reset.setDisable(true)
    reset.setMinWidth(120)
    reset.onAction = (e:ActionEvent) => {
      resetPressed()
    }

  // Instruction drop down menu
  var instructionMenuItemList = List[String]()

  //Dynamically update names based on the file system
  val loader = new JsonLoader()
  val instructions = loader.getInstructions() //get all instructions available
  for(instr <- instructions) {
    ///create menu item
    val newItem = instr.name
    //add to list
    instructionMenuItemList = instructionMenuItemList ::: List(newItem)
  }
  val instructionSelection = new ComboBox(instructionMenuItemList)
  instructionSelection.value = instructionSelection.items()(0)

  //TODO Make these locations relative to the size of the screen
  //Set up selection menu
  instructionSelection.layoutX = 300
  instructionSelection.layoutY = 58
  
  // first register input
  val sr1textbox = new TextField
    sr1textbox.promptText = "SR1"
    sr1textbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    sr1textbox.maxWidth = 50
    sr1textbox.layoutX = 530
    sr1textbox.layoutY = 60
    sr1textbox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateRegisters() }
    //= (e:ActionEvent) => {
    //  validateRegisters()
     // println("test")
    //}

  // second register input
  val sr2textbox = new TextField
    sr2textbox.promptText = "SR2"
    sr2textbox.setTooltip(
    new Tooltip("Enter a register.")
    );
    sr2textbox.maxWidth = 50
    sr2textbox.layoutX = 590
    sr2textbox.layoutY = 60

    sr2textbox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateRegisters() }

 // third register input
  val rdtextbox = new TextField
    rdtextbox.promptText = "RD"
    rdtextbox.setTooltip(
    new Tooltip("Enter a register or numerical value up to (range?)")
    );
    rdtextbox.maxWidth = 50
    rdtextbox.layoutX = 470
    rdtextbox.layoutY = 60
  
    rdtextbox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateRegisters() }


  // execute instruction button
  val execute = new Button("Execute")
    execute.setTooltip(
    new Tooltip("Simulates the execution of an entire instruction.")
    );
    execute.layoutX = 655
    execute.layoutY = 60
    execute.setDisable(true)
    execute.setMinWidth(120)
    execute.onAction = (e:ActionEvent) => {
      run()
    }
  //val topPaneInputs = Array(stepForward, stepBackward, instructionMenu, sr1textbox, sr2textbox, sr1textbox, execute)

  var stepCounter = new Text {
    x = 800
    y = 80
    text = "Step Counter: No Instruction"
    style = "-fx-font-size: 10pt"
    fill = Black
  }
  def updateStep(step: Int) {
    if (step == -1)
      stepCounter.text = "Step Counter: No Instruction"
    else if (step == 0)
      stepCounter.text = "Step Counter: Final Step"
    else
      stepCounter.text = "Step Counter: Step " + step
  }

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
        RegInfo("R7","0x0007"),
        RegInfo("R8","0x0008"),
        RegInfo("R9","0x0009"),
        RegInfo("R10","0x000A"),
        RegInfo("R11","0x000B"),
        RegInfo("R12","0x000C"),
        RegInfo("R13","0x000D"),
        RegInfo("R14","0x000E"),
        RegInfo("R15","0x000F")
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
    //2^16 entries in memory
    val numMemEntries = 65535
    for (i <- 0 to numMemEntries) {
      var str = Integer.toHexString(i)
      while(str.length < 4) { str = "0"+str }
      var hexstr = "0x"+str
      memData.addAll(MemInfo(hexstr,hexstr))
    }

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
    scrollpane2.layoutY = 380

 // reg edit input
  val regAddrBox = new TextField
  regAddrBox.promptText = "Which Register"
  regAddrBox.setTooltip(new Tooltip("Enter an address to set register value"));
  regAddrBox.maxWidth = 100
  regAddrBox.layoutX = 20
  regAddrBox.layoutY = 260
  regAddrBox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateRegVal() }

  val regValBox = new TextField
  regValBox.promptText = "Value"
  regValBox.setTooltip(new Tooltip("Enter a value to set register value"));
  regValBox.maxWidth = 100
  regValBox.layoutX = 20
  regValBox.layoutY = 290
  regValBox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateRegVal() }

  // set reg button
  val setReg = new Button("Set Reg")
  setReg.setTooltip(new Tooltip("Set register value."));
  setReg.layoutX = 20
  setReg.layoutY = 320
  setReg.setMinWidth(120)
  setReg.setDisable(true)
  setReg.onAction = (e:ActionEvent) => {
    updateReg(parseRegMemInput(regAddrBox.text()), parseRegMemInput(regValBox.text()))
  }

 // mem edit input
  val memAddrBox = new TextField
  memAddrBox.promptText = "Address"
  memAddrBox.setTooltip(new Tooltip("Enter an address to set memory value"));
  memAddrBox.maxWidth = 100
  memAddrBox.layoutX = 20
  memAddrBox.layoutY = 590
  memAddrBox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateMemVal() }


  val memValBox = new TextField
  memValBox.promptText = "Value"
  memValBox.setTooltip(new Tooltip("Enter a value to set memory value"));
  memValBox.maxWidth = 100
  memValBox.layoutX = 20
  memValBox.layoutY = 620
  memValBox.text.addListener{( O: javafx.beans.value.ObservableValue[_ <: java.lang.String], oldVal: java.lang.String, newVal: java.lang.String) => val t = validateMemVal() }

  // set mem button
  val setMem = new Button("Set Mem")
  setMem.setTooltip(new Tooltip("Set memory value."));
  setMem.layoutX = 20
  setMem.layoutY = 650
  setMem.setMinWidth(120)
  setMem.setDisable(true)
  setMem.onAction = (e:ActionEvent) => {
    //TODO fix
    updateMem(parseRegMemInput(memAddrBox.text()), parseRegMemInput(memValBox.text()))
  }

  val leftPaneInputs = Array(scrollpane1, regAddrBox, regValBox, setReg,
    scrollpane2, memAddrBox, memValBox, setMem)


  //Functions

  //Enables/Disables the proper buttons when the user starts stepping through a function
  def startStepThrough() {
    stepBackward.setDisable(false)
    reset.setDisable(false)
    execute.setDisable(true)
  } 

  def startExecute() {
    stepForward.setDisable(true)
  }

  def endExecute() {
    stepForward.setDisable(false)
  }

  def endStepThrough() {
    stepForward.setDisable(true)
  }

  def toggleExecute(on : Boolean) {
    execute.setDisable(on)
  }

  def resetButtons() {
    stepBackward.setDisable(true)
    stepForward.setDisable(false)
    reset.setDisable(true)
    execute.setDisable(false)
  }

  //checks all register input text boxes to see if they have input
  def validateRegisters() : Boolean =  {
    val invalidReg = ( (getRegisterInput("sr1") == -1) || (getRegisterInput("sr2") == -1) || (getRegisterInput("rd") == -1))
    if (!invalidReg) {
      //registers all are good
      execute.setDisable(false)
      stepForward.setDisable(false)
    } else {
      execute.setDisable(true)
      stepForward.setDisable(true)
    }

      //sr1 input invalid. Considers empty to be valid entry
      if ((rawRegisterInput("sr1").length() > 0) && getRegisterInput("sr1") == -1) {
          println("s12 invalid")
          sr1textbox.setTooltip(new Tooltip("Please enter a valid register name" ))
          sr1textbox.setStyle("-fx-border-color:red;")
          //sr1textbox.tooltip.isActivated = true
      } else {
          sr1textbox.setStyle("-fx-border-color:none;")
      }

      //sr2 input invalid. Considers empty to be valid entry
      if ((rawRegisterInput("sr2").length() > 0) && getRegisterInput("sr2") == -1) {
          println("sr2 invalid")
          sr2textbox.setTooltip(new Tooltip("Please enter a valid register name" ))
          sr2textbox.setStyle("-fx-border-color:red;")
          //sr1textbox.tooltip.isActivated = true
      } else { 
          sr2textbox.setStyle("-fx-border-color:none;")
      }


      //rd input invalid. Considers empty to be valid entry
      if ((rawRegisterInput("rd").length() > 0) && getRegisterInput("rd") == -1) {
          println("sr2 invalid")
          rdtextbox.setTooltip(new Tooltip("Please enter a valid register" ))
          rdtextbox.setStyle("-fx-border-color:red;")
          //sr1textbox.tooltip.isActivated = true
      } else {
          rdtextbox.setStyle("-fx-border-color:none;")
      }


   return !invalidReg 
  }

  //converts proper mem/reg value input or returns -1
  def parseRegMemInput(str: String) : Int = {
      //regex for a hexstring
      val pattern = new Regex("^0x((A|B|C|D|E|F)|\\d){4}$")
      var matched = (pattern findAllIn str).mkString("")
      if (matched.length == 0) {
        //test for regular int
        val otherPattern = new Regex("^[r|R]{0,1}\\d+$")
        val pureDigit = new Regex("\\d+")
        matched = (pureDigit findAllIn str).mkString("")
        if ((otherPattern findAllIn str).length != 1) {
          //error from both
          return -1
        } else {
          return Integer.parseInt(matched, 10)
        }
      }
      //found a hex string
      return convertHexString(matched)
  }

  def validateMemReg(addrBox: TextField, valBox: TextField, btn: Button, name: String) : Boolean = {
      println("validation")
      val addrInput = addrBox.text()
      val valInput = valBox.text()
      val addr = parseRegMemInput(addrInput)
      val value = parseRegMemInput(valInput)
      
      println(addr)
      println(value)

      if ((addr == -1) || (value == -1)) {
        //invalid input
        btn.setDisable(true)
      } else {
        //input valid
        btn.setDisable(false)
      }

      //address input invalid. Considers empty to be valid entry
      if ((addrInput.length() > 0) && (addr == -1)) {
          println("memory address invalid")
          addrBox.setTooltip(new Tooltip("Please enter a valid address" ))
          addrBox.setStyle("-fx-border-color:red;")
      } else {
          addrBox.setStyle("-fx-border-color:none;")
      }

      //value input invalid. Considers empty to be valid entry
      if ((valInput.length() > 0) && (value == -1)) {
          println("memory address invalid")
          valBox.setTooltip(new Tooltip("Please enter a valid value") )
          valBox.setStyle("-fx-border-color:red;")
      } else {
          valBox.setStyle("-fx-border-color:none;")
      }

      return false

  }

  //checks both memory update values for a correct input
  def validateMemVal() : Boolean = {
    return validateMemReg(memAddrBox, memValBox,setMem, "memory")
  }

  //checks both register update values for correct input
  def validateRegVal() : Boolean =  {
      return validateMemReg(regAddrBox, regValBox, setReg, "register")
  }

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


  //takes in string specifying sr1,sr2,or sr3, returns the raw input currently in that register
  def rawRegisterInput(reg : String ) : String = {
    reg.toLowerCase()
    var selected = new TextField
    reg match {
      case "sr1" => selected = sr1textbox
      case "sr2" => selected = sr2textbox
      case "rd" => selected = rdtextbox
      case woah => return " "
    }
    return selected.text()
  }

  //parses register input text values
  def parseRegisterInput(input : String ) : Int = {
    val pattern = new Regex("\\d+")
    val matched = (pattern findAllIn input)
    if (matched.length <= 0) {
      //TODO Handle error
      return -1
    }
    val str = (pattern findAllIn input).mkString("")
    return Integer.parseInt(str, 10)
  }
  
  //takes in a string with a register name and returns the integer value
  //of that register
  def getRegisterInput(reg : String) : Int = {
    val str = rawRegisterInput(reg)
    val input = parseRegisterInput(str)
    
    return parseRegisterInput(str)
  }

  //Retreives the value currently on the screen at memory location
  def getMemVal(location: Int) :  Int = {
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

  def currentInstructionNum(): Int = {
    var num = 0
    for (i <- 0 until instructions.length)
      if (instructions(i).name == instructionSelection.value.value)
        num = i
    return num
  }
  def thisInstructionNum(ins: Instruction): Int = {
    var num = 0
    for (i <- 0 until instructions.length)
      if (instructions(i) == ins)
        num = i
    return num
  }

  //Tells the simulation manager to run an entire instruction
  def run() {
    SimulationManager.runInstruction(currentInstructionNum())
  }

  //Tells simulation manager to complete one step of an instruction
  def stepForwardPressed() {
    SimulationManager.stepInstruction(currentInstructionNum())
  }

  //Tells simulation manager to go back to the previous instruction step
  def stepBackwardPressed() {
    println("step backward pressed")
    val currentStep = SimulationManager.currentStep
    resetPressed()
    for (i <- 0 until currentStep-1)
      SimulationManager.stepInstruction(currentInstructionNum())
  }

  def resetPressed() {
    for (i <- 0 until regData.size())
      updateReg(i,i)
    for (i <- 0 until memData.size())
      updateMem(i,i)
    resetButtons()
    SimulationManager.resetInstructionSimulation(true)
    var completedInstructionsFromBeginning = SimulationManager.completedInstructionsFromBeginning
    SimulationManager.completedInstructionsFromBeginning = new ArrayBuffer[Instruction]()
    for (ins <- completedInstructionsFromBeginning) {
      for (i <- 0 until ins.steps.length())
        SimulationManager.stepInstruction(thisInstructionNum(ins))
    }
  }

  //Retreives the name of the instruction currently selected in the instruction menu selection
  def getSelectedInstruction() {
    //TODO
  }
}
