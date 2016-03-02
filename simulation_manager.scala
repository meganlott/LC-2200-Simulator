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
  val possibleSignals = List("LdPC", "DrPC", "ALUFunc", "DrALU", "Din", "WrREG", "DrREG", "LdMAR", "Addr", "Din", "WrMEM", "DrMEM", "LdA", "LdB", "LdIR")

  def stepInstruction(i: Int) {
    println("Current step: " + currentStep)
    if (!currentInstruction.isDefined || currentInstruction.get != instructions(i)) {
      currentInstruction = Some(instructions(i))
      currentStep = 0
    }
    var step = currentInstruction.get.getSignals(currentStep) //get all signals for step 0 of add
    for( key <- possibleSignals;
         value = step(key)) {
      //If the signal isn't ""
      val activateFunc = (inputs: Array[Short]) => {
        if (key == "ALUFunc")
          inputs.reduceLeft((j,k)=>(j+k).toShort)
        else if (key == "DrREG")
          1
        else
          inputs(0)
      }.toShort
      if (value) {
        DataPath.activate(key, activateFunc)
        println("Activating " + key)
      } else
        DataPath.deactivate(key)
    }
    println("step forward pressed")
    currentStep += 1
    if (currentStep == currentInstruction.get.steps.length()) {
      currentInstruction = None
      currentStep = 0
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


