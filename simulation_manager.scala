import scala.concurrent._
import ExecutionContext.Implicits.global

object SimulationManager {
  val waitTime = 2000
  val loader = new JsonLoader()
  val instructions = loader.getInstructions() //get all instructions available
  var currentInstruction: Option[Instruction] = None
  var currentStep = 0
  // this is the update order of the datapath, so the order is very important
  // this is not the right way to do this
  val possibleSignals = List("UseSR2Hack", "LdPC", "DrPC", "ALUFunc", "ALUadd", "ALUnand", "DrALU", "Din", "WrREG", "DrREG", "UseDESTHack", "DrOFF", "LdMAR", "Addr", "Din", "WrMEM", "DrMEM", "LdA", "LdB", "LdIR")

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
    var useSR2insteadOfSR1 = false
    DataPath.components("sign\nextend").setOutputData(InputManager.getRegisterInput("sr2").toShort)
    def updateRegAndMemOutput() = {
      val output = (
        if (useSR2insteadOfSR1)
          InputManager.getRegVal( InputManager.getRegisterInput("sr2") )
        else if (step("UseDESTHack"))
          InputManager.getRegVal( InputManager.getRegisterInput("rd") )
        else
          InputManager.getRegVal( InputManager.getRegisterInput("sr1") )
        ).toShort
      DataPath.components("registers").setOutputData(output)
      DataPath.components("memory\n2^32 x\n32 bits").setOutputData(InputManager.getMemVal(DataPath.components("memory\n2^32 x\n32 bits").readInputData()(0)).toShort)
      output
    }
    updateRegAndMemOutput()
    for( key <- possibleSignals;
         value = step(key)) {
      if (key == "UseSR2Hack") {
        useSR2insteadOfSR1 = value
        println("USE SR2 INSTEAD OF SR1: " + value)
        updateRegAndMemOutput()
      } else {
        def activateFunc(inputs: Array[Short]) = {
          if (key == "ALUFunc") {
            if (step("ALUadd"))
              inputs.reduceLeft((j,k)=>(j+k).toShort)
            else if (step("ALUnand"))
              (~(inputs(0)&inputs(1))).toShort
            else {
              println("BAD INSTRUCTION")
              println(step)
              0
            }
          } else if (key == "WrREG") {
            InputManager.updateReg(InputManager.getRegisterInput("rd") ,inputs(0))
            updateRegAndMemOutput()
          } else if (key == "WrMEM") {
            InputManager.updateMem(inputs(0),inputs(1))
            updateRegAndMemOutput()
          } else {
            inputs(0)
          }
        }.toShort
        // not one of our extra information signals
        if (key != "ALUadd" && key != "ALUnand" && key != "UseDESTHack") {
          if (value) {
            DataPath.activate(key, activateFunc)
            println("Activating " + key)
          } else
            DataPath.deactivate(key)
        }
      }
    }
    // at the end, make sure the ALU is updated
    DataPath.components("ALU").setOutputData(DataPath.components("ALU").readInputData().
      reduceLeft((j,k)=>(j+k).toShort))
    println("step forward pressed")
    currentStep += 1
    if (currentStep == currentInstruction.get.steps.length()) {
      currentInstruction = None
      currentStep = 0
      InputManager.updateStep(0);
      InputManager.resetButtons()
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


