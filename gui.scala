import scala.swing._

class UI extends MainFrame {
  title = "GUI Program #1";
  preferredSize = new Dimension(640, 480);
  contents = new Label("Here is the contents!");
  contents = new FlowPanel { 
    contents += new Button("huhu") { 
      minimumSize = preferredSize
      maximumSize = preferredSize
      preferredSize = preferredSize
    }
  }
}

object GuiProgramOne {
  def main(arg: Array[String]) {
    val ui = new UI
    ui.visible = true
    println("End of main function.")
  }
}
