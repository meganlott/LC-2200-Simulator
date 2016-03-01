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
  def getSignals(idx : Int) : ArrayBuffer[String] = {
    val ret : ArrayBuffer[String] = ArrayBuffer()
    if(steps.length() <= idx) {
      return ret
    }

    val signals = steps.getJSONObject(idx)

    //TODO(Marcus): There has got to be a better way of checking to see if a key is in a JSONObject,
    //All of these Try-catch blocks are nasty...
   
    ret += "loada"
    try {
      val loada : Boolean = signals.getBoolean("loada")
      ret += loada.toString()
    } catch {
      //case e : Exception => println("this is gonna be iffy...");
      case e : Exception => ret += "";
    }
    
    ret += "loadb"
    try {
      val loadb : Boolean = signals.getBoolean("loadb")
      ret += loadb.toString()
    } catch {
      case e : Exception => ret += "";
    }
    
    ret += "drreg"
    try {
      val drreg : Boolean = signals.getBoolean("drreg")
      ret += drreg.toString()
    } catch {
      case e : Exception => ret += "";
    }
    
    ret += "drualu"
    try {
      val dralu : Boolean = signals.getBoolean("dralu")
      ret += dralu.toString()
    } catch {
      case e : Exception => ret += "";
    }
    
    ret += "wrreg"
    try {
      val wrreg : Boolean = signals.getBoolean("wrreg")
      ret += wrreg.toString()
    } catch {
      case e : Exception => ret += "";
    }
    
    ret += "alufunc"
    try {
      val alufunc : String = signals.getString("alufunc")
      ret += alufunc
    } catch {
      case e : Exception => ret += "";
    }

    return ret

  }
}
