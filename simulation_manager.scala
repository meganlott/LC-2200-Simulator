import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import ExecutionContext.Implicits.global

object SimulationManager {
  val waitTime = 2000
  val loader = new JsonLoader()
  val instructions = loader.getInstructions() //get all instructions available
  var currentInstruction: Option[Instruction] = None
  var currentStep = 0
  var completedInstructionsFromBeginning = new ArrayBuffer[Instruction]()
  // this is the update order of the datapath, so the order is very important
  val possibleSignals = List("UseSR2Hack", "DrPC", "ALUFunc", "ALUadd", "ALUnand", "ALUsub", "ALUinc", "DrALU", "Din", "DrMEM", "WrREG", "DrREG", "LdPC", "LdZ", "UseDESTHack", "StoreSR1Hack", "DrOFF", "LdMAR", "Addr", "Din", "WrMEM", "LdA", "LdB", "LdIR")
  // These are the keys/signals that just communicate information to the Simulationmanager, and don't have a component in the datapath 
  var ignoreKeys = List("ALUadd", "ALUnand", "ALUsub", "ALUinc", "UseDESTHack", "StoreSR1Hack", "UseSR2Hack")
  def stepInstruction(i: Int) {
    println("Current step: " + currentStep)
    InputManager.updateStep(currentStep+1);
    if (!currentInstruction.isDefined || currentInstruction.get != instructions(i)) {
      currentInstruction = Some(instructions(i))
      currentStep = 0
      InputManager.startStepThrough()
    }
    var step = currentInstruction.get.getSignals(currentStep) //get all signals for step 0 of add
    // first make sure regs is outputing right info
    def updateRegMemOffOutput() = {
      val output = (
        if (step("UseSR2Hack")) {
          // Don't use the immediate value to index into the registers
          if (!step("DrOFF")) {
            InputManager.getRegVal( InputManager.getRegisterInput("sr2") )
          } else 0
        } else if (step("UseDESTHack")) {
          InputManager.getRegVal( InputManager.getRegisterInput("rd") )
        } else {
          InputManager.getRegVal( InputManager.getRegisterInput("sr1") )
        }
        ).toShort
      DataPath.components("registers").setOutputData(output)
      DataPath.components("memory\n2^16 x\n16 bits").setOutputData(InputManager.getMemVal(DataPath.components("memory\n2^16 x\n16 bits").readInputData()(0)).toShort)
      DataPath.components("sign\nextend").setOutputData(InputManager.getRegisterInput("sr2").toShort)
      output
    }
    updateRegMemOffOutput()
    for( key <- possibleSignals;
         value = step(key)) {
      def activateFunc(inputs: Array[Short]) = {
        if (key == "ALUFunc") {
          if (step("ALUadd"))
            inputs.reduceLeft((j,k)=>(j+k).toShort)
          else if (step("ALUinc"))
            (inputs(0)+1).toShort
          else if (step("ALUsub"))
            (inputs(0)-inputs(1)).toShort
          else if (step("ALUnand"))
            (~(inputs(0)&inputs(1))).toShort
          else {
            println("BAD INSTRUCTION")
            println(step)
            0
          }
        } else if (key == "WrREG") {
          if (step("StoreSR1Hack"))
            InputManager.updateReg(InputManager.getRegisterInput("sr1") ,inputs(0))
          else
            InputManager.updateReg(InputManager.getRegisterInput("rd") ,inputs(0))
          updateRegMemOffOutput()
        } else if (key == "WrMEM") {
          InputManager.updateMem(inputs(0),inputs(1))
          updateRegMemOffOutput()
        } else {
          inputs(0)
        }
      }.toShort
      // not one of our extra information signals
      if (!ignoreKeys.contains(key)) {
        if (value) {
          DataPath.activate(key, activateFunc)
          println("Activating " + key)
        } else
          DataPath.deactivate(key)
      }
    }
    // at the end, make sure the ALU is updated
    DataPath.components("ALU").setOutputData(DataPath.components("ALU").readInputData().
      reduceLeft((j,k)=>(j+k).toShort))
    println("step forward pressed")
    currentStep += 1
    if (currentStep == currentInstruction.get.steps.length() || (step("LdZ") && DataPath.components("Z").readInputData()(0) != 0)) {
      completedInstructionsFromBeginning.append(currentInstruction.get)
      resetInstructionSimulation(false)
    }
  }
  def resetInstructionSimulation(fullReset: Boolean) {
    currentInstruction = None
    currentStep = 0
    InputManager.resetButtons()
    if (fullReset) {
      InputManager.updateStep(-1);
      for( key <- possibleSignals) {
        if (!ignoreKeys.contains(key)) {
          DataPath.deactivate(key)
        }
      }
    } else {
      InputManager.updateStep(0);
    }
  }
  def runInstruction(i: Int) {
    println("Exectute pressed")
    InputManager.startExecute()
    stepInstruction(i)
    Future {
      while (currentInstruction.isDefined) {
        Thread sleep waitTime
        stepInstruction(i)
      }
      Thread sleep waitTime
      DataPath.deactivateAll()
    }
    InputManager.endExecute()
  }
}


