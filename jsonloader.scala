import org.json._
import scala.collection.mutable.ArrayBuffer

/**
 * JsonLoader class is used for loading instructions saved as json files
 *
 * @author Marcus Godwin <godwin3@gatech.edu>
 * @version 1.0
 *
 */
class JsonLoader {
  var instructions : ArrayBuffer[Instruction] = ArrayBuffer()

  /**
   * Loads a file in as a string
   * @param file String, the name of the instruction to load
   * @return no return
   */
  def loadFile(file : String) {

    val txt = io.Source.fromFile("instructions/"+file)
    val lines = try txt.mkString finally txt.close()
    parseFile(lines)
  }

  /**
   * Parses a file into json objects and adds the instruction to the list of instructions
   * @param data String, the file as a string
   * @return no return
   */
  def parseFile(data : String) {
    val inst = new Instruction()
    val obj = new JSONObject(data)
    inst.name = obj.getString("instruction")
    inst.steps = obj.getJSONArray("steps")
    instructions += inst
  }

  /**
   * loads all the instruction files for the simulator
   * @return no return
   */
  def loadInstructions() {
    //Load all of the instructions here
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
   * @return instructions ArrayBuffer, the list of instructions for the simulator to use
   */
  def getInstructions() : ArrayBuffer[Instruction] = {
    if(instructions.length == 0) {
      loadInstructions()
    }
    return instructions
  }
}

