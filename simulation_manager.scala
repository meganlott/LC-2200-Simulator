
object SimulationManager {
  val loader = new JsonLoader()
  val instructions = loader.getInstructions() //get all instructions available
  var currentInstruction: Option[Instruction] = None
  var currentStep = 0

  def stepInstruction(i: Int) {
    println("Current step: " + currentStep)
    if (!currentInstruction.isDefined || currentInstruction.get != instructions(i)) {
      currentInstruction = Some(instructions(i))
      currentStep = 0
    }
    var step = currentInstruction.get.getSignals(currentStep) //get all signals for step 0 of add
    for((key,value) <- step) {
      //If the signal isn't ""
      if (value) {
        DataPath.activate(key)
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
    while (currentInstruction.isDefined) {
      Thread sleep 1000
      stepInstruction(i)
    }
  }
}


