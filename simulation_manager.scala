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
  val possibleSignals = List("UseSR2Hack", "LdPC", "DrPC", "ALUFunc", "DrALU", "Din", "WrREG", "DrREG", "LdMAR", "Addr", "Din", "WrMEM", "DrMEM", "LdA", "LdB", "LdIR")

  def stepInstruction(i: Int) {
    println("Current step: " + currentStep)
    InputManager.updateStep(currentStep+1);
    if (!currentInstruction.isDefined || currentInstruction.get != instructions(i)) {
      currentInstruction = Some(instructions(i))
      currentStep = 0
    }
    var step = currentInstruction.get.getSignals(currentStep) //get all signals for step 0 of add
    // first make sure regs is outputing right info
    var useSR2insteadOfSR1 = false
    def updateRegOutput() = {
      val output = (
        if (useSR2insteadOfSR1)
          InputManager.getRegVal( InputManager.getRegisterInput("sr2") )
        else
          InputManager.getRegVal( InputManager.getRegisterInput("sr1") )
        ).toShort
      DataPath.components("registers").setOutputData(output)
      output
    }
    updateRegOutput()
    for( key <- possibleSignals;
         value = step(key)) {
      if (key == "UseSR2Hack") {
        useSR2insteadOfSR1 = value
        println("USE SR2 INSTEAD OF SR1: " + value)
        updateRegOutput()
      } else {
        def activateFunc(inputs: Array[Short]) = {
          if (key == "ALUFunc") {
            inputs.reduceLeft((j,k)=>(j+k).toShort)
          } else if (key == "WrREG") {
            InputManager.updateReg(InputManager.getRegisterInput("rd") ,inputs(0))
            updateRegOutput()
          } else {
            inputs(0)
          }
        }.toShort
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
    if (currentStep == currentInstruction.get.steps.length()) {
      currentInstruction = None
      currentStep = 0
      InputManager.updateStep(0);
    }
  }
  def runInstruction(i: Int) {
    println("Exectute pressed")
    stepInstruction(i)
    Future {
      while (currentInstruction.isDefined) {
        Thread sleep waitTime
        stepInstruction(i)
      }
      Thread sleep waitTime
      DataPath.deactivateAll()
    }
  }
}


