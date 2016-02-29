import scala.collection.mutable.{Map,HashMap} 
package xyz.room409.lc2200 {

  object DatapathState {
    var memory = new HashMap[Int,Int]
    var registers = new HashMap[Int,Int]
    def updateMem(location: Int, value: Int) = memory(location) = value
    def getMemVal(location: Int) = memory getOrElse (location, 0)
    def getRegVal(location: Int) = registers getOrElse (location, 0)
    def updateReg(location: Int, value: Int) = registers(location) = value
  }

}

