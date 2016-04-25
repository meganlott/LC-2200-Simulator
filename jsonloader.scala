import org.json._
import scala.collection.mutable.ArrayBuffer

class JsonLoader {
  var instructions : ArrayBuffer[Instruction] = ArrayBuffer()
  def loadFile(file : String) {

    val txt = io.Source.fromFile("instructions/"+file)
    val lines = try txt.mkString finally txt.close()
    parseFile(lines)
  }

  def parseFile(data : String) {
    val inst = new Instruction()
    val obj = new JSONObject(data)
    inst.name = obj.getString("instruction")
    inst.steps = obj.getJSONArray("steps")
    instructions += inst
    /*val x : ArrayBuffer[String] = inst.getSignals(1)
    var s : String = ""
    for (s <- x) {
      System.out.println(s)
    } */
  }

  def loadInstructions() {
    /*
     * Load all of the instructions here, maybe we should scan a folder
     * and load all json files instead of listing them here?
     */
    loadFile("add.json")
    loadFile("nand.json")
    loadFile("addi.json")
    loadFile("sw.json")
    loadFile("lw.json")
    loadFile("beq.json")
    loadFile("jalr.json")
    loadFile("fetch.json")
  }
  
  /*
   * This is the only function the other code should really need to call.
   * Gets the list of instructions.
   *
   */
  def getInstructions() : ArrayBuffer[Instruction] = {
    if(instructions.length == 0) {
      loadInstructions()
    }
    return instructions
  }
}

