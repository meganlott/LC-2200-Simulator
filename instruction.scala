import org.json._
import scala.collection.mutable.ArrayBuffer

class Instruction {
  var name : String = ""
  var steps : JSONArray = _

  /*
   * Call this function with the step index you want to get back, 0-indexed.
   * It will return an array of strings for signals.
   *
   * Eg. to get list of signals for step 1, instruction.getSignals(0)
   *
   * The array is set up so that it is ["signal name1","signal value1","signal name2", "signal
   * value2", .... ]. If the signal is not present, the signal value string will be the empty string
   * "".
   *
   * if you request a step that doesn't actually exist/beyond the actual steps, the list returned
   * will be empty.
   *
   * Every signal is currently present in the Array, this can change (probably should...)
   */
  val possibleSignals = List("LdPC", "DrPC", "LdA", "LdB", "DrALU", "Din", "WrREG", "DrREG", "LdMAR", "Addr", "Din", "WrMEM", "DrMEM", "LdIR")
  def getSignals(idx : Int) : Map[String,Boolean] = {
    var signalMap: Map[String, Boolean] = Map()
    if(steps.length() <= idx)
      throw new Exception("bad step index")

    val signals = steps.getJSONObject(idx)
    for (i <- possibleSignals) {
      try {
        signalMap += (i -> signals.getBoolean(i))
      } catch {
        case e: Exception => signalMap += (i -> false)
      }
    }
    return signalMap

  }
}
